package com.tweesky.cloudtools.codegen;

import com.tweesky.cloudtools.codegen.model.PostmanVariable;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.servers.ServerVariable;
import org.openapitools.codegen.*;
import org.openapitools.codegen.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenAPI generator for Postman format v2.1
 */
public class PostmanV2Generator extends DefaultCodegen implements CodegenConfig {

  private final Logger LOGGER = LoggerFactory.getLogger(PostmanV2Generator.class);

  // source folder where to write the files
  protected String sourceFolder = "src";
  protected String apiVersion = "1.0.0";
  // Select whether to create folders according to the spec’s paths or tags. Values: Paths | Tags
  public static final String FOLDER_STRATEGY = "folderStrategy";
  public static final String FOLDER_STRATEGY_DEFAULT_VALUE = "Paths";
  public static final String PATH_PARAMS_AS_VARIABLES = "pathParamsAsVariables";
  public static final Boolean PATH_PARAMS_AS_VARIABLES_DEFAULT_VALUE = true;
  public static final String POSTMAN_FILE = "postmanFile";
  public static final String POSTMAN_FILE_DEFAULT_VALUE = "postman.json";
  public static final String NAMING_REQUESTS = "namingRequests";
  public static final String NAMING_REQUESTS_DEFAULT_VALUE = "Fallback";

  public static final String REQUEST_PARAMETER_GENERATION = "requestParameterGeneration";
  public static final String REQUEST_PARAMETER_GENERATION_DEFAULT_VALUE = "Example";

  protected String folderStrategy = FOLDER_STRATEGY_DEFAULT_VALUE; // values: Paths | Tags
  protected Boolean pathParamsAsVariables = PATH_PARAMS_AS_VARIABLES_DEFAULT_VALUE; // values: true | false

  protected String postmanFile = POSTMAN_FILE_DEFAULT_VALUE;
  protected String namingRequests= NAMING_REQUESTS_DEFAULT_VALUE; // values: Feedback | URL
  protected String requestParameterGeneration = REQUEST_PARAMETER_GENERATION_DEFAULT_VALUE; // values: Example, Schema

  Set<PostmanVariable> variables = new HashSet<>();

  // operations grouped by tag
  protected Map<String, List<CodegenOperation>> codegenOperationsByTag = new HashMap<>();
  // list of operations
  protected List<CodegenOperation> codegenOperationsList = new ArrayList<>();

  /**
   * Configures the type of generator.
   *
   * @return  the CodegenType for this generator
   * @see     org.openapitools.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.OTHER;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -g flag.
   *
   * @return the friendly name for the generator
   */
  public String getName() {
    return "postman-v2";
  }

  public PostmanV2Generator() {
    super();

    cliOptions.add(CliOption.newString(FOLDER_STRATEGY, "whether to create folders according to the spec’s paths or tags"));
    cliOptions.add(CliOption.newBoolean(PATH_PARAMS_AS_VARIABLES, "whether to create Postman variables for path parameters"));
    cliOptions.add(CliOption.newString(POSTMAN_FILE, "name of the generated Postman file"));
    cliOptions.add(CliOption.newString(NAMING_REQUESTS, "how the requests inside the generated collection will be named (Fallback or URL)"));
    cliOptions.add(CliOption.newString(REQUEST_PARAMETER_GENERATION, "whether to generate the request parameters based on the schema or the examples"));

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "postman-v2";

    /**
     * Api Package.  Optional, if needed, this can be used in templates
     */
    apiPackage = "org.openapitools.api";

    /**
     * Model Package.  Optional, if needed, this can be used in templates
     */
    modelPackage = "org.openapitools.model";

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("apiVersion", apiVersion);

  }

  @Override
  public void postProcessParameter(CodegenParameter parameter) {
    if(pathParamsAsVariables && parameter.isPathParam) {
      variables.add(new PostmanVariable()
              .addName(parameter.paramName)
              .addType(parameter.dataType)
              .addExample(parameter.example));
    }
  }

  @Override
  public List<CodegenServerVariable> fromServerVariables(Map<String, ServerVariable> variables) {

    if(variables != null){
      variables.entrySet().stream().forEach(serverVariableEntry -> this.variables.add(new PostmanVariable()
              .addName(serverVariableEntry.getKey())
              .addType("string")
              .addExample(serverVariableEntry.getValue().getDefault())));
    }

    return super.fromServerVariables(variables);
  }

