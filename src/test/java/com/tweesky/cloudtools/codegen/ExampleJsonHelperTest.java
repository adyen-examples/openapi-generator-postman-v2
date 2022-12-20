package com.tweesky.cloudtools.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class ExampleJsonHelperTest {

    @Test
    public void getPostmanTypeNumber() {
        CodegenProperty codegenProperty = new CodegenProperty();
        codegenProperty.isNumeric = true;

        assertEquals("number", new ExampleJsonHelper().getPostmanType(codegenProperty));
    }

    @Test
    public void getPostmanTypeDate() {
        CodegenProperty codegenProperty = new CodegenProperty();
        codegenProperty.isDate = true;

        assertEquals("date", new ExampleJsonHelper().getPostmanType(codegenProperty));
    }

    @Test
    public void getPostmanTypeString() {
        CodegenProperty codegenProperty = new CodegenProperty();
        codegenProperty.isString = true;

        assertEquals("string", new ExampleJsonHelper().getPostmanType(codegenProperty));
    }

    @Test
    public void getExampleFromSchema() {
        final String EXPECTED = "{\\n \\\"firstname\\\": \\\"<string>\\\",\\n \\\"lastname\\\": \\\"<string>\\\",\\n \\\"age\\\": \\\"<number>\\\",\\n \\\"birthDate\\\": \\\"<date>\\\"\\n}";

        CodegenParameter codegenParameter = new CodegenParameter();
        codegenParameter.vars.add(new CodegenProperty() {{baseName = "firstname"; isString = true;}});
        codegenParameter.vars.add(new CodegenProperty() {{baseName = "lastname"; isString = true;}});
        codegenParameter.vars.add(new CodegenProperty() {{baseName = "age"; isNumeric = true;}});
        codegenParameter.vars.add(new CodegenProperty() {{baseName = "birthDate"; isDate = true;}});

        assertEquals(EXPECTED, new ExampleJsonHelper().getJsonFromSchema(codegenParameter));
    }

    @Test
    public void formatJson() {

        final String EXPECTED = "{\\n \\\"id\\\": 1,\\n \\\"city\\\": \\\"Amsterdam\\\"\\n}";
        final String JSON = "{\"id\":1,\"city\":\"Amsterdam\"}";

        assertEquals(EXPECTED, new ExampleJsonHelper().formatJson(JSON));

    }

    @Test
    public void convertObjectNodeToJson() {

        final String EXPECTED = "{\\n \\\"id\\\": 1,\\n \\\"city\\\": \\\"Amsterdam\\\"\\n}";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode city = mapper.createObjectNode();

        city.put("id", 1);
        city.put("city", "Amsterdam");

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(city));

    }

    @Test
    public void convertLinkedHashMapToJson() {

        final String EXPECTED = "{\\n \\\"id\\\": 1,\\n \\\"city\\\": \\\"Amsterdam\\\"\\n}";

        LinkedHashMap<String, Object> city = new LinkedHashMap<>();
        city.put("id", 1);
        city.put("city", "Amsterdam");

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(city));

    }

    @Test
    public void convertNestedLinkedHashMapToJson() {

        final String EXPECTED =
                "{\\n " +
                        "\\\"id\\\": 1,\\n \\\"city\\\": \\\"Amsterdam\\\",\\n " +
                        "\\\"country\\\": {\\n \\\"id\\\": 2,\\n \\\"code\\\": \\\"NL\\\"\\n}" +
                        "\\n}";

        LinkedHashMap<String, Object> city = new LinkedHashMap<>();
        city.put("id", 1);
        city.put("city", "Amsterdam");
        LinkedHashMap<String, Object> country = new LinkedHashMap<>();
        country.put("id", 2);
        country.put("code", "NL");
        city.put("country", country);

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(city));

    }

}
