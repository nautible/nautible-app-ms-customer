package jp.co.ogis_ri.nautible.app.customer.inbound.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jp.co.ogis_ri.nautible.app.customer.api.rest.RestCreateCustomerRequest;
import jp.co.ogis_ri.nautible.app.customer.api.rest.RestCustomerService;
import jp.co.ogis_ri.nautible.app.customer.api.rest.RestUpdateCustomerRequest;
import jp.co.ogis_ri.nautible.app.customer.core.rest.MDC;
import jp.co.ogis_ri.nautible.app.customer.domain.Customer;
import jp.co.ogis_ri.nautible.app.customer.domain.CustomerService;

/**
 * REST APIの顧客サービス。REST APIのエンドポイント。
 */
@MDC
public class RestCustomerServiceImpl implements RestCustomerService {

    @Inject
    CustomerService service;
    @Inject
    RestCustomerMapper mapper;

    Logger LOG = Logger.getLogger(RestCustomerServiceImpl.class.getName());

    @Override
    public Response getByCustomerId(@PathParam("customerId") Integer customerId) {
        Customer customer = service.getByCustomerId(customerId);
        return customer == null ? Response.status(Status.NOT_FOUND).build()
                : Response.ok(mapper.customerToRestGetByCustomerIdResponse(customer)).build();
    }

    @Override
    public Response find(@QueryParam("name") String name, @QueryParam("nameStartWith") String nameStartWith,
            @QueryParam("age") Integer age) {
        List<Customer> customer = service.find(name, nameStartWith, age);
        return (customer == null || customer.isEmpty()) ? Response.status(Status.NOT_FOUND).build()
                : Response.ok(mapper.customerToRestFindCustomerResponse(customer)).build();
    }

    @Override
    public Response create(@Valid @NotNull RestCreateCustomerRequest customer) {
        Customer customerRet = service.create(mapper.restCreateCustomerRequestToCustomer(customer));
        return customerRet == null ? Response.status(Status.NOT_FOUND).build()
                : Response.ok(mapper.customerToRestCreateCustomerResponse(customerRet)).build();
    }

    @Override
    public Response update(@Valid @NotNull RestUpdateCustomerRequest customer) {
        Customer customerRet = service.update(mapper.restUpdateCustomerRequestToCustomer(customer));
        return customerRet == null ? Response.status(Status.NOT_FOUND).build()
                : Response.ok(mapper.customerToRestUpdateCustomerResponse(customerRet)).build();
    }

    @Override
    public Response delete(@PathParam("customerId") Integer customerId) {
        service.deleteByCustomerId(customerId);
        return Response.status(Status.NO_CONTENT).build();
    }

}
