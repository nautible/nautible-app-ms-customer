package jp.co.ogis_ri.nautible.app.customer.outbound.cosmosdb;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jp.co.ogis_ri.nautible.app.customer.domain.Customer;
import jp.co.ogis_ri.nautible.app.customer.domain.CustomerRepository;

/**
 * 顧客レポジトリの実装クラス
 */
@Named("CosmosdbCustomerRepositoryImpl")
@ApplicationScoped
public class CosmosdbCustomerRepositoryImpl
        implements CustomerRepository, PanacheMongoRepositoryBase<CosmosdbCustomer, Integer> {
    /** 顧客テーブル名 */
    private static final String CUSTOMER_TABLE_NAME = "Customer";
    @Inject
    MongoClient mongoClient;
    @Inject
    CosmosdbCustomerMapper cosmosdbCustomerMapper;

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public Customer getByCustomerId(int customerId) {
        return cosmosdbCustomerMapper.cosmosdbCustomerToCustomer(
                (jp.co.ogis_ri.nautible.app.customer.outbound.cosmosdb.CosmosdbCustomer) find("_id", customerId)
                        .firstResult());
    }

    @Override
    public List<Customer> find(String name, String nameStartWith, Integer age) {
        StringBuilder query = new StringBuilder();
        Parameters parameters = Parameters.with("dummy", "dummy");
        if (StringUtils.isNotEmpty(name)) {
            query.append(", 'Name': :name");
            parameters = parameters.and("name", name);
        }
        if (StringUtils.isNotEmpty(nameStartWith)) {
            query.append(",'Name': {'$regex': :nameStartWith}");
            parameters = parameters.and("nameStartWith", "^" + nameStartWith);
        }
        if (age != null) {
            query.append(", 'Age': :age");
            parameters = parameters.and("age", age);
        }
        if (parameters.map().size() == 1) {
            return cosmosdbCustomerMapper.cosmosdbCustomerToCustomer(findAll().list());
        }
        List<CosmosdbCustomer> result = find(
                "{" + query.substring(1) + "}", parameters).list();
        return cosmosdbCustomerMapper.cosmosdbCustomerToCustomer(result);
    }

    @Override
    public Customer createCustomer(Customer customer) {
        customer.setId(getSequenceNumber(CUSTOMER_TABLE_NAME));
        persist(cosmosdbCustomerMapper.customerToCosmosdbCustomer(customer));
        return customer;
    }

    @Override
    public void deleteByCustomerId(int customerId) {
        delete("_id", customerId);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        update(cosmosdbCustomerMapper.customerToCosmosdbCustomer(customer));
        return customer;
    }

    /**
     * Mongodbのfunctionでシーケンスを発番する。
     * @param tableName テーブル名
     * @return シーケンス
     */
    private int getSequenceNumber(String tableName) {
        // 本来はマイクロサービスの管理単位を跨ぐような（データベースを跨ぐような）DBアクセスは禁止。
        // サービス毎にシーケンスCollectionを作成するとCosmosdbのコストが高くなる、また作業簡略化のためCommonDBへのアクセスを行う。
        // 時間ができたら共通サービスを作成して発番機能を作る。
        Document result = mongoClient.getDatabase("Common").getCollection("Sequence").findOneAndUpdate(
                Filters.eq("_id", tableName),
                new Document("$inc", new Document("SequenceNumber", 1)),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER));
        return result.getInteger("SequenceNumber");
    }
}
