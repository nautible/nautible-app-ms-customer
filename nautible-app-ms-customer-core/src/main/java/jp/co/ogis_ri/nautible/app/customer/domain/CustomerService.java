package jp.co.ogis_ri.nautible.app.customer.domain;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * 顧客サービス
 */
@ApplicationScoped
public class CustomerService {

    @Inject
    Instance<CustomerRepository> customerRepository;

    /**
     * 顧客を取得する
     * @param id Id
     * @return {@link Customer}
     */
    public Customer getByCustomerId(int id) {
        return customerRepository.get().getByCustomerId(id);
    }

    /**
     * 顧客を検索する
     * @param name 名前
     * @param nameStartWith 名前（前方一致）
     * @param age 年齢
     * @return 顧客のリスト
     */
    public List<Customer> find(String name, String nameStartWith, Integer age) {
        return customerRepository.get().find(name, nameStartWith, age);
    }

    /**
     * 顧客を作成する
     * @param customer 顧客
     * @return {@link Customer}
     */
    public Customer create(Customer customer) {
        return customerRepository.get().createCustomer(customer);
    }

    /**
     * 顧客を更新する
     * @param customer 顧客
     * @return {@link Customer}
     */
    public Customer update(Customer customer) {
        return customerRepository.get().updateCustomer(customer);
    }

    /**
     * 顧客を削除する
     * @param id Id
     */
    public void deleteByCustomerId(int id) {
        customerRepository.get().deleteByCustomerId(id);
    }

}
