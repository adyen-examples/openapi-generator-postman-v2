package com.tweesky.cloudtools.codegen.model;

public class PostmanRequestItem {

    private String id;
    private String name;
    private String body;

    private PostmanResponse response;

    public PostmanRequestItem() {
    }

    public PostmanRequestItem(String name, String body, String id) {
        this.id = id;
        this.name = name;
        this.body = body;
    }
    public PostmanRequestItem(String name, String body) {
        this.name = name;
        this.body = body;
        this.id = ""; // TODO : do better later
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PostmanResponse getResponse() {
        return response;
    }

    public void setResponse(PostmanResponse response) {
        this.response = response;
    }
}
