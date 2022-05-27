package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.impl.HotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * @author Losca
 * @date 2022/5/27 11:16
 */
@SpringBootTest
public class HotelDocTest {

    private RestHighLevelClient client;

    @Autowired
    HotelService hotelService;

    //没有添加 有则修改
    @Test
    public void addDoc() throws IOException {
        Hotel hotel = hotelService.getById(36934);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    public void selDoc() throws IOException {
        GetRequest getSourceRequest = new GetRequest("hotel","36934");
        GetResponse source = client.get(getSourceRequest, RequestOptions.DEFAULT);
        String json = source.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    //增量修改
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest request = new UpdateRequest("hotel","36934");
        request.doc("price", "330",
                "starName","四钻");
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    public void delDoc() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel","36934");
        client.delete(request,RequestOptions.DEFAULT);
    }

    @Test
    public void bulkDoc() throws IOException {
        List<Hotel> hotelList = hotelService.list();
        BulkRequest request = new BulkRequest();
        for (Hotel hotel : hotelList) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel").id(hotelDoc.getId().toString()).source(JSON.toJSONString(hotelDoc),XContentType.JSON));
        }
        client.bulk(request, RequestOptions.DEFAULT);
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