  @Override
  public void processOpts() {
    super.processOpts();

    if(additionalProperties().containsKey(FOLDER_STRATEGY)) {
      folderStrategy = additionalProperties().get(FOLDER_STRATEGY).toString();
    }

    if (additionalProperties.containsKey(PATH_PARAMS_AS_VARIABLES)) {
      pathParamsAsVariables = Boolean.parseBoolean(additionalProperties.get(PATH_PARAMS_AS_VARIABLES).toString());
    }

    if(additionalProperties.containsKey(POSTMAN_FILE)) {
      postmanFile = additionalProperties.get(POSTMAN_FILE).toString();
    }

    if(additionalProperties.containsKey(NAMING_REQUESTS)) {
      namingRequests = additionalProperties().get(NAMING_REQUESTS).toString();
    }

    if(additionalProperties().containsKey(REQUEST_PARAMETER_GENERATION)) {
      requestParameterGeneration = additionalProperties().get(REQUEST_PARAMETER_GENERATION).toString();
    }

    supportingFiles.add(
            new SupportingFile("postman.mustache", "", postmanFile)
    );

    super.vendorExtensions().put("variables", variables);

    if(folderStrategy.equalsIgnoreCase("tags")) {
      this.additionalProperties().put("codegenOperationsByTag", codegenOperationsByTag);
    } else {
      this.additionalProperties().put("codegenOperationsList", codegenOperationsList);
    }

  }
  /**
   * Process and modify operations before generating code
   */
  @Override
  public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
    OperationsMap results = super.postProcessOperationsWithModels(objs, allModels);

    OperationMap ops = results.getOperations();
    List<CodegenOperation> opList = ops.getOperation();

    for(CodegenOperation codegenOperation : opList) {

      if(pathParamsAsVariables) {
        codegenOperation.path = doubleCurlyBraces(codegenOperation.path);
      }

      if(namingRequests.equalsIgnoreCase("url")) {
        codegenOperation.summary = codegenOperation.path;
      } else {
        if(codegenOperation.summary == null) {
          if(codegenOperation.operationId == null) {
            codegenOperation.summary = codegenOperation.path;
          } else {
            codegenOperation.summary = codegenOperation.operationId;
          }
        }
      }

      // request headers
      if(codegenOperation.produces != null && codegenOperation.produces.get(0) != null) {
        // produces mediaType as `Accept` header (use first mediaType only)
        String mediaType = codegenOperation.produces.get(0).get("mediaType");
        CodegenParameter acceptHeader = new CodegenParameter();
        acceptHeader.paramName = "Accept";
        CodegenProperty schema = new CodegenProperty();
        schema.defaultValue = mediaType;
        acceptHeader.setSchema(schema);
        codegenOperation.headerParams.add(0, acceptHeader);
      }

      if(codegenOperation.consumes != null && codegenOperation.consumes.get(0) != null) {
        // consumes mediaType as `Content-Type` header (use first mediaType only)
        String mediaType = codegenOperation.consumes.get(0).get("mediaType");
        CodegenParameter contentTypeHeader = new CodegenParameter();
        contentTypeHeader.paramName = "Content-Type";
        CodegenProperty schema = new CodegenProperty();
        schema.defaultValue = mediaType;
        contentTypeHeader.setSchema(schema);
        codegenOperation.headerParams.add(0, contentTypeHeader);
      }

      // build pathSegments
      String[] pathSegments = codegenOperation.path.substring(1).split("/");
      codegenOperation.vendorExtensions.put("pathSegments", pathSegments);
      codegenOperation.responses.stream().forEach(r -> r.vendorExtensions.put("pathSegments", pathSegments));

      String requestBody = getRequestBody(codegenOperation);
      if(requestBody != null) {
        codegenOperation.vendorExtensions.put("requestBody", requestBody);
        codegenOperation.vendorExtensions.put("hasRequestBody", true);
      } else {
        codegenOperation.vendorExtensions.put("hasRequestBody", false);
      }

      for(CodegenResponse codegenResponse : codegenOperation.responses) {
        String responseBody = getResponseBody(codegenResponse);
        if(responseBody != null) {
          codegenResponse.vendorExtensions.put("responseBody", responseBody);
          codegenResponse.vendorExtensions.put("hasResponseBody", true);
        } else {
          codegenResponse.vendorExtensions.put("hasResponseBody", false);
        }
      }

      if(folderStrategy.equalsIgnoreCase("tags")) {
        addToMap(codegenOperation);
      } else {
        addToList(codegenOperation);
      }

    }

