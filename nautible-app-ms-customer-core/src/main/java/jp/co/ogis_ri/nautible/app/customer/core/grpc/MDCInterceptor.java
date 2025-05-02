package jp.co.ogis_ri.nautible.app.customer.core.grpc;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.MDC;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * MDCに情報を設定するInterceptor。gRPCでの設定値は以下。
 * <ul>
 * <li>「x-request-id」:istioの分散トレーシングのrequest-id
 * <li>「gRPC-methodd」:gRPCのサービス/メソッド
 * </ul>
 */
@ApplicationScoped
public class MDCInterceptor implements ServerInterceptor {

    @Override
    public <I, O> ServerCall.Listener<I> interceptCall(ServerCall<I, O> call, final Metadata requestHeaders,
            ServerCallHandler<I, O> next) {
        ServerCall.Listener<I> listeners = next.startCall(call, requestHeaders);
        return new SimpleForwardingServerCallListener<I>(listeners) {
            @Override
            public void onMessage(I message) {
                MDC.put("x-request-id",
                        requestHeaders.get(Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER)));
                String method = call.getMethodDescriptor().getFullMethodName();
                MDC.put("gRPC-method", method);
                super.onMessage(message);
            }

            @Override
            public void onComplete() {
                super.onComplete();
                MDC.clear();
            }

        };

    }
}
