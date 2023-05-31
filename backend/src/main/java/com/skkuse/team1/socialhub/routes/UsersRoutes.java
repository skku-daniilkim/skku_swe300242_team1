package com.skkuse.team1.socialhub.routes;

import com.skkuse.team1.socialhub.jdbc.impl.UserJDBC;
import com.skkuse.team1.socialhub.requests.RequestChangePassword;
import com.skkuse.team1.socialhub.requests.RequestLogin;
import com.skkuse.team1.socialhub.routes.annotations.APIRoute;
import com.skkuse.team1.socialhub.routes.annotations.APIRouteType;
import com.skkuse.team1.socialhub.routes.annotations.APIRouter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;

@APIRouter(baseURL = "users/")
public class UsersRoutes {
    static JWTAuth auth;
    @APIRoute(type = APIRouteType.PUBLIC)
    public static Future<Router> buildRoutes(Vertx vertx){
        auth = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(new PubSecKeyOptions()
                .setAlgorithm("HS256")
                .setBuffer("temporary secret")));
        return RouterBuilder.create(vertx, "apis/users.yaml").map(rb -> {
            rb.operation("login").handler(UsersRoutes::apiLogin);
            return rb.createRouter();
        });
    }

    @APIRoute(type = APIRouteType.PROTECTED)
    public static Future<Router> buildProtected(Vertx vertx) {
        return RouterBuilder.create(vertx, "apis/users.yaml").map(rb -> {
            rb.operation("changePassword").handler(UsersRoutes::apiChangePassword);
            rb.operation("get").handler(UsersRoutes::apiCurrentInfo);
            return rb.createRouter();
        });
    }

    private static void apiLogin(RoutingContext ctx) {
        RequestLogin request = ctx.body().asPojo(RequestLogin.class);
        UserJDBC.instance().login(request.getLogin(), request.getPassword()).onComplete(ar -> {
            if(ar.succeeded()) {
                // https://www.rfc-editor.org/rfc/rfc6749#section-5.1
                ctx.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json;charset=utf-8")
                        .end(new JsonObject()
                                .put("access_token", auth.generateToken(new JsonObject().put("sub", ar.result().getId())))
                                .put("token_type", "Bearer")
                                .encode());
            }else{
                ctx.fail(ar.cause());
            }
        });
    }

    private static void apiChangePassword(RoutingContext ctx) {
        RequestChangePassword request = ctx.body().asPojo(RequestChangePassword.class);
        UserJDBC.instance().changePassword(ctx.user().principal().getLong("sub"), request.getPassword(), request.getOldPassword())
                .onFailure(ctx::fail)
                .onSuccess(aVoid -> ctx.response().end());
    }

    private static void apiCurrentInfo(RoutingContext ctx) {
        UserJDBC.instance().getById(ctx.user().principal().getLong("sub")).onFailure(ctx::fail).onSuccess(user -> {
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json;charset=utf-8").end(JsonObject.mapFrom(user).encode());
        });
    }
}
