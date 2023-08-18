#!/bin/bash

if [ $# == 0 ]; then
  echo "No command specified. Available commands: generate, push"
	exit
fi

command=$1
cmdline=$@
echo "-->Executing $command ($cmdline)"

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

statusCode=$(java -jar /openapi-generator-postman-v2.jar generate -g com.tweesky.cloudtools.codegen.PostmanV2Generator $cmdparams)

if [[ ! $statusCode ]]
then
  echo "Error during Postman generation"
  exit 1
fi

output_file=$(find /usr/src/app -name "postman.json" | sort -nr | head -n 1)

if [[ $command == "push" ]]
then
  echo "--> Pushing to Postman"

  var=$(cat "$output_file")

  echo '{"collection": '"$var"' }' | curl -X POST \
  --header 'Content-Type: application/json' \
  --header 'X-API-Key: '"${POSTMAN_API_KEY}"'' \
  -d @- "https://api.getpostman.com/collections"

fi
