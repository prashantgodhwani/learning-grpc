package org.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        Greeting greeting = request.getGreeting();
        String responseGreeting = "Hello " + greeting.getFirstName() + " " + greeting.getLastName();

        //create the response
        GreetResponse greetResponse = GreetResponse.newBuilder()
                .setResult(responseGreeting)
                .build();

        //send response
        responseObserver.onNext(greetResponse);

        //complete RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        Greeting greeting = request.getGreeting();
        String responseGreeting = "Hello " + greeting.getFirstName() + " " + greeting.getLastName();

        //create the response
        GreetManyTimesResponse greetResponse = GreetManyTimesResponse.newBuilder()
                .setResult(responseGreeting)
                .build();

        //send response
        try {
            for(int i = 0; i < 10; i++) {
                responseObserver.onNext(greetResponse);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //complete RPC call
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {

        //return implementation of how to respond to them - implementation to how we should react
        StreamObserver<LongGreetRequest> streamObserverRequest = new StreamObserver<>() {
            StringBuilder response = new StringBuilder();

            @Override
            public void onNext(LongGreetRequest value) {
                //client sends a message
                response.append("Hi, " + value.getGreeting().getFirstName() + " \n");
            }

            @Override
            public void onError(Throwable t) {
                //client sends an error
            }

            @Override
            public void onCompleted() {
                //client is done
                LongGreetResponse longGreetResponse = LongGreetResponse.newBuilder()
                        .setResult(response.toString())
                        .build();

                //send response
                responseObserver.onNext(longGreetResponse);
                responseObserver.onCompleted();
            }
        };

        return streamObserverRequest;
    }
}
