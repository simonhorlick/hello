package me.horlick.helloworld;

import io.grpc.stub.StreamObserver;

// A GreeterService implements the Greeter rpc interface by echoing back a
// greeting in English to the supplied name.
class GreeterService extends GreeterGrpc.GreeterImplBase {
  @Override
  public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
    responseObserver.onNext(
        HelloReply.newBuilder().setMessage(String.format("hello, %s!", request.getName())).build());
    responseObserver.onCompleted();
  }
}
