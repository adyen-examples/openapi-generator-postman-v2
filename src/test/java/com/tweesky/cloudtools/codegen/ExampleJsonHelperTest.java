package com.tweesky.cloudtools.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;

import java.util.ArrayList;
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

        final String EXPECTED = "{\\n  \\\"id\\\" : 1,\\n  \\\"city\\\" : \\\"Amsterdam\\\"\\n}";
        final String JSON = "{\"id\":1,\"city\":\"Amsterdam\"}";

        assertEquals(EXPECTED, new ExampleJsonHelper().formatJson(JSON));

    }

    @Test
    public void formatJsonIncludingCommas() {

        final String EXPECTED = "{\\n  \\\"id\\\" : 1,\\n  \\\"list\\\" : \\\"AMS,LON,ROM\\\"\\n}";
        final String JSON = "{\"id\":1,\"list\":\"AMS,LON,ROM\"}";

        assertEquals(EXPECTED, new ExampleJsonHelper().formatJson(JSON));

    }

    @Test
    public void formatJsonWithUrl() {

        final String EXPECTED = "{\\n  \\\"id\\\" : 1,\\n  \\\"url\\\" : \\\"https://github.com\\\"\\n}";
        final String JSON = "{\"id\": 1,\"url\": \"https://github.com\"}";

        assertEquals(EXPECTED, new ExampleJsonHelper().formatJson(JSON));

    }

    @Test
    public void getAttributesFromJson() {

        final String JSON = "{\"id\":1,\"list\":\"AMS,LON,ROM\"}";
        assertEquals(2, new ExampleJsonHelper().getAttributes(JSON).length);

    }

    @Test
    public void convertObjectNodeToJson() {

        final String EXPECTED = "{\\n  \\\"id\\\" : 1,\\n  \\\"city\\\" : \\\"Amsterdam\\\"\\n}";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode city = mapper.createObjectNode();

        city.put("id", 1);
        city.put("city", "Amsterdam");

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(city));

    }

    @Test
    public void convertObjectNodeIncludingDoubleQuoteToJson() {

        final String EXPECTED = "{\\n  \\\"id\\\" : 1,\\n  \\\"city\\\" : \\\"it is \\\\\"Amsterdam\\\\\" \\\"\\n}";

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode city = mapper.createObjectNode();

        city.put("id", 1);
        city.put("city", "it is \"Amsterdam\" ");

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

    @Test
    public void convertNestedArrayListToJson() {

        final String EXPECTED =
                "{\\n " +
                        "\\\"id\\\": 1,\\n \\\"city\\\": \\\"Amsterdam\\\",\\n " +
                        "\\\"tags\\\": [\\\"ams\\\", \\\"adam\\\"]" +
                        "\\n}";

        LinkedHashMap<String, Object> city = new LinkedHashMap<>();
        city.put("id", 1);
        city.put("city", "Amsterdam");
        ArrayList<String> tags = new ArrayList<>();
        tags.add("ams");
        tags.add("adam");
        city.put("tags", tags);

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(city));

    }

    @Test
    public void convertNestedEmptyArrayListToJson() {

        final String EXPECTED =
                "{\\n " +
                        "\\\"id\\\": 1,\\n \\\"city\\\": \\\"Amsterdam\\\",\\n " +
                        "\\\"tags\\\": []" +
                        "\\n}";

        LinkedHashMap<String, Object> city = new LinkedHashMap<>();
        city.put("id", 1);
        city.put("city", "Amsterdam");
        ArrayList<String> tags = new ArrayList<>();
        city.put("tags", tags);

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(city));

    }

    @Test
    public void convertNestedArrayListIncludingDoubleQuotesToJson() {

        final String EXPECTED =
                "{\\n " +
                        "\\\"id\\\": 1,\\n \\\"city\\\": \\\"Amsterdam\\\",\\n " +
                        "\\\"tags\\\": [{\\\"live\\\": \\\"false\\\", \\\"demo\\\": \\\"yes\\\"}]" +
                        "\\n}";

        LinkedHashMap<String, Object> city = new LinkedHashMap<>();
        city.put("id", 1);
        city.put("city", "Amsterdam");
        ArrayList<String> tags = new ArrayList<>();
        tags.add("{\"live\": \"false\", \"demo\": \"yes\"}");
        city.put("tags", tags);

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(city));

    }

    @Test
    public void convertNestedArrayObjectListToJson() {

        final String EXPECTED =
                "{\\n " +
                        "\\\"id\\\": 1,\\n " +
                        "\\\"lineItems\\\": [" +
                        "{\\n \\\"quantity\\\": 10,\\n \\\"description\\\": \\\"item1\\\"\\n}, " +
                        "{\\n \\\"quantity\\\": 100,\\n \\\"description\\\": \\\"item2\\\"\\n}, " +
                        "{\\n \\\"quantity\\\": 20,\\n \\\"description\\\": \\\"item3\\\"\\n}" +
                        "]" +
                "\\n}";

        LinkedHashMap<String, Object> order = new LinkedHashMap<>();
        order.put("id", 1);
        ArrayList<LinkedHashMap> lineItems = new ArrayList<>();
        lineItems.add(new LinkedHashMap<String, Object>() {{
            put("quantity", 10);
            put("description", "item1");
        }});
        lineItems.add(new LinkedHashMap<String, Object>() {{
            put("quantity", 100);
            put("description", "item2");
        }});
        lineItems.add(new LinkedHashMap<String, Object>() {{
            put("quantity", 20);
            put("description", "item3");
        }});

        order.put("lineItems", lineItems);

        assertEquals(EXPECTED, new ExampleJsonHelper().convertToJson(order));

    }

    class LineItem {
        Integer quantity;
        String description;

        LineItem(Integer quantity, String description) {
            this.quantity = quantity;
            this.description = description;
        }
    }



    @Test
    public void formatString() {
        final String EXPECTED = "{\\\\\\\"live\\\\\\\": \\\\\\\"false\\\\\\\", \\\\\\\"demo\\\\\\\": \\\\\\\"yes\\\\\\\"}";

        String json = "{\"live\": \"false\", \"demo\": \"yes\"}";

        assertEquals(EXPECTED, new ExampleJsonHelper().formatString(json));
    }


}
