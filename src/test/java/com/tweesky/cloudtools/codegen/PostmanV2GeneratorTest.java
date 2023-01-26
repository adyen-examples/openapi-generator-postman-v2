package com.tweesky.cloudtools.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tweesky.cloudtools.codegen.model.PostmanRequestItem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostmanV2GeneratorTest {

  @Test
  public void testInitialConfigValues() throws Exception {
    final PostmanV2Generator postmanV2Generator = new PostmanV2Generator();
    postmanV2Generator.processOpts();

    Assert.assertEquals(postmanV2Generator.folderStrategy, "Paths");
    Assert.assertEquals(postmanV2Generator.postmanFile, "postman.json");

    Assert.assertNotNull(postmanV2Generator.additionalProperties().get("codegenOperationsList"));
    Assert.assertNull(postmanV2Generator.additionalProperties().get("codegenOperationsByTag"));
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
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    TestUtils.assertFileExists(Paths.get(output + "/postman.json"));

    JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(output + "/postman.json"));
    // verify json has variables
    assertTrue(jsonObject.get("variable") instanceof JSONArray);
    assertEquals(4, ((JSONArray) jsonObject.get("variable")).size());
  }

  @Test
  public void testVariablesInRequestExample() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/BasicVariablesInExample.yaml")
            .addAdditionalProperty(PostmanV2Generator.POSTMAN_VARIABLES, "MY_VAR_NAME -MY_VAR_LAST_NAME ")
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

  }
  @Test
  public void testGenerateWithoutPathParamsVariables() throws IOException, ParseException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .addAdditionalProperty(PostmanV2Generator.PATH_PARAMS_AS_VARIABLES, false)
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
    assertEquals(3, ((JSONArray) jsonObject.get("variable")).size());
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
    TestUtils.assertFileContains(path, "\"raw\": \"{\\n \\\"id\\\": 777,\\n \\\"firstName\\\": \\\"Alotta\\\",\\n \\\"lastName\\\": \\\"Rotta\\\",\\n ");
  }

  @Test
  public void testNamingRequestsWithUrl() throws IOException, ParseException {

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
    TestUtils.assertFileContains(path, "\"name\": \"/users/{{userId}}\"");
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
    // check auth apiKey
    TestUtils.assertFileContains(path, "\"auth\": { \"type\": \"apikey\", \"apikey\": [");
    // check auth basic NOT found
    TestUtils.assertFileNotContains(path, "\"auth\": { \"type\": \"basic\", \"basic\": [");
  }


  @Test
  public void extractVariables() {
    String str = "/api/{var}/archive";

    assertEquals(1, new PostmanV2Generator().extractVariables(str).size());
  }

  @Test
  public void doubleCurlyBraces() {
    String str = "/api/{var}/archive";

    assertEquals("/api/{{var}}/archive", new PostmanV2Generator().doubleCurlyBraces(str));
  }

  @Test
  public void doubleCurlyBracesNoChanges() {
    String str = "/api/{{var}}/archive";

    assertEquals("/api/{{var}}/archive", new PostmanV2Generator().doubleCurlyBraces(str));
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

}