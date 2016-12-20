package me.horlick.helloworld;

import com.google.common.net.HostAndPort;
import io.prometheus.client.CollectorRegistry;
import java.io.IOException;
import me.horlick.metrics.MetricsServer;

public class GreeterApplication {

  /** Launch the server from the command line. */
  public static void main(String[] args) throws IOException, InterruptedException {
    MetricsServer metrics = null;

    try {
      metrics = new MetricsServer(CollectorRegistry.defaultRegistry, 9090);
      metrics.start();

      // Connect to the db.
      GreetingsRepositoryMySQL db =
          GreetingsRepositoryMySQL.create(HostAndPort.fromString("vtgate-asiaeast1c:15991"));
      GreeterServer server = new GreeterServer(db);

      // We're ready so start the RPC server and block forever.
      server.start();
      server.blockUntilShutdown();
    } finally {
      if (metrics != null) {
        metrics.shutdown();
      }
    }
  }
}
