package com.skkuse.team1.socialhub.routes;

import com.skkuse.team1.socialhub.routes.annotations.APIRoute;
import com.skkuse.team1.socialhub.routes.annotations.APIRouteType;
import com.skkuse.team1.socialhub.routes.annotations.APIRouter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

@APIRouter(baseURL = "test/")
public class TestRoutes {
    @APIRoute(type = APIRouteType.PUBLIC)
    public static Future<Router> buildTestRoutes(Vertx vertx){
        Router router = Router.router(vertx);

        // Setting up "test/user" GET handler:
        router.get("/user").handler((ctx) -> {
            System.out.println("Test! USER: " + ctx.request().absoluteURI());
            // Respond with "HTML END" text.
            ctx.response().end("HTML END");
        });

        // Setting up "test/post" POST handler:
        router.post("/post").handler((ctx) -> {
            // Print out the body of the request:
            System.out.println(ctx.body().asString());
            ctx.response().end();
        });

        // Return our router as Future:
        return Future.succeededFuture(router);
    }
}
