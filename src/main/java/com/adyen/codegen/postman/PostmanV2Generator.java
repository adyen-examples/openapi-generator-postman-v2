package com.adyen.codegen.postman;

import com.adyen.codegen.postman.model.PostmanRequestItem;
import com.adyen.codegen.postman.model.PostmanResponse;
import com.adyen.codegen.postman.model.PostmanVariable;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.ServerVariable;
import org.openapitools.codegen.*;
import org.openapitools.codegen.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OpenAPI generator for Postman format v2.1
 */
public class PostmanV2Generator extends DefaultCodegen implements CodegenConfig {

  protected String apiVersion = "1.0.0";
  // Select whether to create folders according to the spec’s paths or tags. Values: Paths | Tags
  public static final String FOLDER_STRATEGY = "folderStrategy";
  public static final String FOLDER_STRATEGY_DEFAULT_VALUE   = "Tags";
  // Select whether to create Postman variables for path templates
  public static final String PATH_PARAMS_AS_VARIABLES = "pathParamsAsVariables";
  public static final Boolean PATH_PARAMS_AS_VARIABLES_DEFAULT_VALUE = false;
  
  public static final String POSTMAN_FILE_DEFAULT_VALUE = "postman.json";

  // user-defined variables
  public static final String POSTMAN_VARIABLES = "postmanVariables";
  // auto-generated values ie {{$guid}}
  public static final String GENERATED_VARIABLES = "generatedVariables";

  public static final String REQUEST_PARAMETER_GENERATION = "requestParameterGeneration";
  public static final String REQUEST_PARAMETER_GENERATION_DEFAULT_VALUE = "Example";

  protected String folderStrategy = FOLDER_STRATEGY_DEFAULT_VALUE; // values: Paths | Tags
  protected Boolean pathParamsAsVariables = PATH_PARAMS_AS_VARIABLES_DEFAULT_VALUE; // values: true | false

  // Output file
  protected String postmanFile = POSTMAN_FILE_DEFAULT_VALUE;

  // Select whether to generate requests/responses from Example or Schema
  protected String requestParameterGeneration = REQUEST_PARAMETER_GENERATION_DEFAULT_VALUE; // values: Example, Schema

  Set<PostmanVariable> variables = new HashSet<>();
  String[] postmanVariableNames = null;
  String[] generatedVariableNames = null;

  public static final String JSON_ESCAPE_DOUBLE_QUOTE = "\\\"";
  public static final String JSON_ESCAPE_NEW_LINE = "\\n";


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
    cliOptions.add(CliOption.newString(POSTMAN_VARIABLES, "list of Postman variables to create"));
    cliOptions.add(CliOption.newString(GENERATED_VARIABLES, "list of auto-generated variables"));
    cliOptions.add(CliOption.newString(REQUEST_PARAMETER_GENERATION, "whether to generate the request parameters based on the schema or the examples"));

    /*
      Template Location.  This is the location which templates will be read from.  The generator
      will use the resource stream to attempt to read the templates.
     */
    templateDir = "postman-v2";

    /*
     * Api Package.  Optional, if needed, this can be used in templates
     */
    apiPackage = "org.openapitools.api";

    /*
     * Model Package.  Optional, if needed, this can be used in templates
     */
    modelPackage = "org.openapitools.model";

