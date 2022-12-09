# OpenAPI Generator for Postman v2

## Overview
Implementation of the OpenAPI generator for Postman format v2.1

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

| Option | Description                                                      | Values      | Default |
| ------ |------------------------------------------------------------------|-------------|---------|
|folderStrategy| whether to create folders according to the spec’s paths or tags  | Paths, Tags | Paths   |
|pathParamsAsVariables| boolean, whether to create Postman variables for path parameters | true, false | true    |
