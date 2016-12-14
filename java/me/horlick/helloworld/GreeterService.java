package me.horlick.helloworld;

import io.grpc.stub.StreamObserver;

// A GreeterService implements the Greeter rpc interface by echoing back a
// greeting in English to the supplied name.
class GreeterService extends GreeterGrpc.GreeterImplBase {

  private final GreetingsRepository greetingsRepository;

  GreeterService(GreetingsRepository greetingsRepository) {
    this.greetingsRepository = greetingsRepository;
  }

  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    // Record this greeting.
    greetingsRepository.insertGreet(request.getName(), System.nanoTime());

    responseObserver.onNext(
        HelloReply.newBuilder().setMessage(String.format("hello, %s!", request.getName())).build());
    responseObserver.onCompleted();
  }

  @Override
  public void getNames(GetNamesRequest request, StreamObserver<NameHistogram> responseObserver) {
    responseObserver.onNext(greetingsRepository.nameHistogram());
    responseObserver.onCompleted();
  }
}
