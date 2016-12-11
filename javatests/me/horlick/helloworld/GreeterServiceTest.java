package me.horlick.helloworld;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.grpc.stub.StreamObserver;
import org.junit.Test;

public class GreeterServiceTest {

  GreeterService service = new GreeterService();

  HelloRequest name = HelloRequest.newBuilder().setName("Simon").build();

  HelloReply reply = HelloReply.newBuilder().setMessage("hello, Simon!").build();

  @Test
  public void shouldSayHello() {
    StreamObserver<HelloReply> response = mock(StreamObserver.class);
    service.sayHello(name, response);

    verify(response).onNext(reply);
  }
}
