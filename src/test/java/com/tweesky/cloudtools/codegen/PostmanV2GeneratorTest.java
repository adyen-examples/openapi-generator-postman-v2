package com.tweesky.cloudtools.codegen;

import org.junit.Assert;
import org.junit.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import static org.junit.Assert.assertEquals;

public class PostmanV2GeneratorTest {

  @Test
  public void launchCodeGenerator() {

    final CodegenConfigurator configurator = new CodegenConfigurator()
            .setGeneratorName("postman-v2")
            .setInputSpec("./src/test/resources/SampleProject.yaml")
            .setOutputDir("target/out/postman-v2");

    final ClientOptInput clientOptInput = configurator.toClientOptInput();
    DefaultGenerator generator = new DefaultGenerator();
    generator.opts(clientOptInput).generate();
  }

  @Test
  public void extractVariables() {
    String str = "/api/{var}/archive";

    assertEquals(1, new PostmanV2Generator().extractVariables(str).size());
  }

}