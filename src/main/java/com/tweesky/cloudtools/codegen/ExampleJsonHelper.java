package com.tweesky.cloudtools.codegen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.examples.Example;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.tweesky.cloudtools.codegen.PostmanV2Generator.JSON_ESCAPE_DOUBLE_QUOTE;
import static com.tweesky.cloudtools.codegen.PostmanV2Generator.JSON_ESCAPE_NEW_LINE;

/**
 * Extract and format JSON examples
 */
public class ExampleJsonHelper {

    private final Logger LOGGER = LoggerFactory.getLogger(ExampleJsonHelper.class);

    // generate JSON (string) escaping and formatting
    String getJsonFromSchema(CodegenParameter codegenParameter) {

        String ret = "{" + JSON_ESCAPE_NEW_LINE + " ";

        int numVars = codegenParameter.vars.size();
        int counter = 1;

        for (CodegenProperty codegenProperty : codegenParameter.vars) {
            ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + codegenProperty.baseName + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                    JSON_ESCAPE_DOUBLE_QUOTE + "<" + getPostmanType(codegenProperty) + ">" + JSON_ESCAPE_DOUBLE_QUOTE;

            if(counter < numVars) {
                // add comma unless last attribute
                ret = ret + "," + JSON_ESCAPE_NEW_LINE + " ";
            }
            counter++;

        }

        ret = ret + JSON_ESCAPE_NEW_LINE + "}";

        return ret;
    }

    String getJsonFromExample(Example example) {
        String ret = "";

        if(example == null) {
            return ret;
        }

        if(example.getValue() instanceof ObjectNode) {
            ret = convertToJson((ObjectNode)example.getValue());
        } else if(example.getValue() instanceof LinkedHashMap) {
            ret = convertToJson((LinkedHashMap)example.getValue());
        }

        return ret;
    }

    public String formatJson(String json) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // convert to JSON object and prettify
            JsonNode actualObj = objectMapper.readTree(json);
            json = Json.pretty(actualObj);
            json = json.replace("\"", JSON_ESCAPE_DOUBLE_QUOTE);
            json = json.replace("\n", JSON_ESCAPE_NEW_LINE);

        } catch (JsonProcessingException e) {
            LOGGER.warn("Error formatting JSON", e);
            json = "";
        }

        return json;
    }

    // array of attributes from JSON payload (ignore commas within quotes)
    String[] getAttributes(String json) {
        return json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    String convertToJson(ObjectNode objectNode) {
        return formatJson(objectNode.toString());
    }

    // convert to JSON (string) escaping and formatting
    String convertToJson(LinkedHashMap<String, Object> linkedHashMap) {
        String ret = "";

        return traverseMap(linkedHashMap, ret);
    }

    // traverse recursively
    private String traverseMap(LinkedHashMap<String, Object> linkedHashMap, String ret) {

        ret = ret + "{" + JSON_ESCAPE_NEW_LINE + " ";

        int numVars = linkedHashMap.entrySet().size();
        int counter = 1;

        for (Map.Entry<String, Object> mapElement : linkedHashMap.entrySet()) {
            String key = mapElement.getKey();
            Object value = mapElement.getValue();

            if(value instanceof String) {
                // unescape double quotes already escaped
                value = ((String)value).replace("\\\"", "\"");

                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        JSON_ESCAPE_DOUBLE_QUOTE + formatString((String) value) + JSON_ESCAPE_DOUBLE_QUOTE;
            } else if (value instanceof Boolean) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        value;
            } else if (value instanceof Integer) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " +
                        value;
            } else if (value instanceof LinkedHashMap) {
                String in = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": ";
                ret = traverseMap(((LinkedHashMap<String, Object>) value),  in);
            } else if (value instanceof ArrayList<?>) {
                ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + key + JSON_ESCAPE_DOUBLE_QUOTE + ": " + getJsonArray((ArrayList<Object>) value);
            } else {
                LOGGER.warn("Value type unrecognised: " + value.getClass());
            }

            if(counter < numVars) {
                // add comma unless last attribute
                ret = ret + "," + JSON_ESCAPE_NEW_LINE + " ";
            }
            counter++;
        }

        ret = ret + JSON_ESCAPE_NEW_LINE + "}";

        return ret;
    }

    String formatString(String str) {
        String ret = "";

        if(str.startsWith("{")) {
            // isJson (escape double quotes in json: "live" to \\\"live\\\")
            ret = str.replace("\"", "\\\\\\\"");
        } else {
            ret = str;
        }

        return ret;
    }

    String getJsonArray(ArrayList<Object> list) {
        String ret = "";

        for(Object element: list) {
            if(element instanceof String) {
                ret = ret + getStringArrayElement((String) element) + ", ";
            } else if(element instanceof LinkedHashMap) {
                ret = traverseMap((LinkedHashMap<String, Object>) element, ret);
            }
        }

        if(ret.endsWith(", ")) {
            ret = ret.substring(0, ret.length() - 2);
        }

        return "[" + ret + "]";
    }

    String getStringArrayElement(String element) {
        String ret = "";

        if(element.startsWith("{")) {
            // isJson (escape all double quotes)
            ret = ret + element.replace("\"", JSON_ESCAPE_DOUBLE_QUOTE);
        } else {
            // string element (add escaped double quotes)
            ret = ret + JSON_ESCAPE_DOUBLE_QUOTE + element + JSON_ESCAPE_DOUBLE_QUOTE;
        }

        return ret;
    }

    String getPostmanType(CodegenProperty codegenProperty) {
        if(codegenProperty.isNumeric) {
            return "number";
        } else if(codegenProperty.isDate) {
            return "date";
        } else {
            return "string";
        }
    }


}
