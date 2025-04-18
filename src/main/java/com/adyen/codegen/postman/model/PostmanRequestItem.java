package com.adyen.codegen.postman.model;

import java.util.ArrayList;
import java.util.List;

public class PostmanRequestItem {

    private String id;
    private String name;
    private String body;
    private String httpMethod;

    private List<PostmanResponse> responses;

    public PostmanRequestItem(String name, String body, String id, String httpMethod) {
        this.id = id;
        this.name = name;
        this.body = body;
        this.httpMethod = httpMethod;
    }
    public PostmanRequestItem(String name, String body, String httpMethod) {
        this.name = name;
        this.body = body;
        this.httpMethod = httpMethod;
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

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<PostmanResponse> getResponses() {
        return responses;
    }

    public void addResponse(PostmanResponse response) {
        if(this.responses == null) { this.responses = new ArrayList<>(); }

        this.responses.add(response);
    }

    public void addResponses(List<PostmanResponse> responses) {
        if(this.responses == null) { this.responses = new ArrayList<>(); }

        this.responses.addAll(responses);
    }
}
