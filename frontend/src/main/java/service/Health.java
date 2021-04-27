package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

public class Health {

    private static LocalDateTime Catalog1Health = LocalDateTime.now();
    private static LocalDateTime Catalog2Health = LocalDateTime.now();
    private static LocalDateTime Order1Health = LocalDateTime.now();
    private static LocalDateTime Order2Health = LocalDateTime.now();

    private static String Catalog1State = "RUNNING";
    private static String Catalog2State = "RUNNING";
    private static String Order1State = "RUNNING";
    private static String Order2State = "RUNNING";

    Logger logger = LoggerFactory.getLogger("Pygmy");

    public static String getCatalog1State() {
        return Catalog1State;
    }

    public static String getCatalog2State() {
        return Catalog2State;
    }

    public static String getOrder1State() {
        return Order1State;
    }

    public static String getOrder2State() {
        return Order2State;
    }

    public void updateHealth(String serverName) {
        logger.info("Heartbeat message received from - " + serverName);
        switch (serverName) {
            case "catalog1":
                Catalog1Health = LocalDateTime.now();
                break;
            case "catalog2":
                Catalog2Health = LocalDateTime.now();
                break;
            case "order1":
                Order1Health = LocalDateTime.now();
                break;
            case "order2":
                Order2Health = LocalDateTime.now();
                break;
        }
    }

    public void checkSystemHealth() {
        try {
            long WAIT_DURATION = 3;
            while (true) {
                Thread.sleep(3000);
                LocalDateTime timeNow = LocalDateTime.now();
                if (Duration.between(Catalog1Health, timeNow).toSeconds() > WAIT_DURATION && Catalog1State.equals("RUNNING")) {
                    Catalog1State = "FAILED";
                    logger.error("Catalog 1 failed!");
                } else if (Duration.between(Catalog1Health, timeNow).toSeconds() < WAIT_DURATION && Catalog1State.equals("FAILED")) {
                    Catalog1State = "RUNNING";
                    logger.info("Catalog 1 started again!");
                }
                if (Duration.between(Catalog2Health, timeNow).toSeconds() > WAIT_DURATION && Catalog2State.equals("RUNNING")) {
                    Catalog2State = "FAILED";
                    logger.error("Catalog 2 failed!");
                } else if (Duration.between(Catalog2Health, timeNow).toSeconds() < WAIT_DURATION && Catalog2State.equals("FAILED")) {
                    Catalog2State = "RUNNING";
                    logger.info("Catalog 2 started again!");
                }
                if (Duration.between(Order1Health, timeNow).toSeconds() > WAIT_DURATION && Order1State.equals("RUNNING")) {
                    Order1State = "FAILED";
                    logger.error("Order 1 failed!");
                } else if (Duration.between(Order1Health, timeNow).toSeconds() < WAIT_DURATION && Order1State.equals("FAILED")) {
                    Order1State = "RUNNING";
                    logger.info("Order 1 started again!");
                }
                if (Duration.between(Order2Health, timeNow).toSeconds() > WAIT_DURATION && Order2State.equals("RUNNING")) {
                    Order2State = "FAILED";
                    logger.error("Order 2 failed!");
                } else if (Duration.between(Order2Health, timeNow).toSeconds() < WAIT_DURATION && Order2State.equals("FAILED")) {
                    Order2State = "RUNNING";
                    logger.info("Order 2 started again!");
                }

            }
        } catch (Exception e) {
            logger.error("Error in checking health - " + e.getMessage());
        }
    }


}
