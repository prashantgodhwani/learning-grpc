package org.grpc.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.units.qual.C;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("gRPC Client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();



        //Unary
        //unaryCall(channel);

        //Server Streaming
        //serverStreamingCall(channel);

        //Client Stream
        clientStreamingCall(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private static void clientStreamingCall(ManagedChannel channel) throws InterruptedException {
        //create an async client
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
            @Override
            public void onNext(LongGreetResponse value) {
                //response from server
                System.out.println("Received a response");
                System.out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                //server responds with error
            }

            @Override
            public void onCompleted() {
                //server is done sending data;
                System.out.println("Server completed sending us data");
                latch.countDown();
            }
        });

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Prashant")
                        .build())
                .build());

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Godhwani")
                        .build())
                .build());

        requestObserver.onNext(LongGreetRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Jai Shree Ram")
                        .build())
                .build());

        //telling server that client is done sending data
        requestObserver.onCompleted();

        //wait till latch is 0 - which will be when server calls onComplete
        latch.await(3L, TimeUnit.SECONDS);
    }

    private static void unaryCall(ManagedChannel channel){
        //created a greet service client (blocking - sync)
        GreetServiceGrpc.GreetServiceBlockingStub syncGreetClient = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Prashant")
                .setLastName("Godhwani")
                .build();

        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        //call the Unary API
        GreetResponse greetResponse = syncGreetClient.greet(greetRequest);
        System.out.println(greetResponse.getResult());
    }

    private static void serverStreamingCall(ManagedChannel channel){
        //created a greet service client (blocking - sync)
        GreetServiceGrpc.GreetServiceBlockingStub syncGreetClient = GreetServiceGrpc.newBlockingStub(channel);

        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Prashant")
                .setLastName("Godhwani")
                .build();

        GreetManyTimesRequest greetRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        syncGreetClient.greetManyTimes(greetRequest)
                .forEachRemaining(greetManyTimesResponse -> System.out.println(greetManyTimesResponse.getResult()));
    }
}
