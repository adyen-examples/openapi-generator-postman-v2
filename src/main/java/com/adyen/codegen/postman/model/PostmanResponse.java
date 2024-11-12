package com.adyen.codegen.postman.model;

import com.adyen.codegen.postman.CodegenUtils;
import org.openapitools.codegen.CodegenResponse;

public class PostmanResponse {

    private String id;
    private String code;
    private String status;
    private String name;
    private String body;

    // Somehow Postman decided that response examples should contain the request data again...
    private PostmanRequestItem originalRequest;

    public PostmanResponse(String id, CodegenResponse response, String name, String body) {
        this.id = id;
        this.code = response.code;
        this.status = CodegenUtils.getStatus(response);
        this.name = name;
        this.body = body;
        this.originalRequest = null; // Setting this here explicitly for clarity
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    private String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PostmanRequestItem getOriginalRequest() { return originalRequest; }

    public void setOriginalRequest(PostmanRequestItem originalRequest) { this.originalRequest = originalRequest; }
}
