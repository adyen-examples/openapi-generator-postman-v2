FROM maven:3.5-jdk-8 AS build
COPY src /src
COPY pom.xml /
COPY script.sh /
COPY tmp /tmp
RUN mvn -f /pom.xml clean package


FROM adoptopenjdk:11-jre-hotspot

WORKDIR /usr/src/app

COPY tmp/openapi-generator-cli.jar /openapi-generator-cli.jar
COPY target/openapi-generator-postman-v2.jar /openapi-generator-postman-v2.jar

COPY ./script.sh /
RUN chmod +x /script.sh
ENTRYPOINT ["/script.sh"]
