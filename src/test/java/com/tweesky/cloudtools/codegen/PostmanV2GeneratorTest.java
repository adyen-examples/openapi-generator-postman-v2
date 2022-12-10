package com.tweesky.cloudtools.codegen;

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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PostmanV2GeneratorTest {

  @Test
  public void testInitialConfigValues() throws Exception {
    final PostmanV2Generator postmanV2Generator = new PostmanV2Generator();
    postmanV2Generator.processOpts();

    Assert.assertEquals(postmanV2Generator.folderStrategy, "Paths");

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
  public void testBasicGeneration() throws IOException {

    File output = Files.createTempDirectory("postmantest_").toFile();
    output.deleteOnExit();

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .addAdditionalProperty(PostmanV2Generator.POSTMAN_FILE, "basic.json")
            .setInputSpec("./src/test/resources/Basic.yaml")
            .setOutputDir(output.getAbsolutePath().replace("\\", "/"));

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    List<File> files = generator.opts(clientOptInput).generate();

    System.out.println(files);
    files.forEach(File::deleteOnExit);

    TestUtils.assertFileExists(Paths.get(output + "/basic.json"));
    Path docFile = Paths.get(output + "/basic.json");
    TestUtils.assertFileContains(docFile, "\"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"");
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

    TestUtils.assertFileExists(Paths.get(output + "/postman.json"));
    Path docFile = Paths.get(output + "/postman.json");
    TestUtils.assertFileContains(docFile, "\"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"");
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

    TestUtils.assertFileExists(Paths.get(output + "/postman.json"));
    Path docFile = Paths.get(output + "/postman.json");
    // verify response body comes from components/examples
    TestUtils.assertFileContains(docFile, "\"body\": {\"id\":777,\"firstName\":\"Alotta\",\"lastName\":\"Rotta\",\"email\":\"alotta.rotta@gmail.com\",");
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
  public void extractExampleByName() {
    String str = "#/components/examples/get-user-basic";

    assertEquals("get-user-basic", new PostmanV2Generator().extractExampleByName(str));
  }

}