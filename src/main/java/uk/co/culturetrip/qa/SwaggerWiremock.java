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

public class SwaggerWiremock {
    // declare fields

    public static WireMockServer wireMockServer = null;
    private static SwaggerValidationListener wireMockListener = null;

    /*********************************************************************************
     configure port,  swagger, stateModel, mappings file location
     connect transformers
     ********************************************************************************/

    public void configureStub(String port, String swaggerFile, String mappingsFileLocation) throws Exception {

        // start server passing in port and mappingd
        wireMockServer = new WireMockServer(options()
                .port(Integer.parseInt(port))
                .usingFilesUnderDirectory(mappingsFileLocation)
                .extensions(ContractValidationTransformer.class));

        // convert yaml to json if needed
        String swagger = new String(Files.readAllBytes(Paths.get(swaggerFile)));
        if (swaggerFile.endsWith("yml") || swaggerFile.endsWith("yaml"))
            swagger = convertYamlToJson(swagger);

        // add swagger validator listerner
        wireMockListener = new SwaggerValidationListener(swagger);
        wireMockServer.addMockServiceRequestListener(wireMockListener);

        System.out.println("configured smart stub on port:" + port + " with swagger:" + swaggerFile + " with mappings:" + mappingsFileLocation);

    }

    /**************
     start server
     **************/
    public void start() {
        if (wireMockServer != null && !wireMockServer.isRunning()) {
            wireMockServer.start();
            try {
                Thread.sleep(2000);
            } catch (Exception e) {

            }
            System.out.println("started smart stub");
        }
    }

    /**************
     stop server
     **************/
    public void stop() {
        if (wireMockServer == null)
            System.out.println("Wiremock server may have found an invalid contract - please check logs");
        else
            wireMockServer.stop();
    }

    /*******************************************************
     validate contract swagger and state and apply latency
     ******************************************************/
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
        SwaggerWiremock mystub = new SwaggerWiremock();
        try {
            mystub.configureStub(args[0], args[1], args[2]);
            mystub.start();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

}