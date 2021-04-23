import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Random;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;


/**
 * client makes http requests:lookup, search and buy at random with random parameters to frontend server
 * prints the response from microservices
 */
public class Client {
    private static final String[] ACTIONS = {"lookup", "search", "buy"};
    private static final String[] TOPICS = {"distributed systems", "graduate school"};

    public static void main(String[] args) {
        try {
            File configFile = new File("../hostname.conf");
            Properties prop = new Properties();
            FileReader reader = new FileReader(configFile);

            if (reader != null) {
                prop.load(reader);
            } else {
                System.out.println("property file 'config.properties' not found in the classpath");
                return;
            }

            //String frontendHostName = prop.getProperty("frontendHost");
            String frontendHostName = "localhost";

            while (true) {
                String action = getRandomAction();
                System.out.println("\n");
                if (action.equals("lookup")) {
                    int id = getRandomNumber();
                    System.out.println("[LOOKUP] Requesting details of book: " + id);
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://" + frontendHostName + ":8080/lookup/" + id))
                                .timeout(Duration.ofMinutes(1))
                                .header("Content-Type", "application/json")
                                .GET()
                                .build();
                        long startTime = System.nanoTime();
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        long timeElapsed = System.nanoTime() - startTime;
                        System.out.println("[LOOKUP] Response time in milliseconds : " + timeElapsed / 1000000);
                        if (response.statusCode() != 200) {
                            System.out.println("[LOOKUP] Non 200 response code received: " + response.statusCode());
                        }
                        System.out.println("[LOOKUP] Response: " + response.body().toString());
                    } catch (Exception e) {
                        System.out.println("[LOOKUP] Error: " + e.getMessage());
                    }
                } else if (action.equals("search")) {
                    String topic = getRandomTopic();
                    System.out.println("[SEARCH] Requesting topic details: " + topic);
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        String restUrl = URLEncoder.encode(topic, StandardCharsets.UTF_8.toString());
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://" + frontendHostName + ":8080/search/" + restUrl))
                                .timeout(Duration.ofMinutes(1))
                                .header("Content-Type", "application/json")
                                .GET()
                                .build();
                        long startTime = System.nanoTime();
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        long timeElapsed = System.nanoTime() - startTime;
                        System.out.println("[SEARCH] Response time in milliseconds : " + timeElapsed / 1000000);
                        if (response.statusCode() != 200) {
                            System.out.println("[SEARCH] Non 200 response code received: " + response.statusCode());
                        }
                        System.out.println("[SEARCH] Response: " + response.body().toString());
                    } catch (Exception e) {
                        System.out.println("[SEARCH] Error: " + e.getMessage());
                    }
                } else if (action.equals("buy")) {
                    int id = getRandomNumber();
                    System.out.println("[BUY] Buy Request for book: " + id);
                    try {
                        HttpClient client = HttpClient.newHttpClient();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create("http://" + frontendHostName + ":8080/buy/" + id))
                                .timeout(Duration.ofMinutes(1))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.noBody())
                                .build();
                        long startTime = System.nanoTime();
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        long timeElapsed = System.nanoTime() - startTime;
                        System.out.println("[BUY] Response time in milliseconds : " + timeElapsed / 1000000);
                        if (response.statusCode() != 200) {
                            System.out.println("[BUY] Non 200 response code received: " + response.statusCode());
                        }
                        System.out.println("[BUY] Response: " + response.body().toString());
                    } catch (Exception e) {
                        System.out.println("[BUY] Error: " + e.getMessage());
                    }
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("[CLIENT] Error: " + e.getMessage());
        }

    }

    public static String getRandomAction() {
        Random random = new Random();
        int randomNum = random.nextInt(ACTIONS.length);
        return ACTIONS[randomNum];
    }

    public static int getRandomNumber() {
        Random random = new Random();
        int randomNum = random.nextInt(4) + 1;
        return randomNum;
    }

    public static String getRandomTopic() {
        Random random = new Random();
        int randomNum = random.nextInt(TOPICS.length);
        return TOPICS[randomNum];
    }
}