    /*
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
              .addType(mapToPostmanType(parameter.dataType))
              .addDefaultValue(parameter.defaultValue));
    }
  }

  @Override
  public void preprocessOpenAPI(OpenAPI openAPI) {
    super.preprocessOpenAPI(openAPI);
    this.additionalProperties().put("formattedDescription", formatDescription(openAPI.getInfo().getDescription()));
  }

  @Override
  public List<CodegenServerVariable> fromServerVariables(Map<String, ServerVariable> variables) {

    if(variables != null){
      variables.forEach((key, value) -> this.variables.add(new PostmanVariable()
              .addName(key)
              .addType("string")
              .addDefaultValue(value.getDefault())));
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

    if(additionalProperties().containsKey(REQUEST_PARAMETER_GENERATION)) {
      requestParameterGeneration = additionalProperties().get(REQUEST_PARAMETER_GENERATION).toString();
    }

    if(additionalProperties().containsKey(POSTMAN_VARIABLES)) {
      extractPostmanVariableNames(additionalProperties().get(POSTMAN_VARIABLES).toString());
    }

    if(additionalProperties().containsKey(GENERATED_VARIABLES)) {
      extractGeneratedVariableNames(additionalProperties().get(GENERATED_VARIABLES).toString());
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

      // use Postman notation for path parameter
      codegenOperation.path = replacesBracesInPath(codegenOperation.path);

      if(pathParamsAsVariables) {
        // set value of path parameter with corresponding env variable
        for(CodegenParameter codegenParameter : codegenOperation.pathParams) {
          codegenParameter.defaultValue = "{{" + codegenParameter.paramName + "}}";
        }
      }

      // ad-hoc customisation for specific path parameters:
      // companyId: set value as YOUR_COMPANY_ACCOUNT env variable
      // merchantId: set value as YOUR_MERCHANT_ACCOUNT env variable
      if(codegenOperation.path.contains(":companyId") || codegenOperation.path.contains(":merchantId")) {
        for(CodegenParameter codegenParameter : codegenOperation.pathParams) {
          if(codegenParameter.paramName.equalsIgnoreCase("companyId")) {
            // set default value for `companyId` path parameter
            codegenParameter.defaultValue = "{{YOUR_COMPANY_ACCOUNT}}";
          }
          if(codegenParameter.paramName.equalsIgnoreCase("merchantId")) {
            // set default value for `merchantId` path parameter
            codegenParameter.defaultValue = "{{YOUR_MERCHANT_ACCOUNT}}";
          }
        }
      }

      codegenOperation.summary = getSummary(codegenOperation);

      // request headers
      if(codegenOperation.produces != null && codegenOperation.produces.get(0) != null) {
        // produces mediaType as `Accept` header (use first mediaType only)
        String mediaType = codegenOperation.produces.get(0).get("mediaType");
        CodegenParameter acceptHeader = new CodegenParameter();
        acceptHeader.baseName = "Accept";
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
        contentTypeHeader.baseName = "Content-Type";
        contentTypeHeader.paramName = "Content-Type";
        CodegenProperty schema = new CodegenProperty();
        schema.defaultValue = mediaType;
        contentTypeHeader.setSchema(schema);
        codegenOperation.headerParams.add(0, contentTypeHeader);
      }

      // build pathSegments
      String[] pathSegments = codegenOperation.path.substring(1).split("/");
      codegenOperation.vendorExtensions.put("pathSegments", pathSegments);
      codegenOperation.responses.forEach(r -> r.vendorExtensions.put("pathSegments", pathSegments));

      List<PostmanRequestItem> postmanRequests = getPostmanRequests(codegenOperation);
      if(postmanRequests != null) {
        if(isCreatePostmanVariables()) {
          postmanRequests = createPostmanVariables(postmanRequests);
        }
        if(isGeneratedVariables()) {
          postmanRequests = createGeneratedVariables(postmanRequests);
        }
        codegenOperation.vendorExtensions.put("postmanRequests", postmanRequests);
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

    String key;
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

    // sort requests by path
    list.sort(Comparator.comparing(obj -> obj.path));

  }

  void addToList(CodegenOperation codegenOperation) {
    codegenOperationsList.add(codegenOperation);

    // sort requests by path
    codegenOperationsList.sort(Comparator.comparing(obj -> obj.path));
  }

  List<PostmanResponse> getResponseExamples(CodegenResponse codegenResponse, String message) {
    List<PostmanResponse> postmanResponses = new ArrayList<>();

    if (codegenResponse.getContent() != null && codegenResponse.getContent().get("application/json") != null &&
            codegenResponse.getContent().get("application/json").getExamples() != null) {

      var examples = codegenResponse.getContent().get("application/json").getExamples();
      for (Map.Entry<String, Example> entry : examples.entrySet()) {
        String key = entry.getKey();
        String ref = entry.getValue().get$ref();

        String response;
        if (ref != null) {
          // get example by $ref
          Example example = this.openAPI.getComponents().getExamples().get(extractExampleByName(ref));
          response = new ExampleJsonHelper().getJsonFromExample(example);
        } else {
          // get inline example
          response = new ExampleJsonHelper().getJsonFromExample(entry.getValue());
        }
        postmanResponses.add(new PostmanResponse(key, codegenResponse, message, response));
      }

    } else if (codegenResponse.getContent() != null) {
      // TODO : Implement
    }

    return postmanResponses;
  }

  // from OpenAPI operation to n Postman requests
  List<PostmanRequestItem> getPostmanRequests(CodegenOperation codegenOperation) {
    // Note : For all PostmanRequestItem who get created without an id parameter, no responses will be available

    List<PostmanRequestItem> items = new ArrayList<>();

    if (codegenOperation.getHasBodyParam()) {
      // operation with bodyParam
      if (requestParameterGeneration.equalsIgnoreCase("Schema")) {
        // get from schema
        items.add(new PostmanRequestItem(codegenOperation.summary, new ExampleJsonHelper().getJsonFromSchema(codegenOperation.bodyParam),
                codegenOperation.httpMethod));
      } else {
        // get from examples
        if (codegenOperation.bodyParam.getContent().get("application/json") != null &&
                codegenOperation.bodyParam.getContent().get("application/json").getExamples() != null) {
          // find in components/examples
          for (Map.Entry<String, Example> entry : codegenOperation.bodyParam.getContent().get("application/json").getExamples().entrySet()) {
            String exampleRef = entry.getValue().get$ref();

            String exampleAsString;
            String exampleName;
            if (exampleRef != null) {
              // get example by $ref
              Example example = this.openAPI.getComponents().getExamples().get(extractExampleByName(exampleRef));
              exampleAsString = new ExampleJsonHelper().getJsonFromExample(example);
              exampleName = example.getSummary();
            } else {
              // get inline example
              exampleAsString = new ExampleJsonHelper().getJsonFromExample(entry.getValue());
              exampleName = entry.getValue().getSummary();
            }
            items.add(new PostmanRequestItem(exampleName, exampleAsString, entry.getKey(), codegenOperation.httpMethod));
          }
        } else if (codegenOperation.bodyParam.example != null) {
          // find in bodyParam example
          items.add(new PostmanRequestItem(codegenOperation.summary, new ExampleJsonHelper().formatJson(codegenOperation.bodyParam.example),
                  codegenOperation.httpMethod));
        } else if (codegenOperation.bodyParam.getSchema() != null) {
          // find in schema example
          String exampleAsString = new ExampleJsonHelper().formatJson(codegenOperation.bodyParam.getSchema().getExample());
          items.add(new PostmanRequestItem(codegenOperation.summary, exampleAsString, codegenOperation.httpMethod));
        } else {
          // example not found
          // get from schema
          items.add(new PostmanRequestItem(codegenOperation.summary, new ExampleJsonHelper().getJsonFromSchema(codegenOperation.bodyParam), codegenOperation.httpMethod));

        }
      }
    } else {
      // operation without bodyParam
      PostmanRequestItem postmanRequestItem = new PostmanRequestItem(codegenOperation.summary, "", codegenOperation.httpMethod);
      items.add(postmanRequestItem);
    }

    // Grabbing responses
    List<CodegenResponse> responses = codegenOperation.responses;
    List<PostmanResponse> allPostmanResponses = new ArrayList<>();
    for (CodegenResponse response : responses) {
      List<PostmanResponse> postmanResponses = getResponseExamples(response, response.message);
      allPostmanResponses.addAll(postmanResponses);
    }

    // Adding responses to corresponding requests
    for (PostmanRequestItem item : items) {
      List<PostmanResponse> postmanResponses = allPostmanResponses.stream().filter(r -> Objects.equals(r.getId(), item.getId())).collect(Collectors.toList());
      if (!postmanResponses.isEmpty()) {
        postmanResponses.forEach(r -> r.setOriginalRequest(item));
        item.addResponses(postmanResponses);
      } else {
        // no matching response example
        if (item.getHttpMethod() != null && item.getHttpMethod().equals("GET")) {
          // in case of GET use first response example
          if (allPostmanResponses.size() > 0) {
            PostmanResponse postmanResponse = allPostmanResponses.stream()
                    .findFirst()
                    .orElse(null);

            if(postmanResponse != null) {
              postmanResponse.setOriginalRequest(item);
              item.addResponse(postmanResponse);
            }
          }
        }
      }
    }
    return items;
  }

  // split, trim
  void extractPostmanVariableNames(String postmanVariablesCsv) {
    postmanVariableNames = postmanVariablesCsv.split("-");
    Arrays.parallelSetAll(postmanVariableNames, (i) -> postmanVariableNames[i].trim());
  }
  // split, trim
  void extractGeneratedVariableNames(String generatedVariablesCsv) {
    generatedVariableNames = generatedVariablesCsv.split("-");
    Arrays.parallelSetAll(generatedVariableNames, (i) -> generatedVariableNames[i].trim());
  }
  boolean isGeneratedVariables() {
    return generatedVariableNames != null;
  }
  boolean isCreatePostmanVariables() {
    return postmanVariableNames != null;
  }
  List<PostmanRequestItem> createPostmanVariables(List<PostmanRequestItem> postmanRequests) {

    for (String var : postmanVariableNames) {
      for(PostmanRequestItem requestItem : postmanRequests) {
        if (requestItem.getBody().indexOf(var) > 0) {

          requestItem.setBody(requestItem.getBody().replace(var, "{{" + var + "}}"));

          variables.add(new PostmanVariable()
                  .addName(var)
                  .addType("string")
                  .addDefaultValue(""));
        }
      }
    }

    return postmanRequests;
  }

  List<PostmanRequestItem> createGeneratedVariables(List<PostmanRequestItem> postmanRequests) {

    for (String var : generatedVariableNames) {
      for(PostmanRequestItem requestItem : postmanRequests) {
        requestItem.setBody(requestItem.getBody().replace(var, "{{$guid}}"));
      }
    }

    return postmanRequests;
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
   * characters to avoid code injection
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
   * Return the default value of the property
   * <p>
   * Return null when the default is not defined
   *
   * @param schema Property schema
   * @return string presentation of the default value of the property
   */
  @SuppressWarnings("static-method")
  @Override
  public String toDefaultValue(Schema schema) {
    if (schema.getDefault() != null) {
      return schema.getDefault().toString();
    }

    return null;
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

  // convert path from /users/{id} to /users/:id
  String replacesBracesInPath(String path) {

    String s = path.replace("{", ":");
    s = s.replace("}", "");

    return s;

  }

  String extractExampleByName(String ref) {
    return ref.substring(ref.lastIndexOf("/") + 1);
  }

  String mapToPostmanType(String openApiDataType) {
    String ret = "any";  // default value

    if(openApiDataType.equalsIgnoreCase("string")) {
      ret = "string";
    } else if(openApiDataType.equalsIgnoreCase("number") ||
            openApiDataType.equalsIgnoreCase("integer")) {
      ret = "number";
    } else if(openApiDataType.equalsIgnoreCase("boolean")) {
      ret = "boolean";
    }

    return ret;
  }

  // make sure operation name is always set
  String getSummary(CodegenOperation codegenOperation) {
    String ret;

    if(codegenOperation.summary != null) {
      ret = codegenOperation.summary;
    } else if (codegenOperation.operationId != null) {
      ret = codegenOperation.operationId;
    } else {
      ret = codegenOperation.httpMethod;
    }
    return ret;
  }

  /**
   * Format text to include in JSON file
   * @param description potentially multiline input with special characters
   * @return formatted description
   */
  String formatDescription(String description) {

    description = description.replace("\n", JSON_ESCAPE_NEW_LINE);
    description = description.replace("\"", JSON_ESCAPE_DOUBLE_QUOTE);

    return description;
  }
}
