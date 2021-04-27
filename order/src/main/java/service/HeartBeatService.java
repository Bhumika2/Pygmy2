package service;

import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HeartBeatService extends Thread{
    Logger logger = LoggerFactory.getLogger("Pygmy");
    private NinjaProperties ninjaProperties;
    public HeartBeatService(NinjaProperties ninjaProperties) {
        this.ninjaProperties = ninjaProperties;
    }

    @Override
    public void run(){
        while(true){
            try {
                logger.info("Sending heartbeat message to frontend cache");
                HttpClient client = HttpClient.newHttpClient();
                String serverName = ninjaProperties.get("frontendHost") + ":" + ninjaProperties.get("frontendPort");
                String server = "order"+System.getProperty("server.name");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://" + serverName + "/heartbeat/" + server))
                        .timeout(Duration.ofMinutes(1))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();
                client.send(request, HttpResponse.BodyHandlers.ofString());
                Thread.sleep(20000);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
    }
}
