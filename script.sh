#!/bin/bash

if [ $# == 0 ]; then
  echo "No command specified. Available commands: generate, push"
	exit
fi

command=$1
cmdline=$@
echo "-->Executing [$command]"

if [[ $command == "generate" ]]
then
  cmdparams="${cmdline#*generate}"
elif [[ $command == "push" ]]
then
  if [[ $POSTMAN_API_KEY == "" ]]
  then
    echo "ERROR: define postmanApiKey when running [push] command "
    exit
  fi
    cmdparams="${cmdline#*push}"
fi

java -cp /openapi-generator-postman-v2.jar:/openapi-generator-cli.jar \
  org.openapitools.codegen.OpenAPIGenerator generate -g com.tweesky.cloudtools.codegen.PostmanV2Generator $cmdparams

if [[ $command == "push" ]]
then
  echo "--> Pushing to Postman"

  var=$(cat /usr/src/app/tmp/postman.json)

  echo '{"collection": '"$var"' }' | curl -X POST \
  --header 'Content-Type: application/json' \
  --header 'X-API-Key: '"${POSTMAN_API_KEY}"'' \
  -d @- "https://api.getpostman.com/collections"

fi