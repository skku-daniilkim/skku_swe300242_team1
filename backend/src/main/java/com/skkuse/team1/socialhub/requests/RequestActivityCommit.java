package com.skkuse.team1.socialhub.requests;

import com.skkuse.team1.socialhub.model.Activity;
import io.vertx.ext.auth.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestActivityCommit implements Serializable {
    private String title;
    private String description;
    private String location;
    private LocalDateTime dateTime;
    private List<Long> idParticipantList = new ArrayList<>();

    public RequestActivityCommit() {}

    public RequestActivityCommit(String title, String description, String location, LocalDateTime dateTime, Long... idParticipant) {
        this(title, description, location, dateTime, Arrays.stream(idParticipant).toList());
    }

    public RequestActivityCommit(String title, String description, String location, LocalDateTime dateTime, List<Long> idParticipantList) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.idParticipantList = idParticipantList;
        this.dateTime = dateTime;
    }

    public String getTitle() {
        return title;
    }

    public RequestActivityCommit setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RequestActivityCommit setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public RequestActivityCommit setLocation(String location) {
        this.location = location;
        return this;
    }

    public List<Long> getIdParticipantList() {
        return idParticipantList;
    }

    public RequestActivityCommit setIdParticipantList(List<Long> idParticipantList) {
        this.idParticipantList = idParticipantList;
        return this;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public RequestActivityCommit setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public Activity formActivity(User user) {
        return new Activity(
                user.principal().getLong("sub"),
                idParticipantList.size(),
                title,
                description,
                dateTime,
                location
        ).setIdParticipantList(this.idParticipantList);
    }
}
