package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static cn.itcast.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

/**
 * @author Losca
 * @date 2022/5/27 11:16
 */
public class HotelIndexTest {

    private RestHighLevelClient client;
    @Test
    public void test() {
        System.out.println(client);
    }

    @Test
    public void createIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest("hotel");
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    public void DelIndex() throws IOException {

        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    public void selIndex() throws IOException {

        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists ?"存在":"不存在");
    }

    @BeforeEach
    public void before(){
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.142.202:9200")));
    }

    @AfterEach
    public void after() throws IOException {
        this.client.close();
    }
}
