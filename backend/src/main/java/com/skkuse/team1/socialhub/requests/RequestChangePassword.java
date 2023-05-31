package com.skkuse.team1.socialhub.requests;

import java.io.Serializable;

public class RequestChangePassword implements Serializable {
    private String password;
    private String oldPassword;

    public RequestChangePassword(){}

    public RequestChangePassword(String password, String oldPassword) {
        this.password = password;
        this.oldPassword = oldPassword;
    }

    public String getPassword() {
        return password;
    }

    public RequestChangePassword setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public RequestChangePassword setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
        return this;
    }
}
