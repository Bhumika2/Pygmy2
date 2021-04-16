/**
 * Copyright (C) 2012-2019 the original author or authors.
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
 * <p>
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
 * <p>
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
 * <p>
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
 * <p>
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

/**
 * Copyright (C) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers;

import com.google.inject.Singleton;
import models.Book;
import models.UpdateResponse;
import ninja.Result;
import ninja.Results;
import ninja.params.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Singleton
public class ApplicationController {

    Logger logger = LoggerFactory.getLogger("Pygmy");
    private HashMap<Integer, Book> bookMap;
    private HashMap<String, List<Integer>> topicMap;
    private Connection connection;

    public ApplicationController() {
        setDBConnection();
        getAllBooks();
    }
    /**
     * queryByItem serves the lookup http requests from frontend and order server
     * returns book details corresponding to specified id
     */
    public Result queryByItem(@PathParam("id") int id) {
        logger.info("Query by Item request received for item: " + id);
        long startTime = System.nanoTime();
        Book book = bookMap.get(id);
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
        List<Integer> bookList = topicMap.get(topic);
        List<Book> booksByTopic = new ArrayList<>();
        for (Integer bookNumber : bookList) {
            booksByTopic.add(bookMap.get(bookNumber));
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
        String message = "failure";
        if (type.equals("Restock")) {
            restockBook(bookMap.get(id).getBookNumber());
            bookMap.get(id).setCount(5);
            message = "success";
        } else {
            synchronized (bookMap) {
                if (bookMap.get(id) != null) {
                    if (bookMap.get(id).getCount() > 0) {
                        bookMap.get(id).setCount(bookMap.get(id).getCount() - 1);
                        message = "success";
                    }
                }
            }
            if (message.equals("success")) {
                updateDB(bookMap.get(id).getBookNumber());
            }
        }
        UpdateResponse updateRes = new UpdateResponse();
        updateRes.setBookNumber(id);
        updateRes.setMessage(message);
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

        updateCostInDb(bookMap.get(id).getBookNumber(), cost);
        bookMap.get(id).setCost(cost);

        String message = "success";
        UpdateResponse updateRes = new UpdateResponse();
        updateRes.setBookNumber(id);
        updateRes.setMessage(message);

        long timeElapsed = System.nanoTime() - startTime;
        logger.info("Update cost response time in milliseconds : " + timeElapsed / 1000000);
        return Results.json().render(updateRes);
    }

    /**
     * getAllBooks fetches the book details from the database table and populates the hashmap
     */
    public void getAllBooks() {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("select * from book");
            bookMap = new HashMap<>();
            topicMap = new HashMap<>();
            List<Integer> distributedBooks = new ArrayList<>();
            List<Integer> graduateBooks = new ArrayList<>();
            while (rs.next()) {
                Book book = new Book(rs.getInt("book_number"), rs.getString("book_name"),
                        rs.getString("topic"), rs.getInt("cost"), rs.getInt("count"));
                bookMap.put(rs.getInt("book_number"), book);
                switch (rs.getString("topic")) {
                    case "distributed systems":
                        distributedBooks.add(rs.getInt("book_number"));
                        break;
                    case "graduate school":
                        graduateBooks.add(rs.getInt("book_number"));
                        break;
                }
            }
            topicMap.put("distributed systems", distributedBooks);
            topicMap.put("graduate school", graduateBooks);
            logger.info("Current inventory in library: " + bookMap);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * updateDB updates the count of book in database table after successful buy
     */
    public void updateDB(Integer bookNumber) {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("update book set count = count - 1 where book_number = " + bookNumber);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * restockBook updates the count of book in database to replenish the stock
     */
    public void restockBook(Integer bookNumber) {
        logger.info("Restocking book - " + bookNumber);
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("update book set count = 5 where book_number = " + bookNumber);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * updateCostInDb updates the cost of book in database
     */
    public void updateCostInDb(Integer bookNumber, Integer cost) {
        logger.info("Updating cost in DB for book - " + bookNumber);
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("update book set cost = " + cost + " where book_number = " + bookNumber);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }


    /**
     * setDBConnection creates the database connection to books.db
     */
    public void setDBConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            logger.info("first "+connection);
            if (connection == null) {
                connection = DriverManager.getConnection("jdbc:sqlite:books.db");
                logger.info("test"+connection);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

}
