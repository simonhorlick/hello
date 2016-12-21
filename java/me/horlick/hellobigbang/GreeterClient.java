package me.horlick.hellobigbang;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.DnsNameResolverProvider;
import java.util.concurrent.TimeUnit;
import me.horlick.helloworld.GreeterGrpc;
import me.horlick.helloworld.HelloRequest;

public class GreeterClient {

  private final ManagedChannel channel;
  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  /** Construct client for accessing GreeterGrpc server at {@code host:port}. */
  GreeterClient(String host, int port) {
    this(
        ManagedChannelBuilder.forAddress(host, port)
            .nameResolverFactory(new DnsNameResolverProvider())
            .usePlaintext(true));
  }

  /** Construct client for accessing GreeterGrpc server using the existing channel. */
  private GreeterClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  void greet(String name) {
    blockingStub.sayHello(HelloRequest.newBuilder().setName(name).build());
  }

  void awaitTermination() {
    try {
      channel.awaitTermination(3650, TimeUnit.DAYS);
    } catch (InterruptedException ignored) {
    }
  }
}
