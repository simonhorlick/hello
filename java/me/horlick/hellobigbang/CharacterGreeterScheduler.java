package me.horlick.hellobigbang;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CharacterGreeterScheduler {
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
  private final GreeterClient client;
  private final List<String> names;

  static final Logger logger = LoggerFactory.getLogger(CharacterGreeterScheduler.class);

  CharacterGreeterScheduler(GreeterClient client, List<String> names) {
    this.client = client;
    this.names = names;
  }

  void start() {
    Runnable task =
        new Runnable() {
          public void run() {
            String name = names.get((int) (Math.random() * names.size()));
            logger.info("Inserting {}", name);
            client.greet(name);
          }
        };
    scheduler.scheduleAtFixedRate(task, 0, 50, MILLISECONDS);
  }
}
