package com.skkuse.team1.socialhub.routes.annotations;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.skkuse.team1.socialhub.routes.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class APIRouterProcessor extends AbstractProcessor {

    private record APIRouterEntry(String className, String baseURL, List<APIRouteEntry> routes){
        private APIRouterEntry(String className, String baseURL) {
            this(className, baseURL, new LinkedList<>());
        }
    }

    private record APIRouteEntry(APIRouteType type, int version, String methodName, boolean provideAuth){}

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Set<Modifier> modifiers = Set.of(Modifier.PUBLIC, Modifier.STATIC);

        @SuppressWarnings("unchecked")
        final Set<TypeElement> classes = (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(APIRouter.class);

        List<APIRouterEntry> entries = new LinkedList<>();

        for (final TypeElement clazz : classes) {
            APIRouterEntry entry = new APIRouterEntry(clazz.getQualifiedName().toString(), clazz.getAnnotation(APIRouter.class).baseURL());
            for (final Element el : clazz.getEnclosedElements()) {
                APIRoute routeAnnotation = el.getAnnotation(APIRoute.class);
                if (routeAnnotation != null) {
                    ExecutableElement method = (ExecutableElement) el;
                    // Validate the signature:
                    if (!method.getModifiers().containsAll(modifiers))
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error! Method: " + el.getSimpleName() + " in Class: " + clazz.getSimpleName() + " must be 'public static'");

                    if (!method.getReturnType().toString().equals("io.vertx.core.Future<io.vertx.ext.web.Router>"))
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error! Method: " + el.getSimpleName() + " in Class: " + clazz.getSimpleName() + " must return 'io.vertx.core.Future<io.vertx.ext.web.Router>'");

                    if (method.getParameters().size() != 1 && method.getParameters().size() != 2) {
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error! Method: " + el.getSimpleName() + " in Class: " + clazz.getSimpleName() + " must have at most only 2 arguments: 'io.vertx.core.Vertx' and 'io.vertx.ext.auth.jwt.JWTAuth'");
                    }

                    if (!method.getParameters().get(0).asType().toString().equals("io.vertx.core.Vertx")){
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error! Method: " + el.getSimpleName() + " in Class: " + clazz.getSimpleName() + " 1st argument must be of type 'io.vertx.core.Vertx'");
                    }
                    if (method.getParameters().size() == 2 && !method.getParameters().get(1).asType().toString().equals("io.vertx.ext.auth.jwt.JWTAuth")){
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error! Method: " + el.getSimpleName() + " in Class: " + clazz.getSimpleName() + " 2nd argument must be of type 'io.vertx.ext.auth.jwt.JWTAuth'");
                    }

                    entry.routes.add(new APIRouteEntry(routeAnnotation.type(), routeAnnotation.version(), method.getSimpleName().toString(), method.getParameters().size() == 2));
                }
            }
            entries.add(entry);
        }
        if(!entries.isEmpty())
            generate(entries);
        return true;
    }

    private void generate(List<APIRouterEntry> entries){
        final String packageName = "com.skkuse.team1.socialhub.routes.processed";
        final String className = "APIRoutesManager";
        final String fullName = packageName + "." + className;

        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(fullName).openWriter())) {
            writer.println("""
                    package %s;
                                        
                    import io.vertx.core.Vertx;
                    import io.vertx.core.Future;
                    import io.vertx.core.CompositeFuture;
                    import io.vertx.ext.web.Router;
                    import io.vertx.ext.auth.jwt.JWTAuth;
                    
                    import java.lang.Void;
                         
                    public class %s {
                    """
                    .formatted(packageName, className)
            );

            writer.print("""
                        private static Future<Void> mountRouter(Router mainRouter, String url, Router subRouter){
                            mainRouter.route(url).subRouter(subRouter);
                            return Future.succeededFuture();
                        }
                        public static Future<Void> registerAllRoutes(Vertx vertx, Router mainRouter, JWTAuth authProvider) {
                            return CompositeFuture.all(
                    """
            );

            StringBuilder composed = new StringBuilder();
            for(APIRouterEntry entry : entries){
                for(var route : entry.routes){
//                    writer.print("""
//                                    router.route("/%s%s*").subRouter(%s.%s(vertx));
//                        """.formatted(route.getKey() == Route.RouteType.PUBLIC ? "public" : "protected", entry.baseURL, entry.className, route.getValue())
//                    );
                    composed.append("""
                                        %s.%s(vertx%s).compose(router -> mountRouter(mainRouter, "%s%s/%s*", router)),                        
                            """.formatted(entry.className, route.methodName, route.provideAuth ? ", authProvider" : "", route.type, "v"+route.version, entry.baseURL));
                }
            }
            composed.deleteCharAt(composed.lastIndexOf(","));
            writer.print(composed);
            writer.print("""
                            ).compose(temp -> Future.<Void>succeededFuture());
                        }
                    }
                    """);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

