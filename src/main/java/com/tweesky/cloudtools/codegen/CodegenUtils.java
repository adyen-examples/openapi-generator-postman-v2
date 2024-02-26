package com.tweesky.cloudtools.codegen;

import org.openapitools.codegen.CodegenResponse;

public class CodegenUtils {

    /**
     * get HTTP Status Code as text
     *
     * @param codegenResponse
     * @return
     */
    public static String getStatus(CodegenResponse codegenResponse) {
        String ret = "";

        if (codegenResponse.is2xx) {
            if (codegenResponse.code.equalsIgnoreCase("200")) {
                ret = "OK";
            } else if (codegenResponse.code.equalsIgnoreCase("201")) {
                ret = "Created";
            } else {
                ret = "Success";
            }
        } else if (codegenResponse.is3xx) {
            ret = "Redirection";
        }
        if (codegenResponse.is4xx) {
            if (codegenResponse.code.equalsIgnoreCase("400")) {
                ret = "Bad Request";
            } else if (codegenResponse.code.equalsIgnoreCase("401")) {
                ret = "Unauthorized";
            } else if (codegenResponse.code.equalsIgnoreCase("403")) {
                ret = "Forbidden";
            } else if (codegenResponse.code.equalsIgnoreCase("404")) {
                ret = "Not Found";
            } else if (codegenResponse.code.equalsIgnoreCase("409")) {
                ret = "Conflict";
            } else {
                ret = "Client Error";
            }
        }
        if (codegenResponse.is5xx) {
            if (codegenResponse.code.equalsIgnoreCase("500")) {
                ret = "Internal Server Error";
            } else if (codegenResponse.code.equalsIgnoreCase("501")) {
                ret = "Not Implemented";
            } else {
                ret = "Server Error";
            }
        }

        return ret;
    }
}