package service;

import com.google.inject.Inject;
import ninja.scheduler.Schedule;
import ninja.utils.NinjaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Singleton
public class HeartBeatService{
    Logger logger = LoggerFactory.getLogger("Pygmy");

    @Inject
    NinjaProperties ninjaProperties;

    @Schedule(delay = 2, initialDelay = 0, timeUnit = TimeUnit.SECONDS)
    public void sendHeartBeat(){
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
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
    }
}
