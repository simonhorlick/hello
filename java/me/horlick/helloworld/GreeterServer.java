package me.horlick.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import java.io.IOException;
import me.dinowernli.grpc.prometheus.Configuration;
import me.dinowernli.grpc.prometheus.MonitoringServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GreeterServer {

  private static final Logger logger = LoggerFactory.getLogger(GreeterServer.class);

  private static final int port = 50051;

  private Server server;

  void start() throws IOException {
    MonitoringServerInterceptor monitoringInterceptor =
        MonitoringServerInterceptor.create(Configuration.allMetrics());

    server =
        ServerBuilder.forPort(port)
            .addService(ServerInterceptors.intercept(new GreeterService(), monitoringInterceptor))
            .build()
            .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GreeterServer.this.stop();
                System.err.println("*** server shut down");
              }
            });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }
}
