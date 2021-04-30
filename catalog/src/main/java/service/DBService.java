package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;

@Singleton
public class DBService {

    private static Connection connection;

    private static Logger logger = LoggerFactory.getLogger("Pygmy");

    /**
     * setDBConnection creates the database connection to books.db
     */
    public static void setDBConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            if (connection == null) {
                connection = DriverManager.getConnection("jdbc:sqlite:books.db");
                logger.info("test" + connection);
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            setDBConnection();
        }
        return connection;
    }


}
