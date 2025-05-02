package jp.co.ogis_ri.nautible.app.customer.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

import jp.co.ogis_ri.nautible.app.customer.domain.CustomerRepository;

/**
 * ヘルスチェック。 Dynamodbへの接続を検証する.{@link HealthCheck}。
 */
@Readiness
@ApplicationScoped
public class CustomerHealthCheck implements HealthCheck {

    Logger LOG = Logger.getLogger(CustomerHealthCheck.class.getName());
    @Inject
    Instance<CustomerRepository> customerRepository;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Dynamodb connection");

        try {
            customerRepository.get().getByCustomerId(Integer.MIN_VALUE);
            return builder.up().build();
        } catch (Throwable e) {
            LOG.log(Level.SEVERE, "Dynamodb connection error", e);
            return builder.down().build();
        }
    }

}
