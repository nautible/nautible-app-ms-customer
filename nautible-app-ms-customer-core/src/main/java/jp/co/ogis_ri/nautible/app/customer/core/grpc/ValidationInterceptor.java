package jp.co.ogis_ri.nautible.app.customer.core.grpc;

import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * <p>gRPCリクエストのBeanValidationを実行するInterceptor。QuarkusでgRPCのvalidationをサポートする機能は無い。</p>
 * https://github.com/quarkusio/quarkus/issues/5531<br>
 * Quarkus管理のValidatorはxmlのvalidation定義が有効にならないため独自のインスタンスを利用する
 */
@ApplicationScoped
public class ValidationInterceptor implements ServerInterceptor {

    Validator validator;

    @PostConstruct
    public void postConstruct() {
        // https://github.com/quarkusio/quarkus/issues/5531
        // Quarkus管理のValidatorはxmlのvalidation定義が有効にならないため独自のインスタンスを利用する
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Override
    public <I, O> ServerCall.Listener<I> interceptCall(ServerCall<I, O> call, final Metadata requestHeaders,
            ServerCallHandler<I, O> next) {
        ServerCall.Listener<I> listeners = next.startCall(call, requestHeaders);
        return new SimpleForwardingServerCallListener<I>(listeners) {
            @Override
            public void onMessage(I message) {
                // リクエストメッセージのvalidationを実行
                Set<ConstraintViolation<I>> violations = validator.validate(message);
                if (!violations.isEmpty()) {
                    Status status = Status.INVALID_ARGUMENT
                            .withDescription(new ConstraintViolationException(violations).getMessage());
                    call.close(status, requestHeaders);
                } else {
                    super.onMessage(message);
                }
            }

        };

    }

}
