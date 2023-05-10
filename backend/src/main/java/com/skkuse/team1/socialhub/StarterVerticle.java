package com.skkuse.team1.socialhub;

import com.skkuse.team1.socialhub.routes.annotations.APIRouteType;
import com.skkuse.team1.socialhub.routes.processed.APIRoutesManager;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class StarterVerticle extends AbstractVerticle {

    private static final String API_BASEURL = "/api/";

    @Override
    public void start(Promise<Void> startPromise) {
        Router rootRouter = Router.router(vertx);

        rootRouter.route().handler(BodyHandler.create());

        Router router = Router.router(vertx);

        router.route().handler((ctx) -> {
            // HACK: Temporary Logger:
            System.out.printf("'%s' Request on: %s%n", ctx.request().method(), ctx.request().path());
            ctx.next();
        });

        //TODO: Implement OpenAPI.

        // TODO: Use proper crypto keys:
        JWTAuth authProvider = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer("temporary secret")));

        router.route(APIRouteType.PROTECTED+"*").handler(JWTAuthHandler.create(authProvider));

        APIRoutesManager.registerAllRoutes(vertx, router, authProvider)
                .compose((v) -> {
                    // HACK: Temporary 404 Handler:
                    router.route().handler((ctx) -> {
                        ctx.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end("404");
                    });

                    rootRouter.route(API_BASEURL+"*").subRouter(router);
                    return ConfigRetriever.create(vertx).getConfig();
                })
                .compose((configRetriever) ->
                        vertx.createHttpServer()
                                .requestHandler(rootRouter)
                                .listen(
                                        configRetriever.getInteger(Config.KEY_PORT, Config.DEFAULT_PORT),
                                        configRetriever.getString(Config.KEY_HOST, Config.DEFAULT_HOST)
                                )
                )
                .compose(
                        (httpServer) -> Future.<Void>succeededFuture()
                )
                .onComplete(startPromise);

    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        //TODO: Cleanup
    }

}
