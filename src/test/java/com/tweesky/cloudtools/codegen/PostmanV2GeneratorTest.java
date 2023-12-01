package com.tweesky.cloudtools.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweesky.cloudtools.codegen.model.PostmanRequestItem;
import io.swagger.v3.oas.models.tags.Tag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostmanV2GeneratorTest {

  @Test
  public void testInitialConfigValues() throws Exception {
    final PostmanV2Generator postmanV2Generator = new PostmanV2Generator();
    postmanV2Generator.processOpts();

    Assert.assertEquals(postmanV2Generator.folderStrategy, "Tags");
    Assert.assertEquals(postmanV2Generator.postmanFile, "postman.json");

    Assert.assertNull(postmanV2Generator.additionalProperties().get("codegenOperationsList"));
    Assert.assertNotNull(postmanV2Generator.additionalProperties().get("codegenOperationsByTag"));
  }

  @Test
  public void testConfigWithFolderStrategyTags() throws Exception {
    final PostmanV2Generator postmanV2Generator = new PostmanV2Generator();

    postmanV2Generator.additionalProperties().put(postmanV2Generator.FOLDER_STRATEGY, "Tags");
    postmanV2Generator.processOpts();

    Assert.assertEquals(postmanV2Generator.folderStrategy, "Tags");

    Assert.assertNull(postmanV2Generator.additionalProperties().get("codegenOperationsList"));
    Assert.assertNotNull(postmanV2Generator.additionalProperties().get("codegenOperationsByTag"));
  }

  @Test
  public void testConfigWithCreationPostmanVariables() throws Exception {
    final PostmanV2Generator postmanV2Generator = new PostmanV2Generator();

    postmanV2Generator.additionalProperties().put(postmanV2Generator.POSTMAN_VARIABLES, "VAR1-VAR2-VAR3");
    postmanV2Generator.processOpts();

    Assert.assertTrue(postmanV2Generator.isCreatePostmanVariables());
    Assert.assertArrayEquals(postmanV2Generator.postmanVariableNames, new String[]{"VAR1", "VAR2", "VAR3"});
  }
  @Test
  public void testBasicGeneration() throws IOException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/Basic.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    TestUtils.assertFileContains(path, "\"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"");

    // verify request name (from summary)
    TestUtils.assertFileContains(path, "\"name\": \"Get User\"");
    // verify request endpoint
    TestUtils.assertFileContains(path, "\"name\": \"/users/:userId\"");

  }

  @Test
  public void testBasicGenerationJson() throws IOException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/BasicJson.json")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    TestUtils.assertFileContains(path, "\"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"");
  }

  @Test
  public void testValidatePostmanJson() throws IOException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    final ObjectMapper mapper = new ObjectMapper();
    mapper.readTree(new FileReader(output + "/postman.json"));

  }
  
  @Test
  public void testVariables() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .addAdditionalProperty(PostmanV2Generator.PATH_PARAMS_AS_VARIABLES, true)
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);

    JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(output + "/postman.json"));
    // verify json has variables
    assertTrue(jsonObject.get("variable") instanceof JSONArray);
    assertEquals(6, ((JSONArray) jsonObject.get("variable")).size());
    // verify param userId (without default value)
    TestUtils.assertFileContains(path,
            "key\": \"userId\", \"value\": \"\", \"type\": \"number\"");
    // verify param groupId (with default value)
    TestUtils.assertFileContains(path,
            "key\": \"groupId\", \"value\": \"1\", \"type\": \"number\"");

    // verify request endpoint
    TestUtils.assertFileContains(path, "\"name\": \"/users/:userId\"");

  }

  @Test
  public void testVariablesInRequestExample() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/BasicVariablesInExample.yaml")
            .addAdditionalProperty(PostmanV2Generator.POSTMAN_VARIABLES, "MY_VAR_NAME -MY_VAR_LAST_NAME ")
            .addAdditionalProperty(PostmanV2Generator.PATH_PARAMS_AS_VARIABLES, true)
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);

    JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(output + "/postman.json"));
    // verify json has variables
    assertTrue(jsonObject.get("variable") instanceof JSONArray);
    assertEquals(4, ((JSONArray) jsonObject.get("variable")).size());

    TestUtils.assertFileContains(path, "{{MY_VAR_NAME}}");

    // verify request endpoint
    TestUtils.assertFileContains(path, "\"name\": \"/users/:userId\"");

  }
  @Test
  public void testVariableThatDoesNotExist() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/BasicVariablesInExample.yaml")
            .addAdditionalProperty(PostmanV2Generator.POSTMAN_VARIABLES, "NOT_FOUND_VARIABLE")
            .addAdditionalProperty(PostmanV2Generator.PATH_PARAMS_AS_VARIABLES, true)
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);

    JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(output + "/postman.json"));
    // verify json has 2 variables only
    assertTrue(jsonObject.get("variable") instanceof JSONArray);
    assertEquals(2, ((JSONArray) jsonObject.get("variable")).size());

    TestUtils.assertFileNotContains(path, "{{NOT_FOUND_VAR}}");

  }
  @Test
  public void testGenerateWithoutPathParamsVariables() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(configurator.toClientOptInput()).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    TestUtils.assertFileExists(Paths.get(output + "/postman.json"));

    JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(output + "/postman.json"));
    // verify json has only Server variables (baseUrl, etc..)
    assertTrue(jsonObject.get("variable") instanceof JSONArray);
    assertEquals(4, ((JSONArray) jsonObject.get("variable")).size());

  }

  @Test
  public void testComponentExamples() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    // verify response body comes from components/examples
    TestUtils.assertFileContains(path, "\"name\": \"Example request for Get User\"");
    TestUtils.assertFileContains(path, "\"raw\": \"{\\n  \\\"id\\\": 777,\\n  \\\"firstName\\\": \\\"Alotta\\\",\\n  \\\"lastName\\\": \\\"Rotta\\\",\\n ");
  }

  @Test
  public void testNamingRequestsWithUrl() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .addAdditionalProperty(PostmanV2Generator.PATH_PARAMS_AS_VARIABLES, true)
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    // verify request name (from path)
    TestUtils.assertFileContains(path, "\"name\": \"/users/:userId\"");
  }

  @Test
  public void testExampleFromSchema() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .addAdditionalProperty(PostmanV2Generator.REQUEST_PARAMETER_GENERATION, "Schema")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(configurator.toClientOptInput()).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    // verify request name (from path)
    TestUtils.assertFileContains(path, "{\\n \\\"firstName\\\": \\\"<string>\\\",\\n \\\"lastName\\\": \\\"<string>\\\",\\n \\\"email\\\": \\\"<string>\\\",\\n \\\"dateOfBirth\\\": \\\"<date>\\\"\\n}");

  }

  @Test
  public void testSecuritySchemes() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    // check auth basic (1st security scheme in OpenAPI file)
    TestUtils.assertFileContains(path, "\"auth\": { \"type\": \"basic\", \"basic\": [");
    // check auth apiKey NOT found
    TestUtils.assertFileNotContains(path, "\"auth\": { \"type\": \"apikey\", \"apikey\": [");
  }

  @Test
  public void testHeaderParameters() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    TestUtils.assertFileContains(path, "{ \"key\": \"Content-Type\", \"value\": \"application/json\"");
    TestUtils.assertFileContains(path, "{ \"key\": \"Accept\", \"value\": \"application/json\"");
    // header without default value (disabled: true)
    TestUtils.assertFileContains(path, "{ \"key\": \"Custom-Header\", \"value\": \"\", \"disabled\": true");
    // header with default value (disabled: false)
    TestUtils.assertFileContains(path, "{ \"key\": \"Another-Custom-Header\", \"value\": \"abc\", \"disabled\": false");
  }

  @Test
  public void extractExampleByName() {
    String str = "#/components/examples/get-user-basic";

    assertEquals("get-user-basic", new PostmanV2Generator().extractExampleByName(str));
  }

  @Test
  public void processRequestExample() {
    String STR = "{\\n \\\"id\\\": 777,\\n \\\"firstName\\\": \\\"MY_VAR_1\\\",\\n \\\"MY_VAR_2\\\": \\\"Rotta\\\"\\n}";
    String EXPECTED = "{\\n \\\"id\\\": 777,\\n \\\"firstName\\\": \\\"{{MY_VAR_1}}\\\",\\n \\\"{{MY_VAR_2}}\\\": \\\"Rotta\\\"\\n}";;

    PostmanV2Generator postmanV2Generator = new PostmanV2Generator();
    postmanV2Generator.postmanVariableNames = new String[]{"MY_VAR_1", "MY_VAR_2"};

    List<PostmanRequestItem> requestItems = new ArrayList<>();
    requestItems.add(new PostmanRequestItem("get by id", STR));

    requestItems = postmanV2Generator.createPostmanVariables(requestItems);

    assertEquals(1, requestItems.size());
    assertEquals(EXPECTED, requestItems.get(0).getBody());
    assertEquals(2, postmanV2Generator.variables.size());
  }

  @Test
  public void extractPostmanVariableNames() {
    PostmanV2Generator postmanV2Generator = new PostmanV2Generator();

    postmanV2Generator.extractPostmanVariableNames("var1-var2   -var3");
    assertEquals(3, postmanV2Generator.postmanVariableNames.length);
  }

  @Test
  public void mapToPostmanType() {
    assertEquals("string", new PostmanV2Generator().mapToPostmanType("String"));
    assertEquals("number", new PostmanV2Generator().mapToPostmanType("integer"));
    assertEquals("any", new PostmanV2Generator().mapToPostmanType("object"));
  }

  @Test
  public void testJsonExampleIncludingValueWithCommas() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/JsonWithCommasInJsonExample.json")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    // check value with commas within quotes
    TestUtils.assertFileContains(path, "\\\"acceptHeader\\\": \\\"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\\\"");
  }

  @Test
  public void testDeprecatedEndpoint() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(configurator.toClientOptInput()).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    // verify request name (from path)
    TestUtils.assertFileContains(path, "(DEPRECATED)");
  }

  @Test
  public void testGeneratedVariables() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/BasicVariablesInExample.yaml")
            .addAdditionalProperty(PostmanV2Generator.GENERATED_VARIABLES, "RANDOM_VALUE ")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);

    TestUtils.assertFileContains(path, "\\\"createDate\\\": \\\"{{$guid}}\\\"");

  }

  @Test
  public void testFormatDescription() throws Exception {

    final String DESCRIPTION = "## Description \n\n Text with markdown \n";
    final String EXPECTED = "## Description \\n\\n Text with markdown \\n";

    assertEquals(EXPECTED, new PostmanV2Generator().formatDescription(DESCRIPTION));
  }

  @Test
  public void testAddToList() {

    PostmanV2Generator postmanV2Generator = new PostmanV2Generator();

    CodegenOperation operationUsers = new CodegenOperation();
    operationUsers.path = "/users";
    postmanV2Generator.addToList(operationUsers);

    CodegenOperation operationGroups = new CodegenOperation();
    operationGroups.path = "/groups";
    postmanV2Generator.addToList(operationGroups);

    CodegenOperation operationUserId = new CodegenOperation();
    operationUserId.path = "/users/{id}";
    postmanV2Generator.addToList(operationUserId);

    assertEquals(3, postmanV2Generator.codegenOperationsList.size());
    // verify order
    assertEquals("/groups", postmanV2Generator.codegenOperationsList.get(0).path);
    assertEquals("/users", postmanV2Generator.codegenOperationsList.get(1).path);
    assertEquals("/users/{id}", postmanV2Generator.codegenOperationsList.get(2).path);
  }

  @Test
  public void testAddToMap() {

    PostmanV2Generator postmanV2Generator = new PostmanV2Generator();

    CodegenOperation operationUsers = new CodegenOperation();
    operationUsers.path = "/users";
    operationUsers.tags = new ArrayList<>(Arrays.asList(new Tag().name("basic")));
    postmanV2Generator.addToMap(operationUsers);

    CodegenOperation operationGroups = new CodegenOperation();
    operationGroups.path = "/groups";
    operationGroups.tags = new ArrayList<>(Arrays.asList(new Tag().name("basic")));
    postmanV2Generator.addToMap(operationGroups);

    CodegenOperation operationUserId = new CodegenOperation();
    operationUserId.path = "/users/{id}";
    operationUserId.tags = new ArrayList<>(Arrays.asList(new Tag().name("basic")));
    postmanV2Generator.addToMap(operationUserId);

    // verify tag 'basic'
    assertEquals(1, postmanV2Generator.codegenOperationsByTag.size());
    assertEquals(true, postmanV2Generator.codegenOperationsByTag.containsKey("basic"));

    List<CodegenOperation> operations = postmanV2Generator.codegenOperationsByTag.get("basic");
    // verify order
    assertEquals("/groups", operations.get(0).path);
    assertEquals("/users", operations.get(1).path);
    assertEquals("/users/{id}", operations.get(2).path);
  }

  @Test
  public void testAddToMapUsingDefaultTag() {

    PostmanV2Generator postmanV2Generator = new PostmanV2Generator();

    CodegenOperation operationUsers = new CodegenOperation();
    operationUsers.path = "/users";
    postmanV2Generator.addToMap(operationUsers);

    // verify tag 'default' is used
    assertEquals(1, postmanV2Generator.codegenOperationsByTag.size());
    assertEquals(true, postmanV2Generator.codegenOperationsByTag.containsKey("default"));
  }

  // test special handling of `merchantId` and `companyId` path parameters
  @Test
  public void testMgmtApi() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/MgmtApi.json")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(configurator.toClientOptInput()).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    TestUtils.assertFileExists(Paths.get(output + "/postman.json"));

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);

    // verify merchantId default value
    TestUtils.assertFileContains(path,
            "key\": \"merchantId\", \"value\": \"{{YOUR_MERCHANT_ACCOUNT}}\",");
    // verify companyId default value
    TestUtils.assertFileContains(path,
            "key\": \"companyId\", \"value\": \"{{YOUR_COMPANY_ACCOUNT}}\",");
    // verify storeId default value is empty
    TestUtils.assertFileContains(path,
            "key\": \"storeId\", \"value\": \"\",");
  }

  @Test
  public void testRequiredQueryParameter() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(configurator.toClientOptInput()).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    Path path = Paths.get(output + "/postman.json");
    TestUtils.assertFileExists(path);
    // verify param pUserId is set as disabled=false
    TestUtils.assertFileContains(path, "{ \"key\": \"pUserId\", \"value\": \"888\", \"disabled\": false");

  }


}