package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import models.Book;
import models.UpdateResponse;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;

public class UpdateService {

    private NinjaProperties ninjaProperties;

    Logger logger = LoggerFactory.getLogger("Pygmy");

    public UpdateService(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }

    public UpdateService() {
    }

    /**
     * updateInventory serves the http requests from order server to decrement the count of books upon successful buy
     * updates the book count in database table and in-memory hashmap
     */
    public UpdateResponse updateInventory(int id, String type) {
        String message = "failure";
        if (type.equals("Restock")) {
            restockBook(QueryService.getBookMap().get(id).getBookNumber());
            QueryService.getBookMap().get(id).setCount(5);
            message = "success";
        } else {
            synchronized (QueryService.getBookMap()) {
                if (QueryService.getBookMap().get(id) != null) {
                    if (QueryService.getBookMap().get(id).getCount() > 0) {
                        QueryService.getBookMap().get(id).setCount(QueryService.getBookMap().get(id).getCount() - 1);
                        message = "success";
                    }
                }
            }
            if (message.equals("success")) {
                logger.info("Updating inventory of item " + id);
                updateDB(QueryService.getBookMap().get(id).getBookNumber());
            }
        }
        UpdateResponse updateRes = new UpdateResponse();
        updateRes.setBookNumber(id);
        updateRes.setMessage(message);
        return updateRes;
    }

    /**
     * updateDB updates the count of book in database table after successful buy
     */
    private void updateDB(Integer bookNumber) {
        try {
            Statement statement = DBService.getConnection().createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("update book set count = count - 1 where book_number = " + bookNumber);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * restockBook updates the count of book in database to replenish the stock
     */
    private void restockBook(Integer bookNumber) {
        logger.info("Restocking book - " + bookNumber);
        try {
            Statement statement = DBService.getConnection().createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("update book set count = 5 where book_number = " + bookNumber);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * updateCost serves the http requests to update the cost of books
     * updates the book cost in database table and in-memory hashmap
     */
    public UpdateResponse updateCost(int id, int cost) {
        updateCostInDb(QueryService.getBookMap().get(id).getBookNumber(), cost);
        QueryService.getBookMap().get(id).setCost(cost);

        String message = "success";
        UpdateResponse updateRes = new UpdateResponse();
        updateRes.setBookNumber(id);
        updateRes.setMessage(message);
        return updateRes;
    }


    /**
     * updateCostInDb updates the cost of book in database
     */
    private void updateCostInDb(Integer bookNumber, Integer cost) {
        logger.info("Updating cost in DB for book - " + bookNumber);
        try {
            Statement statement = DBService.getConnection().createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("update book set cost = " + cost + " where book_number = " + bookNumber);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * frontendCacheInvalidate invalidates the cache on frontend server in case of updates to DB
     */
    public void frontendCacheInvalidate(int id) {
        try {
            logger.info("Invalidating frontend cache");
            HttpClient client = HttpClient.newHttpClient();
            String serverName = ninjaProperties.get("frontendHost") + ":" + ninjaProperties.get("frontendPort");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + serverName + "/invalidate/" + id))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from frontend server: " + response.statusCode());
            } else {
                logger.info("Frontend cache invalidated successfully");
            }

        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * syncReplica syncs all DBs in case of updates to DB
     */
    public void syncReplica(int id, String type, int cost) {
        logger.info("Syncing DB across all replica");
        try {
            HttpClient client = HttpClient.newHttpClient();
            String catalogServer = getCatalogServer();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + catalogServer + "/syncDb/" + id + "/" + type + "?cost=" + cost))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from catalog server: " + response.statusCode());
            } else {
                logger.info("DB sync successful");
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * getCatalogServer return the ip and port of the other catalog server
     *
     */
    public String getCatalogServer() {
        String serverName = System.getProperty("server.name");
        switch (serverName) {
            case "1":
                return ninjaProperties.get("catalogReplicaHost") + ":" + ninjaProperties.get("catalogReplicaPort");
            case "2":
                return ninjaProperties.get("catalogHost") + ":" + ninjaProperties.get("catalogPort");
        }
        return "";
    }


    public void updateDB(){
        logger.info("Re-syncing DB across replica");
        try {
            HttpClient client = HttpClient.newHttpClient();
            String catalogServer = getCatalogServer();
            ObjectMapper objectMapper = new ObjectMapper();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + catalogServer + "/resyncDB"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.info("Non 200 response code received from catalog server for resync: " + response.statusCode());
            } else {
                logger.info("Updating DB for resync");
                Statement statement = DBService.getConnection().createStatement();
                List<Book> bookList= objectMapper.readValue(response.body().toString(), new TypeReference<List<Book>>(){});
                for(Book book: bookList){
                    statement.executeUpdate("update book set cost = " + book.getCost() + ", count = " + book.getCount() + " where book_number = " + book.getBookNumber());
                }
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }
}
