package uk.co.culturetrip.qa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.atlassian.oai.validator.wiremock.SwaggerValidationListener;
import com.github.tomakehurst.wiremock.common.FileSource;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SwaggerWiremock extends WireMockServer {

    private static SwaggerValidationListener wireMockListener = null;

    /*********************************************************************************
     configure port,  swagger, stateModel, mappings file location
     connect transformers
     ********************************************************************************/

    public SwaggerWiremock(String port, String swaggerFile, String mappingsFileLocation) throws Exception {

        // Start server on port with transformer and canned files
        super(options()
                .port(Integer.parseInt(port))
                .usingFilesUnderDirectory(mappingsFileLocation)
                .extensions(ContractValidationTransformer.class));
        setUpServer(swaggerFile);
        System.out.println("configured smart stub on port:" + port + " with swagger:" + swaggerFile + " with mappings:" + mappingsFileLocation);
    }

    public SwaggerWiremock(String port, String swaggerFile) throws Exception {

        // Start server on port with transformer and canned files
        super(options()
                .port(Integer.parseInt(port))
                .extensions(ContractValidationTransformer.class));
        setUpServer(swaggerFile);
        System.out.println("configured smart stub on port:" + port + " with swagger:" + swaggerFile);
    }

    private void setUpServer(String swaggerFile) throws Exception{
        // convert yaml to json if needed
        String swagger = new String(Files.readAllBytes(Paths.get(swaggerFile)));
        if (swaggerFile.endsWith("yml") || swaggerFile.endsWith("yaml"))
            swagger = convertYamlToJson(swagger);

        // add swagger validator listener
        wireMockListener = new SwaggerValidationListener(swagger);
        super.addMockServiceRequestListener(wireMockListener);
  }


    /****************************
     validate contract swagger
     ****************************/
    public static class ContractValidationTransformer extends ResponseTransformer {

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {

            // validate swagger
            try {
                wireMockListener.reset();
                wireMockListener.requestReceived(request, response);
                wireMockListener.assertValidationPassed();
                return response;

            } catch (Exception e) {
                return Response.Builder.like(response)
                        .but()
                        .statusMessage("Invalid contract: " + e.getLocalizedMessage())
                        .body("Invalid contract: " + e.getLocalizedMessage())
                        .status(400)
                        .build();
            }
        }

        @Override
        public String getName() {
            return "ContractValidationTransformer";
        }
    }



    private String convertYamlToJson(String yaml) throws Exception{
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);
        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    // String port, String swaggerFile, String mappingsFileLocation
    public static void main(String[] args) {
        try {
            SwaggerWiremock mystub = new SwaggerWiremock(args[0], args[1], args[2]);
            mystub.start();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

}