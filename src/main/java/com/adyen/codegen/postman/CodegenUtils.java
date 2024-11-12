package com.adyen.codegen.postman;

import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.openapitools.codegen.CodegenResponse;

import java.net.HttpURLConnection;
import java.util.Objects;

public class CodegenUtils {

    /**
     * get HTTP Status Code as text
     *
     * @param codegenResponse response information from a request
     * @return HTTP Status code information, as text description
     */
    public static String getStatus(CodegenResponse codegenResponse) {
        if(codegenResponse == null || codegenResponse.code == null || codegenResponse.code.isEmpty()){
            return "";
        }

        return Objects.requireNonNullElse(
                EnglishReasonPhraseCatalog.INSTANCE.getReason(Integer.parseInt(codegenResponse.code), null)
                , ""
        );
    }
}