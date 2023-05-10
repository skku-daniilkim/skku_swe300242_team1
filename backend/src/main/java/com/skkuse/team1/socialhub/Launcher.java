package com.skkuse.team1.socialhub;

import io.vertx.core.Vertx;

public class Launcher {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(StarterVerticle.class.getName()).onComplete(ar -> {
            if(ar.failed()){
                ar.cause().printStackTrace();
                vertx.close();
            }
        });
    }

}
