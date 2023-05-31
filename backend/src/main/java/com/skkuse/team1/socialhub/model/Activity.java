package com.skkuse.team1.socialhub.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Activity {
    private Long id;
    private Long idUserCreate;
    private Integer participantCount;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private List<Long> idParticipantList = new ArrayList<>();

    // Пустой конструктор приватный для сериализации json
    private Activity() {}

    public Activity(Long idUserCreate, Integer participantCount, String title, String description, LocalDateTime dateTime, String location) {
        this.idUserCreate = idUserCreate;
        this.participantCount = participantCount;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public Activity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getIdUserCreate() {
        return idUserCreate;
    }

    public Activity setIdUserCreate(Long idUserCreate) {
        this.idUserCreate = idUserCreate;
        return this;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public Activity setParticipantCount(Integer participantCount) {
        this.participantCount = participantCount;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Activity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Activity setDescription(String description) {
        this.description = description;
        return this;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Activity setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Activity setLocation(String location) {
        this.location = location;
        return this;
    }

    public List<Long> getIdParticipantList() {
        return idParticipantList;
    }

    public Activity setIdParticipantList(List<Long> idParticipantList) {
        this.idParticipantList = idParticipantList;
        return this;
    }
}
