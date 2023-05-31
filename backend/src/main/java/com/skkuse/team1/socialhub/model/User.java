package com.skkuse.team1.socialhub.model;

public class User {
    private Long id;
    private String username;
    private String password;
    private Integer idSecurityQuestion;
    private String securityAnswer;

    public User() {}

    public User(String username, String password, Integer idSecurityQuestion, String securityAnswer) {
        this.username = username;
        this.password = password;
        this.idSecurityQuestion = idSecurityQuestion;
        this.securityAnswer = securityAnswer;
    }

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getIdSecurityQuestion() {
        return idSecurityQuestion;
    }

    public User setIdSecurityQuestion(Integer idSecurityQuestion) {
        this.idSecurityQuestion = idSecurityQuestion;
        return this;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public User setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
        return this;
    }
}