    return results;
  }

  void addToMap(CodegenOperation codegenOperation){

    String key = null;
    if(codegenOperation.tags == null || codegenOperation.tags.isEmpty()) {
      key = "default";
    } else {
      key = codegenOperation.tags.get(0).getName();
    }

    List<CodegenOperation> list = codegenOperationsByTag.get(key);

    if(list == null) {
      list = new ArrayList<>();
    }
    list.add(codegenOperation);

    codegenOperationsByTag.put(key, list);

  }

  void addToList(CodegenOperation codegenOperation) {
    codegenOperationsList.add(codegenOperation);
  }

  String getResponseBody(CodegenResponse codegenResponse) {
    String responseBody = "";

    if(codegenResponse.getContent() != null && codegenResponse.getContent().get("application/json") != null &&
            codegenResponse.getContent().get("application/json").getExamples() != null) {
      // find in components/examples
      String exampleRef = codegenResponse.getContent().get("application/json").getExamples()
              .values().iterator().next().get$ref();
      if(exampleRef != null) {
        Example example = this.openAPI.getComponents().getExamples().get(extractExampleByName(exampleRef));
        responseBody = new ExampleJsonHelper().getJsonFromExample(example);
      }
    } else if(codegenResponse.getContent() != null) {
      // find in context examples
      Map<String, Example> maxExamples = codegenResponse.getContent().get("application/json").getExamples();
      if(maxExamples != null && maxExamples.values().iterator().hasNext()) {
        responseBody = new ExampleJsonHelper().getJsonFromExample(maxExamples.values().iterator().next());
      }
    }

    return responseBody;
  }

  String getRequestBody(CodegenOperation codegenOperation) {
    String requestBody = null;

    if(codegenOperation.getHasBodyParam()) {
      if (requestParameterGeneration.equalsIgnoreCase("Schema")) {
        // get from schema
        requestBody = new ExampleJsonHelper().getJsonFromSchema(codegenOperation.bodyParam);
      } else {
        // get from examples
        if (codegenOperation.bodyParam.example != null) {
          // find in bodyParam example
          requestBody = new ExampleJsonHelper().formatJson(codegenOperation.bodyParam.example);
        } else if (codegenOperation.bodyParam.getContent().get("application/json") != null &&
                codegenOperation.bodyParam.getContent().get("application/json").getExamples() != null) {
          // find in components/examples
          String exampleRef = codegenOperation.bodyParam.getContent().get("application/json").getExamples()
                  .values().iterator().next().get$ref();
          Example example = this.openAPI.getComponents().getExamples().get(extractExampleByName(exampleRef));
          requestBody = new ExampleJsonHelper().getJsonFromExample(example);
        } else if (codegenOperation.bodyParam.getSchema() != null) {
          // find in schema example
          requestBody = new ExampleJsonHelper().formatJson(codegenOperation.bodyParam.getSchema().getExample());
        }
      }
    }

    return requestBody;
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   *
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a postman-v2 file";
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reserved words
   *
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;  // add an underscore to the name
  }

  /**
   * override with any special text escaping logic to handle unsafe
   * characters so as to avoid code injection
   *
   * @param input String to be cleaned up
   * @return string with unsafe characters removed or escaped
   */
  @Override
  public String escapeUnsafeCharacters(String input) {
    //TODO: check that this logic is safe to escape unsafe characters to avoid code injection
    return input;
  }

  /**
   * Escape single and/or double quote to avoid code injection
   *
   * @param input String to be cleaned up
   * @return string with quotation mark removed or escaped
   */
  public String escapeQuotationMark(String input) {
    //TODO: check that this logic is safe to escape quotation mark to avoid code injection
    return input.replace("\"", "\\\"");
  }

  // extract template variable from string (ie /api/{var})
  Set<String> extractVariables(String str) {
    Set<String> variables = new HashSet<>();

    Pattern p = Pattern.compile("\\{(.*?)\\}");
    Matcher m = p.matcher(str);

    while(m.find()) {
      variables.add(m.group(1));
    }

    return variables;
  }

  String doubleCurlyBraces(String str) {

    // remove doublebraces first
    String s = str.replace("{{", "{").replace("}}", "}");
    // change all singlebraces to doublebraces
    s = s.replace("{", "{{").replace("}", "}}");

    return s;

  }

  String extractExampleByName(String ref) {
    return ref.substring(ref.lastIndexOf("/") + 1);
  }

}
