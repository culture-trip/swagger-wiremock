
# WIREMOCK PLUS SWAGGER

Stubbing of dependent APIs can be achieved with Wiremock out-of-the box.

However we want to avoid finding integration issues late in the test cycle, and we want to move to Contract Based testing.

We can do this be using Wiremock stubs that validate against the Swagger contract. 

This framework will provide that.

## HOW TO BUILD

```
gradle clean build

docker build -f Dockerfile -t swagger-wiremock --no-cache .
```


## HOW TO RUN


```
1. As a standalone JAR file with the following environment variables:
   SWAGGER_WIREMOCK_PORT - Port
   SWAGGER_WIREMOCK_CONTRACT - Swagger file path or Public URL for the Swagger file (json or yaml)
   SWAGGER_WIREMOCK_MAPPINGS- Root directory of Wiremock mappings... MUST be a folder called 'mappings' see Wiremock docs

   E.g.  java -jar build/libs/swagger-wirmock.jar
   
         ... then hit http://localhost:9987

2. Programmatically

   E.g.  SwaggerWiremock myStub = new SwaggerWiremock("9987", "https://.....");
         myStub.stubFor(get(urlMatching(".*/cars"))
                .willReturn(aResponse()
                .withStatus(200)
                .withBody("{\"response\":\"lorries\"}")));
         myStub.start();

3. Docker container which only accepts port and a Swagger Public URL

   E.g. docker run -p 9986:9986 -e "SWAGGER_WIREMOCK_PORT=9986" -e "SWAGGER_WIREMOCK_CONTRACT=https://development-platform.theculturetrip.com/api/v1/reviews/_swagger.yaml" swagger-wiremock

```



