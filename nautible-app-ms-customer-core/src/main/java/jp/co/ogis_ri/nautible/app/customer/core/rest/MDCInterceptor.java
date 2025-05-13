package jp.co.ogis_ri.nautible.app.customer.core.rest;

import java.util.Optional;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.logging.MDC;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

/**
 * MDCに情報を設定するInterceptor。REST APIでの設定値は以下。
 * <ul>
 * <li>「x-request-id」:istioの分散トレーシングのrequest-id
 * <li>「url」:HttpのURL
 * <li>「method」:HttpMethod
 * </ul>
 */

@jp.co.ogis_ri.nautible.app.customer.core.rest.MDC
@Interceptor
public class MDCInterceptor {//implements ContainerRequestFilter, ContainerResponseFilter {

    @AroundInvoke
    Object logInvocation(InvocationContext context) throws Exception {
        RoutingContext routingContext = CDI.current().select(RoutingContext.class).get();
        HttpServerRequest request = routingContext.request();
        Optional.ofNullable(request.getHeader("x-request-id")).ifPresent(xRequestId -> MDC.put("x-request-id", xRequestId));
        Optional.ofNullable(request.path()).ifPresent(path -> MDC.put("url", path));
        Optional.ofNullable(request.method()).ifPresent(method -> MDC.put("method", method));
        Object ret = null;
        try {
            ret = context.proceed();
        } finally {
            MDC.clear();
        }
        return ret;
    }

}
