package com.skkuse.team1.socialhub.requests;

import java.io.Serializable;

public class RequestLogin implements Serializable {
    private String login;
    private String password;

    private RequestLogin() {}
    public RequestLogin(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public RequestLogin setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RequestLogin setPassword(String password) {
        this.password = password;
        return this;
    }
}
