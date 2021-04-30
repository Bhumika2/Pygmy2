package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import models.CatalogResponse;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class Catalog {

    Logger logger = LoggerFactory.getLogger("Pygmy");
    /**
     * searchTopic makes a http get request to catalog server to get the books information on given topic.
     * It is invoked from application controller and returns book information response from catalog server
     */
    @Inject
    NinjaProperties ninjaProperties;

    public Catalog(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }

    public List<CatalogResponse> searchTopic(String topic, String host, String port) {
        List<CatalogResponse> catalogResponse = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpClient client = HttpClient.newHttpClient();
            String serverName = host + ":" + port;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverName + "/queryBySubject/" + topic))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from catalog server: " + response.statusCode());
                return null;
            }
            catalogResponse = objectMapper.readValue(response.body().toString(), new TypeReference<List<CatalogResponse>>() {
            });
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(catalogResponse);
            logger.info("Response for search request: " + json);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return catalogResponse;
    }

    /**
     * lookupBook makes a http get request to catalog server to get the book information.
     * It is invoked from application controller and returns book information response from catalog server
     */
    public CatalogResponse lookupBook(Integer bookNumber, String host, String port) {
        CatalogResponse catalogResponse = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //String catalogReqStr = objectMapper.writeValueAsString(catalogRequest);
            HttpClient client = HttpClient.newHttpClient();
            String serverName = host + ":" + port;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverName + "/queryByItem/" + bookNumber))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from catalog server: " + response.statusCode());
                return null;
            }
            catalogResponse = objectMapper.readValue(response.body().toString(), CatalogResponse.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(catalogResponse);
            logger.info("Response for lookup request: " + json);
        } catch (Exception e) {
            logger.info(String.valueOf(e.getMessage()));
        }
        return catalogResponse;
    }
}
