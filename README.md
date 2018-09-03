
# WIREMOCK PLUS SWAGGER

Stubbing of dependent APIs can be achieved with Wiremock out-of-the box.

However we want to avoid finding integration issues late in the test cycle, and we want to move to Contract Based testing.

We can do this be using Wiremock stubs that validate against the wagger contract. 

This framework will provide that... you will need to provide a Swagger file and the canned request/responses required by your tests.

## HOW TO RUN

A JAR file runs Wiremock with the following arguments:
- Port
- Swagger file (json or yaml)
- Root directory of Wiremock mappings... MUST be a folder called 'mappings' see Wiremock docs

```
E.g.

java -jar build/libs/swagger-wirmock.jar 9987 src/test/resources/openApi.json src/test/resources
```




