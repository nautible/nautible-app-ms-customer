package jp.co.ogis_ri.nautible.app.customer.outbound.dynamodb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;

import jp.co.ogis_ri.nautible.app.customer.domain.Customer;
import jp.co.ogis_ri.nautible.app.customer.domain.CustomerRepository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

/**
 * 顧客レポジトリの実装クラス
 */
@Named("DynamodbCustomerRepositoryImpl")
@ApplicationScoped
public class DynamodbCustomerRepositoryImpl implements CustomerRepository {
    /** 顧客テーブル名 */
    private static final String CUSTOMER_TABLE_NAME = "Customer";

    @Inject
    DynamoDbClient dynamoDBClient;

    @Inject
    DynamodbCustomerMapper dynamodbCustomerMapper;

    @Override
    public Customer getByCustomerId(int customerId) {
        Key key = Key.builder().partitionValue(customerId).build();
        jp.co.ogis_ri.nautible.app.customer.outbound.dynamodb.DynamodbCustomer result = getDynamoDbTable()
                .getItem(r -> r.key(key));
        return dynamodbCustomerMapper.dynamodbCustomerToCustomer(result);
    }

    @Override
    public List<Customer> find(String name, String nameStartWith, Integer age) {
        // full scan
        return dynamodbCustomerMapper
                .dynamodbCustomerToCustomer(getDynamoDbTable().scan().items().stream().filter(customerItem -> {
                    if (StringUtils.isNotEmpty(name) && StringUtils.equals(name, customerItem.getName()) == false) {
                        return false;
                    }
                    if (StringUtils.isNotEmpty(nameStartWith)
                            && StringUtils.startsWith(customerItem.getName(), nameStartWith) == false) {
                        return false;
                    }
                    if (age != null) {
                        if (customerItem.getAge() == null || customerItem.getAge().compareTo(age) != 0) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList()));
    }

    @Override
    public Customer createCustomer(Customer customer) {
        int sequence = getSequenceNumber("Customer");
        customer.setId(sequence);
        getDynamoDbTable().putItem(dynamodbCustomerMapper.customerToDynamodbCustomer(customer));
        return customer;
    }

    @Override
    public void deleteByCustomerId(int customerId) {
        Key key = Key.builder().partitionValue(customerId).build();
        getDynamoDbTable().deleteItem(key);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        getDynamoDbTable().updateItem(dynamodbCustomerMapper.customerToDynamodbCustomer(customer));
        return customer;
    }

    /**
     * Dyanmodbのアトミックカウンタでシーケンスを発番する。
     * @param tableName テーブル名
     * @return シーケンス
     */
    private int getSequenceNumber(String tableName) {
        // 本来はマイクロサービスの管理単位を跨ぐような（データベースを跨ぐような）DBアクセスは禁止。作業簡略化のためCommonDBへのアクセスを行う。
        // 時間ができたら共通サービスを作成して発番機能を作る。
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("Name", AttributeValue.builder().s(tableName).build());
        Map<String, AttributeValueUpdate> update = new HashMap<>();
        update.put("SequenceNumber", AttributeValueUpdate.builder().value(AttributeValue.builder().n("1").build())
                .action(AttributeAction.ADD).build());
        UpdateItemRequest updateRequest = UpdateItemRequest.builder().tableName("Sequence").key(key)
                .attributeUpdates(update).returnValues(ReturnValue.UPDATED_NEW).build();
        UpdateItemResponse updateResponse = dynamoDBClient.updateItem(updateRequest);
        return Integer.parseInt(updateResponse.attributes().get("SequenceNumber").n());
    }

    /**
     * 顧客のDynamoDbTableを取得する
     * @return {@link DynamoDbTable}
     */
    private DynamoDbTable<DynamodbCustomer> getDynamoDbTable() {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDBClient).build();
        DynamoDbTable<DynamodbCustomer> mappedTable = enhancedClient.table(CUSTOMER_TABLE_NAME,
                TableSchema.fromBean(DynamodbCustomer.class));
        return mappedTable;
    }

}
