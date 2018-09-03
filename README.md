
Stubbing of dependent APIs can be achieved with Wiremock out-of-the box.

However we want to avoid finding integration issues late in the test cycle, and support Contract Based testing, 
by using Wiremock stubs, that validate against Swagger contract. 

To run...

Arguments:
- Port
- Swagger file (json or yaml)
- Root directory of Wiremock mappings... MUST be a folder called 'mappings' see Wiremock docs

java -jar build/libs/swagger-wirmock.jar 9987 src/test/resources/openApi.json src/test/resources

