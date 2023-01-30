FROM maven:3.5-jdk-11 AS build
COPY src /src
COPY pom.xml /
COPY script.sh /
COPY tmp /tmp
RUN mvn -f /pom.xml clean package


FROM adoptopenjdk:11-jre-hotspot

WORKDIR /usr/src/app

COPY --from=build tmp/openapi-generator-cli.jar /openapi-generator-cli.jar
COPY --from=build target/openapi-generator-postman-v2.jar /openapi-generator-postman-v2.jar

COPY --from=build  ./script.sh /
RUN chmod +x /script.sh
ENTRYPOINT ["/script.sh"]
