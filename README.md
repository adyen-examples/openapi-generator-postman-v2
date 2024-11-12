# OpenAPI Generator for Postman v2

## Overview
Implementation of the OpenAPI generator for Postman format v2.1: from the [Adyen OpenAPI files](https://github.com/Adyen/adyen-openapi/) it generates the Postman collections in the Postman V2 JSON format.  

The collections are available on the [AdyenDev Postman space](https://www.postman.com/adyendev) as well as in the [Adyen Postman GitHub repository](https://github.com/Adyen/adyen-postman/tree/main/postman).

More on our blog [Effortless API Testing with the Adyen Postman collections](https://www.adyen.com/knowledge-hub/adyen-postman-collections).

## Usage

* [Run with Docker](#run-with-docker)
* [Build from source](#run-from-source)

See the available [options](#config-options) to customise the generation.

### Run with Docker

Run with the pre-built image passing `-i` inputspec (path of the OpenAPI spec file) and `-o` output dir (location 
of the generated file i.e ./postman/gen).

It supports the following commands:
* `generate`: create the postman.json file
* `push`: create postman.json and push to your postman.com default `My Workspace`. 
This uses the [Postman API](https://www.postman.com/postman/workspace/postman-public-workspace/folder/12959542-c705956d-1005-4fbc-803c-b6b985242a85?ctx=documentation) 
and requires a valid API key from Postman's integrations [dashboard](https://web.postman.co/settings/me/api-keys).

```docker
# generate only
docker run -v $(pwd):/usr/src/app \
   -it --rm --name postmanv2-container ghcr.io/adyen-examples/openapi-generator-postman-v2:main generate \
   -i src/test/resources/SampleProject.yaml \
   -o tmp 

# generate only (with additional parameters)
docker run -v $(pwd):/usr/src/app \
   -it --rm --name postmanv2-container ghcr.io/adyen-examples/openapi-generator-postman-v2:main generate \
   -i src/test/resources/SampleProject.yaml \
   -o tmp \
   --additional-properties folderStrategy=Tags,postmanVariables=MY_VAR1-ANOTHERVAR


# generate and push to Postman.com
# note: require POSTMAN API KEY
docker run -v $(pwd):/usr/src/app \
   -e POSTMAN_API_KEY=YOUR_POSTMAN_API_KEY \
   -it --rm --name postmanv2-container ghcr.io/adyen-examples/openapi-generator-postman-v2:main push \
   -i src/test/resources/SampleProject.yaml \
   -o tmp \
   --additional-properties folderStrategy=Tags,postmanVariables=MY_VAR1-ANOTHERVAR      
```

### Run from source

Clone and build [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator) CLI

Build `postman-v2` from source

Run OpenAPI Generator adding `postman-v2` jar file in the class path and specifying the `PostmanV2Generator` generator:
```shell
java -cp target/openapi-generator-postman-v2.jar:/openapi-generator/modules/openapi-generator-cli/target/openapi-generator-cli.jar \
  org.openapitools.codegen.OpenAPIGenerator generate -g com.adyen.codegen.postman.PostmanV2Generator \
  -i src/test/resources/BasicJson.json -o output
```
## METADATA

| Property | Value     | Notes |
| -------- |-----------|---|
| generator name | postman-v2 | pass this to the generate command after -g |
| generator stability | DEVELOPMENT |   |
| generator type | DOCUMENTATION |   |
| generator default templating engine | mustache  |   |
| helpTxt | Creates a Postman json file (v2.1.0) | Schema https://schema.postman.com/collection/json/v2.1.0/draft-07/collection.json |

## CONFIG OPTIONS
These options may be applied as additional-properties (cli) or configOptions (plugins). 

| Option                     | Description                                                                                                                                                                 | Values          | Default      |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|--------------|
| folderStrategy             | whether to create folders according to the spec’s paths or tags                                                                                                             | Paths, Tags     | Paths        |
| pathParamsAsVariables      | boolean, whether to create Postman variables for path parameters                                                                                                            | true, false     | false         |
| postmanVariables           | kebab-case list of Postman variables (i.e VAR1-VAR2-VAR3) to be created during the generation. Matching placeholders in request bodies will be defined as Postman variables |                 |       |
| generatedVariables         | kebab-case list of auto-generated variables (i.e VAR1-VAR2-VAR3). Matching placeholders in request bodies will be replaced with `{{$guid}}` Postman formula                 |                 |       |
| requestParameterGeneration | whether to generate the request parameters based on the schema or the examples                                                                                              | Example, Schema | Example      |
