package me.horlick.helloworld;

public interface GreetingsRepository {
    void insertGreet(String name, long timeNs);

    NameHistogram nameHistogram();
}
