package service;

import models.Book;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryService {

    private static Logger logger = LoggerFactory.getLogger("Pygmy");


    private static HashMap<Integer, Book> bookMap;
    private static HashMap<String, List<Integer>> topicMap;

    /**
     * getAllBooks fetches the book details from the database table and populates the hashmap
     */
    public static void getAllBooks(NinjaProperties ninjaProperties) {
        try {
            UpdateService updateService = new UpdateService(ninjaProperties);
            updateService.updateDB();
            Statement statement = DBService.getConnection().createStatement();
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

    public static HashMap<Integer, Book> getBookMap() {
        return bookMap;
    }


    public static HashMap<String, List<Integer>> getTopicMap() {
        return topicMap;
    }
}
