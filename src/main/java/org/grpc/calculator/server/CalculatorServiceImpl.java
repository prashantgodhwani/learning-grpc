package org.grpc.calculator.server;

import com.proto.calculator.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        SumResponse sumResponse = SumResponse.newBuilder()
                .setSum(request.getFirst() + request.getSecond())
                .build();

        responseObserver.onNext(sumResponse);

        responseObserver.onCompleted();
    }

    @Override
    public void decomposeNumber(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {

        int prime = 2;
        int number = request.getNum();

        while(number > 1){
            if(number % prime == 0){
                responseObserver.onNext(PrimeNumberDecompositionResponse.newBuilder()
                        .setPrimeFactor(prime)
                        .build());
                number /= prime;
            }else{
                prime++;
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        StreamObserver<ComputeAverageRequest> computeAverageRequestStreamObserver = new StreamObserver<ComputeAverageRequest>() {
            double sum = 0;
            int count = 0;

            @Override
            public void onNext(ComputeAverageRequest value) {
                sum += value.getNum();
                count++;
            }

            @Override
            public void onError(Throwable t) {
                //do nothing
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(ComputeAverageResponse.newBuilder()
                        .setAverage(sum / count)
                        .build());
                responseObserver.onCompleted();
            }
        };

        return computeAverageRequestStreamObserver;
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<>() {
            int max = Integer.MIN_VALUE;
            @Override
            public void onNext(FindMaximumRequest value) {
                max = Math.max(value.getNum(), max);
                responseObserver.onNext(FindMaximumResponse.newBuilder()
                        .setMax(max)
                        .build());
            }

            @Override
            public void onError(Throwable t) {
                //no nothing
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        int number = request.getNum();

        if(number >= 0){
            responseObserver.onNext(SquareRootResponse.newBuilder()
                    .setSquareRoot((int) Math.sqrt(number))
                    .build());
            responseObserver.onCompleted();
        }else{
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Number is negative")
                    .augmentDescription("number : " + number)
                    .asRuntimeException());
        }
    }
}
