package com.tweesky.cloudtools.codegen;

import io.swagger.v3.oas.models.responses.ApiResponse;
import junit.framework.TestCase;
import org.openapitools.codegen.CodegenResponse;
import org.openapitools.codegen.DefaultCodegen;

public class CodegenUtilsTest extends TestCase {

    private final DefaultCodegen defaultCodegen = new DefaultCodegen();

    public void testGetStatusForNullValue() {
        assertEquals(
                CodegenUtils.getStatus(null),
                "");
    }

    public void testGetStatusForEmptyValue() {
        assertEquals(
                CodegenUtils.getStatus(new CodegenResponse()),
                "");
    }


    public void testGetStatusForKnownvalues() {
        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("200", new ApiResponse().description("200 response"))),
                "OK");

        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("201", new ApiResponse().description("201 response"))),
                "Created");

        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("202", new ApiResponse().description("202 response"))),
                "Accepted");

        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("400", new ApiResponse().description("400 response"))),
                "Bad Request");

        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("409", new ApiResponse().description("409 response"))),
                "Conflict");

        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("500", new ApiResponse().description("500 response"))),
                "Internal Server Error");

        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("501", new ApiResponse().description("501 response"))),
                "Not Implemented");
    }

    public void testGetStatusForUnknownvalue() {
        assertEquals(
                CodegenUtils.getStatus(defaultCodegen.fromResponse("599", new ApiResponse().description("599 response"))),
                "");
    }
}