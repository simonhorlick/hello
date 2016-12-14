package me.horlick.helloworld;

import com.google.common.base.Ticker;
import io.grpc.stub.StreamObserver;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GreeterServiceTest {

  GreetingsRepository greetingsRepo = mock(GreetingsRepository.class);

  Ticker ticker = mock(Ticker.class);

  GreeterService service = new GreeterService(greetingsRepo, ticker);

  HelloRequest name = HelloRequest.newBuilder().setName("Simon").build();

  HelloReply reply = HelloReply.newBuilder().setMessage("hello, Simon!").build();

  @Test
  public void shouldSayHello() {
    StreamObserver<HelloReply> response = mock(StreamObserver.class);
    service.sayHello(name, response);

    verify(response).onNext(reply);
  }

  @Test
  public void shouldPersistGreet() {
    // Fix time timestamp for this greet.
    when(ticker.read()).thenReturn(123L);

    StreamObserver<HelloReply> response = mock(StreamObserver.class);
    service.sayHello(name, response);

    verify(greetingsRepo).insertGreet("Simon", 123L);
  }
}
