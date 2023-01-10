FROM adoptopenjdk:11-jre-hotspot

# default destination folder
ENV outputFolder=/usr/src/app

WORKDIR /usr/src/app

COPY tmp/openapi-generator-cli.jar /openapi-generator-cli.jar
COPY target/openapi-generator-postman-v2.jar /openapi-generator-postman-v2.jar

COPY ./script.sh /
RUN chmod +x /script.sh
ENTRYPOINT ["/script.sh"]
