package com.adyen.codegen.postman.model;

import java.util.Objects;

public class PostmanVariable {

    private String name;
    private String type;
    private String defaultValue;

    public PostmanVariable addName(String name) {
        this.name = name;
        return this;
    }

    public PostmanVariable addType(String type) {
        this.type = type;
        return this;
    }

    public PostmanVariable addDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostmanVariable that = (PostmanVariable) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "PostmanVariable{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
