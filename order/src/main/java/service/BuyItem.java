package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import models.Book;
import models.BuyRequest;
import models.BuyResponse;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BuyItem {
    Logger logger = LoggerFactory.getLogger("Pygmy");
    /**
     * checkBookAvailability makes http request to catalog server to check the availability of book
     * returns the response from catalog server
     */
    @Inject
    NinjaProperties ninjaProperties;

    /**
     * buy check the availability of book and initiates the buy operation
     * returns the message of purchase status
     */
    public BuyItem(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }

    public BuyResponse buy(BuyRequest buyObj) {
        Integer bookNumber = buyObj.getBookNumber();
        Book book = checkBookAvailability(bookNumber);
        String message;
        if (book != null && book.getCount() > 0) {
            boolean status = initiateBuy(bookNumber);
            message = "Buy failed for book - " + book.getBookName();
            if (status) {
                message = "Successfully bought book - " + book.getBookName();
            }
        } else {
            message = "Insufficient quantity of book - " + book.getBookName();
            restockBook(bookNumber);
        }
        BuyResponse buyResponse = new BuyResponse();
        buyResponse.setBookNumber(bookNumber);
        buyResponse.setMessage(message);
        return buyResponse;
    }

    public Book checkBookAvailability(Integer bookNumber) {
        logger.info("Checking availability of book: " + bookNumber);
        Book book = null;
        try {
            HttpClient client = HttpClient.newHttpClient();
            String serverName = getCatalogServer();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverName + "/queryByItem/" + bookNumber))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from catalog server: " + response.statusCode());
            }
            ObjectMapper mapper = new ObjectMapper();
            book = mapper.readValue(response.body().toString(), Book.class);
        } catch (Exception e) {
            logger.info(String.valueOf(e.getStackTrace()));
        }
        return book;
    }

    /**
     * initiateBuy makes http request to catalog server to update the count of book purchased in the inventory
     * returns true on successful buy
     */
    public boolean initiateBuy(Integer bookNumber) {
        logger.info("Initiating buy request for book: " + bookNumber);
        boolean buyStatus = false;
        try {
            HttpClient client = HttpClient.newHttpClient();
            String serverName = getCatalogServer();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverName + "/updateInventory/" + bookNumber + "/Buy"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from catalog server: " + response.statusCode());
            }
            ObjectMapper mapper = new ObjectMapper();
            BuyResponse buyResponse = mapper.readValue(response.body().toString(), BuyResponse.class);
            if (buyResponse.getMessage().equals("success")) {
                buyStatus = true;
            }
        } catch (Exception e) {
            logger.info(String.valueOf(e.getStackTrace()));
        }
        return buyStatus;
    }

    /**
     * restockBook makes http request to catalog server to replenish the inventory
     */
    public void restockBook(Integer bookNumber) {
        logger.info("Initiating restock request for book: " + bookNumber);
        try {
            HttpClient client = HttpClient.newHttpClient();
            String serverName = getCatalogServer();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverName + "/updateInventory/" + bookNumber + "/Restock"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from catalog server: " + response.statusCode());
            }
        } catch (Exception e) {
            logger.info(String.valueOf(e.getStackTrace()));
        }
    }

    private String getCatalogServer() {
        String serverName = System.getProperty("server.name");
        switch (serverName) {
            case "1":
                return ninjaProperties.get("catalogHost") + ":" + ninjaProperties.get("catalogPort");
            case "2":
                return ninjaProperties.get("catalogReplicaHost") + ":" + ninjaProperties.get("catalogReplicaPort");
        }
        return "";
    }
}
