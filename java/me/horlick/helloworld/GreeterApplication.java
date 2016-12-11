package me.horlick.helloworld;

import java.io.IOException;

public class GreeterApplication {

  /** Launch the server from the command line. */
  public static void main(String[] args) throws IOException, InterruptedException {

    GreeterServer server = new GreeterServer();

    // We're ready so start the RPC server and block forever.
    server.start();
    server.blockUntilShutdown();
  }
}
