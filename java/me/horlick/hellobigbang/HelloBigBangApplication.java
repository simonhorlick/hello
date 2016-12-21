package me.horlick.hellobigbang;

import io.prometheus.client.CollectorRegistry;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import me.horlick.metrics.MetricsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloBigBangApplication {

  static final Logger logger = LoggerFactory.getLogger(HelloBigBangApplication.class);

  /** Launch the server from the command line. */
  public static void main(String[] args) throws IOException, InterruptedException {
    MetricsServer metrics = null;

    try {
      metrics = new MetricsServer(CollectorRegistry.defaultRegistry, 9090);
      metrics.start();

      GreeterClient client = new GreeterClient("greeter", 50051);

      List<String> names = Arrays.asList("Leonard", "Sheldon", "Penny", "Howard", "Raj");

      new CharacterGreeterScheduler(client, names).start();

      client.awaitTermination();

      logger.info("Shutting down client.");
    } finally {
      if (metrics != null) {
        metrics.shutdown();
      }
    }
  }
}
