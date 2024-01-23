package org.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("gRPC Client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncCalculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

//        SumRequest sumRequest = SumRequest.newBuilder()
//                .setFirst(5)
//                .setSecond(10)
//                .build();
//
//        SumResponse response = syncCalculatorClient.sum(sumRequest);
//        System.out.println(response);

//        PrimeNumberDecompositionRequest primeNumberDecompositionRequest = PrimeNumberDecompositionRequest.newBuilder()
//                .setNum(18)
//                .build();
//
//        syncCalculatorClient.decomposeNumber(primeNumberDecompositionRequest)
//                .forEachRemaining(primeNumberDecompositionResponse -> System.out.println(primeNumberDecompositionResponse.getPrimeFactor()));

        CalculatorServiceGrpc.CalculatorServiceStub asyncCalculatorClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> computeAverageRequestStreamObserver = asyncCalculatorClient.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                System.out.println("Average = " + value);
            }

            @Override
            public void onError(Throwable t) {
                //do nothing
            }

            @Override
            public void onCompleted() {
                System.out.println("Server completed sending us data");
                latch.countDown();
            }
        });

        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNum(5).build());
        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNum(18).build());
        computeAverageRequestStreamObserver.onNext(ComputeAverageRequest.newBuilder().setNum(10).build());

        computeAverageRequestStreamObserver.onCompleted();

        latch.await(3L, TimeUnit.SECONDS);

        channel.shutdown();
    }
}
