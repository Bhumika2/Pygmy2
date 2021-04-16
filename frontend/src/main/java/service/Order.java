package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import models.OrderRequest;
import models.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.google.inject.Inject;
import ninja.utils.NinjaProperties;

public class Order {

    Logger logger = LoggerFactory.getLogger("Pygmy");
    public Order(NinjaProperties ninjaProperties){
        this.ninjaProperties = ninjaProperties;
    }
    /**
     * buyBook makes a http post request to order server to buy the book.
     * It is invoked from application controller and returns message about the purchase status
     */
    @Inject
    NinjaProperties ninjaProperties;
    public OrderResponse buyBook(OrderRequest orderReq) {
        OrderResponse orderResponse = null;
        try {
            logger.info("Calling Order microservice");
            ObjectMapper objectMapper = new ObjectMapper();
            String orderReqStr = objectMapper.writeValueAsString(orderReq);
            HttpClient client = HttpClient.newHttpClient();
            String serverName = ninjaProperties.get("orderHost")+":"+ninjaProperties.get("orderPort");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://"+serverName+"/buy"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(orderReqStr))
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from order server: " + response.statusCode());
            }
            orderResponse = objectMapper.readValue(response.body().toString(), OrderResponse.class);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(orderResponse);
            logger.info("Response for buy request: " + json);
        } catch (Exception e) {
            logger.info(String.valueOf(e.getStackTrace()));
        }
        return orderResponse;
    }
}
