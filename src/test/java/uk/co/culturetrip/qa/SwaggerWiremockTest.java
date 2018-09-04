package uk.co.culturetrip.qa;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.*;

import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class SwaggerWiremockTest {

    SwaggerWiremock myStub = null;

    @After
    public void tearDown() {
        myStub.stop();
    }

    @Test
    public void basicHappyPathTestJson() throws Exception {
        myStub = new SwaggerWiremock("9988", "src/test/resources/openApi.json", "src/test/resources" );
        myStub.start();
        URL url = new URL("http://localhost:9988/cars");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "*/*");
        Assert.assertEquals("OK", con.getResponseMessage());
        Assert.assertEquals(200, con.getResponseCode());
    }

    @Test
    public void basicHappyPathTestYaml() throws Exception {
        myStub = new SwaggerWiremock("9988", "src/test/resources/openApi.yml", "src/test/resources" );
        myStub.start();
        URL url = new URL("http://localhost:9988/cars");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "*/*");
        String response = con.getResponseMessage();
        Assert.assertEquals("OK", con.getResponseMessage());
        Assert.assertEquals(200, con.getResponseCode());
    }

    @Test
    public void basicSadPathTest() throws Exception {
        myStub = new SwaggerWiremock("9988", "src/test/resources/openApi.json", "src/test/resources" );
        myStub.start();
        URL url = new URL("http://localhost:9988/cars");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        String response = con.getResponseMessage();
        Assert.assertEquals("Invalid contract: Validation failed.?[ERROR] Request Accept header '*; q=.2' is not a valid media type", response);
    }

    @Test
    public void basicHappyPathTestProgrammatic() throws Exception {
        myStub = new SwaggerWiremock("9987", "src/test/resources/openApi.json");
        myStub.stubFor(get(urlMatching(".*/cars")).atPriority(0)
                .willReturn(aResponse()
                        .withStatus(200)
                .withBody("{\"response\":\"lorries\"}")));
        myStub.start();

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(new HttpGet("http://localhost:9987/cars"));
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        Assert.assertEquals("{\"response\":\"lorries\"}", responseString);
    }

    @Test
    public void sadPathProgromatic() throws Exception {
        myStub = new SwaggerWiremock("9987", "src/test/resources/openApi.json");
        myStub.stubFor(get(urlMatching(".*/cars")).atPriority(0)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"response\":\"trains\"}")));
        myStub.start();
        URL url = new URL("http://localhost:9987/cars");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        String response = con.getResponseMessage();
        Assert.assertEquals("Invalid contract: Validation failed.?[ERROR] Request Accept header '[application/json]' does not match any defined response types. Must be one of: [*/*].", response);
    }
}
