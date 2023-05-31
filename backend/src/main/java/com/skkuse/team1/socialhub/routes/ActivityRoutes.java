package com.skkuse.team1.socialhub.routes;

import com.skkuse.team1.socialhub.ExceptionWithHttpCode;
import com.skkuse.team1.socialhub.jdbc.impl.ActivityJDBC;
import com.skkuse.team1.socialhub.model.Activity;
import com.skkuse.team1.socialhub.requests.RequestActivityCommit;
import com.skkuse.team1.socialhub.routes.annotations.APIRoute;
import com.skkuse.team1.socialhub.routes.annotations.APIRouteType;
import com.skkuse.team1.socialhub.routes.annotations.APIRouter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.RouterBuilder;

import java.util.List;

@APIRouter(baseURL = "activities/")
public class ActivityRoutes {

    @APIRoute(type = APIRouteType.PROTECTED)
    public static Future<Router> buildProtected(Vertx vertx){
        return RouterBuilder.create(vertx, "apis/activities.yaml").map(rb -> {
            rb.operation("commit").handler(ActivityRoutes::apiCommit);
            rb.operation("get").handler(ActivityRoutes::apiGet);
            rb.operation("update").handler(ActivityRoutes::apiUpdate);
            rb.operation("delete").handler(ActivityRoutes::apiDelete);
            return rb.createRouter();
        });
    }

    private static void apiCommit(RoutingContext ctx) {
        RequestActivityCommit request = ctx.body().asJsonObject().mapTo(RequestActivityCommit.class);
        ActivityJDBC.instance().create(List.of(request.formActivity(ctx.user()))).onComplete(ar -> {
            if(ar.failed()) ctx.fail(ar.cause());
            JsonArray array = new JsonArray();
            ar.result().stream().map(JsonObject::mapFrom).forEach(array::add);
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json;charset=utf-8").end(array.encode());
        });
    }

    private static void apiGet(RoutingContext ctx) {
        ActivityJDBC.instance().get(ctx.user().principal().getLong("sub")).onComplete(ar -> {
            if(ar.failed()) ctx.fail(ar.cause());
            JsonArray array = new JsonArray();
            ar.result().stream().map(JsonObject::mapFrom).forEach(array::add);
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json;charset=utf-8").end(array.encode());
        });
    }

    private static void apiUpdate(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.request().getParam("id"));
        ActivityJDBC.instance().getById(id).compose(found -> {
            if(found == null || found.getIdUserCreate() != ctx.user().principal().getLong("sub").longValue()) return Future.failedFuture(new ExceptionWithHttpCode(HttpResponseStatus.FORBIDDEN.code(), "Only owner can update activity"));
            return ActivityJDBC.instance().update(ctx.body().asPojo(Activity.class).setId(id));
        }).onFailure(ctx::fail).onSuccess(aVoid -> ctx.response().end());
    }

    private static void apiDelete(RoutingContext ctx) {
        Long id = Long.valueOf(ctx.request().getParam("id"));
        ActivityJDBC.instance().getById(id).compose(found -> {
            if(found == null || found.getIdUserCreate() != ctx.user().principal().getLong("sub").longValue()) return Future.failedFuture(new ExceptionWithHttpCode(HttpResponseStatus.FORBIDDEN.code(), "Only owner can update activity"));
            return ActivityJDBC.instance().delete(id);
        }).onFailure(ctx::fail).onSuccess(aVoid -> ctx.response().end());
    }
}
