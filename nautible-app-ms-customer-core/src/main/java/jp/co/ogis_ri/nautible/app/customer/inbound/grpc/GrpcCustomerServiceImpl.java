package jp.co.ogis_ri.nautible.app.customer.inbound.grpc;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

//import org.eclipse.microprofile.opentracing.Traced;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.CustomerServiceGrpc;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.Empty;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcCreateCustomerRequest;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcCreateCustomerResponse;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcDeleteByCustomerIdRequest;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcFindCustomerRequest;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcFindCustomerResponse;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcGetByCustomerIdRequest;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcGetByCustomerIdResponse;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcUpdateCustomerRequest;
import jp.co.ogis_ri.nautible.app.customer.api.grpc.GrpcUpdateCustomerResponse;
import jp.co.ogis_ri.nautible.app.customer.domain.Customer;
import jp.co.ogis_ri.nautible.app.customer.domain.CustomerService;

/**
 * gRPCのサービス。gRPCのエンドポイント。
 */
@Singleton
@GrpcService
public class GrpcCustomerServiceImpl extends CustomerServiceGrpc.CustomerServiceImplBase {

    @Inject
    CustomerService service;
    @Inject
    GrpcCustomerMapper mapper;

    Logger LOG = Logger.getLogger(GrpcCustomerServiceImpl.class.getName());

    @Override
    public void getByCustomerId(GrpcGetByCustomerIdRequest request,
            StreamObserver<GrpcGetByCustomerIdResponse> responseObserver) {
        int customerId = request.getCustomerId();

        Customer customer = service.getByCustomerId(customerId);
        if (customer == null) {
            responseObserver.onNext(GrpcGetByCustomerIdResponse.newBuilder().build());
        } else {
            responseObserver.onNext(GrpcGetByCustomerIdResponse.newBuilder()
                    .setCustomer(mapper.customerToGrpcCustomer(customer)).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteByCustomerId(GrpcDeleteByCustomerIdRequest request, StreamObserver<Empty> responseObserver) {
        service.deleteByCustomerId(request.getCustomerId());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void create(GrpcCreateCustomerRequest request, StreamObserver<GrpcCreateCustomerResponse> responseObserver) {
        Customer customer = service.create(mapper.grpcCustomerToCustomer(request.getCustomer()));
        responseObserver
                .onNext(GrpcCreateCustomerResponse.newBuilder().setCustomer(mapper.customerToGrpcCustomer(customer))
                        .build());
        responseObserver.onCompleted();
    }

    @Override
    public void update(GrpcUpdateCustomerRequest request, StreamObserver<GrpcUpdateCustomerResponse> responseObserver) {
        Customer customer = service.update(mapper.grpcCustomerToCustomer(request.getCustomer()));
        responseObserver
                .onNext(GrpcUpdateCustomerResponse.newBuilder().setCustomer(mapper.customerToGrpcCustomer(customer))
                        .build());
        responseObserver.onCompleted();
    }

    @Override
    public void find(GrpcFindCustomerRequest request, StreamObserver<GrpcFindCustomerResponse> responseObserver) {
        List<Customer> customer = service.find(request.getName(), request.getNameStartWith(), request.getAge());
        responseObserver.onNext(
                GrpcFindCustomerResponse.newBuilder().addAllCustomers(mapper.customerToGrpcCustomer(customer)).build());
        responseObserver.onCompleted();
    }

}
