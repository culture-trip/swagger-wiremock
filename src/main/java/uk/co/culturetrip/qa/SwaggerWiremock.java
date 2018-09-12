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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

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

        // extract swagger string
        String swagger = (swaggerFile.startsWith("http") || swaggerFile.startsWith("file:///")) ? getUrlContents(swaggerFile) : new String(Files.readAllBytes(Paths.get(swaggerFile)));

        // convert yaml to json if needed
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


    private static String getUrlContents(String theUrl)
    {
        StringBuilder content = new StringBuilder();

        // many of these calls can throw exceptions, so i've just
        // wrapped them all in one try/catch statement.
        try
        {
            // create a url object
            URL url = new URL(theUrl);

            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();

            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }

    // String port, String swaggerFile, String mappingsFileLocation
    public static void main(String[] args) {
        try {
            Map<String, String> env = System.getenv();
            SwaggerWiremock mystub = null;
            if (env.get("SWAGGER_WIREMOCK_MAPPINGS_FOLDER")!=null)
                mystub = new SwaggerWiremock(env.get("SWAGGER_WIREMOCK_PORT"), env.get("SWAGGER_WIREMOCK_CONTRACT"), env.get("SWAGGER_WIREMOCK_MAPPINGS_FOLDER"));
            else
                mystub = new SwaggerWiremock(env.get("SWAGGER_WIREMOCK_PORT"), env.get("SWAGGER_WIREMOCK_CONTRACT"));
            mystub.start();
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

}