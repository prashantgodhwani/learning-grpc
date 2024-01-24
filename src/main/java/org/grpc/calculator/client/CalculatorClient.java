package org.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("gRPC Client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        //unaryCall(channel);

        //serverStreamingCall(channel);

        //clientStreamingCall(channel);

        //biDirectionalStreamingCall(channel);

        squareRootErrorHandlingCall(channel);

        channel.shutdown();
    }

    private static void squareRootErrorHandlingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncCalculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        try{
            syncCalculatorClient.squareRoot(SquareRootRequest.newBuilder().setNum(-1).build());
        }catch (StatusRuntimeException exception){
            System.out.println(exception.getStatus() + " : " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private static void unaryCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncCalculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        SumRequest sumRequest = SumRequest.newBuilder()
        .setFirst(5)
        .setSecond(10)
        .build();

        SumResponse response = syncCalculatorClient.sum(sumRequest);
        System.out.println(response);
    }

    private static void serverStreamingCall(ManagedChannel channel){
        CalculatorServiceGrpc.CalculatorServiceBlockingStub syncCalculatorClient = CalculatorServiceGrpc.newBlockingStub(channel);

        PrimeNumberDecompositionRequest primeNumberDecompositionRequest = PrimeNumberDecompositionRequest.newBuilder()
        .setNum(18)
        .build();

        syncCalculatorClient.decomposeNumber(primeNumberDecompositionRequest)
                .forEachRemaining(primeNumberDecompositionResponse -> System.out.println(primeNumberDecompositionResponse.getPrimeFactor()));
    }
    private static void clientStreamingCall(ManagedChannel channel) throws InterruptedException {
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
    }

    private static void biDirectionalStreamingCall(ManagedChannel channel) throws InterruptedException {
        CalculatorServiceGrpc.CalculatorServiceStub asyncCalculatorClient = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<FindMaximumRequest> computeAverageRequestStreamObserver =
                asyncCalculatorClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
                    @Override
                    public void onNext(FindMaximumResponse value) {
                        System.out.println("Max Now = " + value.getMax());
                    }

                    @Override
                    public void onError(Throwable t) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Server completed sending us data");
                        countDownLatch.countDown();
                    }
                });

        Arrays.asList(10, 8, 100, 158).forEach(number ->
                computeAverageRequestStreamObserver.onNext(FindMaximumRequest.newBuilder()
                        .setNum(number).build()));

        computeAverageRequestStreamObserver.onCompleted();

        countDownLatch.await(3L, TimeUnit.SECONDS);
    }
}
