package com.skkuse.team1.socialhub.routes.annotations;

public enum APIRouteType {
    PUBLIC("/public/"), PROTECTED("/protected/");

    private final String baseURL;
    APIRouteType(String baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public String toString(){
        return baseURL;
    }
}
