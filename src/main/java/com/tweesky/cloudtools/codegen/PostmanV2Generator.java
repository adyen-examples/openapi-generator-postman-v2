package com.tweesky.cloudtools.codegen;

import com.tweesky.cloudtools.codegen.model.PostmanVariable;
import io.swagger.v3.oas.models.examples.Example;
import org.openapitools.codegen.*;
import org.openapitools.codegen.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.io.File;
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

  Set<PostmanVariable> variables = new HashSet<>();

  protected Map<String, Object> additionalProperties = new HashMap<>();

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

  @Override
  public void postProcessParameter(CodegenParameter parameter) {
    if(parameter.isPathParam) {
      variables.add(new PostmanVariable()
              .addName(parameter.paramName)
              .addType(parameter.dataType)
              .addExample(parameter.example));
    }
  }

  @Override
  public void processOpts() {
    super.processOpts();
    super.vendorExtensions().put("variables", variables);
  }
  /**
   * Provides an opportunity to inspect and modify operation data before the code is generated.
   */
  @Override
  public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
    OperationsMap results = super.postProcessOperationsWithModels(objs, allModels);

    OperationMap ops = results.getOperations();
    List<CodegenOperation> opList = ops.getOperation();


    // iterate over the operations to customise operations
    for(CodegenOperation co : opList) {
      co.path = doubleCurlyBraces(co.path);

      // request headers
      if(co.produces != null && co.produces.get(0) != null) {
        // produces mediaType as `Accept` header (use first mediaType only)
        String mediaType = co.produces.get(0).get("mediaType");
        CodegenParameter acceptHeader = new CodegenParameter();
        acceptHeader.paramName = "Accept";
        CodegenProperty schema = new CodegenProperty();
        schema.defaultValue = mediaType;
        acceptHeader.setSchema(schema);
        co.headerParams.add(0, acceptHeader);
      }

      if(co.consumes != null && co.consumes.get(0) != null) {
        // consumes mediaType as `Content-Type` header (use first mediaType only)
        String mediaType = co.consumes.get(0).get("mediaType");
        CodegenParameter contentTypeHeader = new CodegenParameter();
        contentTypeHeader.paramName = "Content-Type";
        CodegenProperty schema = new CodegenProperty();
        schema.defaultValue = mediaType;
        contentTypeHeader.setSchema(schema);
        co.headerParams.add(0, contentTypeHeader);
      }

      // build pathSegments
      String[] pathSegments = co.path.substring(1).split("/");
      co.vendorExtensions.put("pathSegments", pathSegments);
      co.responses.stream().forEach(r -> r.vendorExtensions.put("pathSegments", pathSegments));

      String requestBody = getRequestBody(co);
      if(requestBody != null) {
        co.vendorExtensions.put("requestBody", getRequestBody(co));
        co.vendorExtensions.put("hasRequestBody", true);
      } else {
        co.vendorExtensions.put("hasRequestBody", false);
      }

      for(CodegenResponse codegenResponse : co.responses) {
        Object responseBody = getResponseBody(codegenResponse);

        if(responseBody != null) {
          codegenResponse.vendorExtensions.put("responseBody", responseBody);
          codegenResponse.vendorExtensions.put("hasResponseBody", true);
        } else {
          codegenResponse.vendorExtensions.put("hasResponseBody", false);
        }
      }
    }

    return results;
  }

  Object getResponseBody(CodegenResponse codegenResponse) {
    Object responseBody = null;

    if(codegenResponse.getContent() != null) {
      Map<String, Example> maxExamples = codegenResponse.getContent().get("application/json").getExamples();
      if(maxExamples != null && maxExamples.values().iterator().hasNext()) {
        responseBody = maxExamples.values().iterator().next().getValue();
      }
    }

    return responseBody;
  }

  String getRequestBody(CodegenOperation codegenOperation) {
    String requestBody = null;

    if(codegenOperation.getHasBodyParam()) {
      if(codegenOperation.bodyParam.example != null) {
        requestBody = codegenOperation.bodyParam.example;
      } else if(codegenOperation.bodyParam.getSchema() != null) {
        requestBody = codegenOperation.bodyParam.getSchema().getExample();
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
    return "Generates a postman-v2 client library.";
  }

  public PostmanV2Generator() {
    super();

    // set the output folder here
    outputFolder = "generated-code/postman-v2";

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

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    supportingFiles.add(
            new SupportingFile("postman.mustache", "", "postman.json")
    );

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
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
  }

  /**
   * Location to write api files.  You can use the apiPackage() as defined when the class is
   * instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
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
    return str.replace("{", "{{").replace("}", "}}");

  }

}
