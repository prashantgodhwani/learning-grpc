package org.grpc.calculator.server;

import com.proto.calculator.*;
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
}
