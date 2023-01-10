#!/bin/bash

if [ $# == 0 ]; then
  echo "No command specified. Available commands: generate, push"
	exit
fi

command=$1
echo "-->Executing [$command]"

if [[ $command == "push" ]]
then
  if [[ $postmanApiKey == "" ]]
  then
    echo "ERROR: define postmanApiKey when running [push] command "
    exit
  fi
fi

java -cp /openapi-generator-postman-v2.jar:/openapi-generator-cli.jar \
  org.openapitools.codegen.OpenAPIGenerator generate -g com.tweesky.cloudtools.codegen.PostmanV2Generator \
  -i $inputFile -o $outputFolder --additional-properties namingRequests=url

echo "--> Generated $outputFolder/postman.json"

if [[ $command == "push" ]]
then
  echo "--> Pushing to Postman"

  value=`cat $outputFolder/postman.json`

  curl --location --request POST 'https://api.getpostman.com/collections' \
  --header 'Content-Type: application/json' \
  --header 'X-API-Key: '"${postmanApiKey}"'' \
  --data-raw '{ "collection": '"$value"' }'

fi
