package uk.co.culturetrip.qa;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.atlassian.oai.validator.wiremock.SwaggerValidationListener;
import com.github.tomakehurst.wiremock.common.FileSource;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;


import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class SwaggerWiremock {
    // declare fields

    public static WireMockServer wireMockServer = null;
    private static SwaggerValidationListener wireMockListener = null;

    /*********************************************************************************
     configure port,  swagger, stateModel, mappings file location
     connect transformers
     ********************************************************************************/

    public void configureStub(String port, String swaggerFile, String mappingsFileLocation) {

        wireMockServer = new WireMockServer(options()
                .port(Integer.parseInt(port))
                .usingFilesUnderDirectory(mappingsFileLocation)
                .extensions(ContractValidationTransformer.class));

        try {
            String swagger = new String(Files.readAllBytes(Paths.get(swaggerFile)));
            wireMockListener = new SwaggerValidationListener(swagger);
            wireMockServer.addMockServiceRequestListener(wireMockListener);
            System.out.println("configured smart stub on port:" + port + " with swagger:" + swaggerFile + " with mappings:" + mappingsFileLocation);

        } catch (Exception e) {
            System.out.println("Error reading swagger:"+e.getLocalizedMessage());
        }

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
                wireMockListener.requestReceived(request, response);
                wireMockListener.assertValidationPassed();
                return response;

            } catch (Exception e) {
                return Response.Builder.like(response)
                        .but()
                        .statusMessage("Invalid contract: " + e.getLocalizedMessage())
                        .status(400)
                        .build();
            }
        }

        @Override
        public String getName() {
            return "ContractValidationTransformer";
        }
    }
}