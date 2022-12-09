package com.tweesky.cloudtools.codegen;

import org.junit.Assert;
import org.junit.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
  public void testSupportingFiles() throws Exception {
    final PostmanV2Generator postmanV2Generator = new PostmanV2Generator();

    Assert.assertEquals(1, postmanV2Generator.supportingFiles().size());

  }

  @Test
  public void testBasicGeneration() throws IOException {

    File output = Files.createTempDirectory("test").toFile();
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

    TestUtils.assertFileExists(Paths.get(output + "/postman.json"));
    Path docFile = Paths.get(output + "/postman.json");
    TestUtils.assertFileContains(docFile, "\"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"");

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