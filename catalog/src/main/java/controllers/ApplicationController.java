/**
 * Copyright (C) 2013 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import models.Book;
import models.UpdateResponse;
import ninja.Result;
import ninja.Results;
import ninja.params.Param;
import ninja.params.PathParam;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.DBService;
import service.HeartBeatService;
import service.QueryService;
import service.UpdateService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


@Singleton
public class ApplicationController {

    Logger logger = LoggerFactory.getLogger("Pygmy");

    NinjaProperties ninjaProperties;

    @Inject
    public ApplicationController(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
        QueryService.getAllBooks(ninjaProperties);
    }

    /**
     * queryByItem serves the lookup http requests from frontend and order server
     * returns book details corresponding to specified id
     */
    public Result queryByItem(@PathParam("id") int id) {
        logger.info("Query by Item request received for item: " + id);
        long startTime = System.nanoTime();
        Book book = QueryService.getBookMap().get(id);
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Query by Item response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(book);
    }

    /**
     * queryBySubject serves the search http requests from frontend server
     * returns book details corresponding to specified topic
     */
    public Result queryBySubject(@PathParam("topic") String topic) throws UnsupportedEncodingException {
        topic = URLDecoder.decode(topic, StandardCharsets.UTF_8.toString());
        logger.info("Query by Subject request received for topic: " + topic);
        long startTime = System.nanoTime();
        List<Integer> bookList = QueryService.getTopicMap().get(topic);
        List<Book> booksByTopic = new ArrayList<>();
        for (Integer bookNumber : bookList) {
            booksByTopic.add(QueryService.getBookMap().get(bookNumber));
        }
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Query by Subject response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(booksByTopic);
    }

    /**
     * update serves the http requests from order server to decrement the count of books upon successful buy
     * updates the book count in database table and in-memory hashmap
     */
    public Result update(@PathParam("id") int id, @PathParam("type") String type) {
        logger.info(type + " update request received for item: " + id);
        long startTime = System.nanoTime();
        UpdateService updateService = new UpdateService(ninjaProperties);
        updateService.frontendCacheInvalidate(id);
        UpdateResponse updateRes = updateService.updateInventory(id, type);
        //TODO: check if lock required here
        if (updateRes.getMessage().equals("success")) {
            updateService.syncReplica(id, type, 0);
        }
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Update response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(updateRes);
    }

    /**
     * updateCost serves the http requests to update the cost of books
     * updates the book cost in database table and in-memory hashmap
     */
    public Result updateCost(@PathParam("id") int id, @PathParam("cost") int cost) {
        logger.info("Update cost request received for item: " + id);
        long startTime = System.nanoTime();
        UpdateService updateService = new UpdateService(ninjaProperties);
        UpdateResponse updateRes = updateService.updateCost(id, cost);
        if (updateRes.getMessage().equals("success")) {
            updateService.frontendCacheInvalidate(id);
            updateService.syncReplica(id, "Cost", cost);
        }
        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Update cost response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(updateRes);
    }

    /**
     * syncDB updates the DB (cost or count) as per the request from other replica
     */
    public Result syncDB(@PathParam("id") int id, @PathParam("key") String key, @Param("cost") int cost) {
        logger.info("Syncing DB");
        UpdateService updateService = new UpdateService();
        switch (key) {
            case "Cost":
                updateService.updateCost(id, cost);
                break;
            case "Buy":
                updateService.updateInventory(id, "Buy");
                break;
            case "Restock":
                updateService.updateInventory(id, "Restock");
                break;
        }
        return Results.json().render("success");
    }

    /**
     * resyncDB fetch the DB data from replica during recovery
     */
    public Result resyncDB() {
        logger.info("Re-Syncing DB");
        Statement statement = null;
        ResultSet rs = null;
        List<Book> bookList = null;
        try {
            statement = DBService.getConnection().createStatement();
            statement.setQueryTimeout(30);
            rs = statement.executeQuery("select * from book");
            bookList = new ArrayList<>();
            while (rs.next()) {
                Book book = new Book(rs.getInt("book_number"), rs.getString("book_name"),
                        rs.getString("topic"), rs.getInt("cost"), rs.getInt("count"));
                bookList.add(book);
            }
        }catch(Exception e){
            logger.info(e.getMessage());
        }
        return Results.json().render(bookList);
    }


}
