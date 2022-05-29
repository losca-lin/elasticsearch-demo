package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author Losca
 * @date 2022/5/27 11:16
 */
public class HotelDSLTest {

    private RestHighLevelClient client;
    @Test
    public void test() {
        System.out.println(client);
    }

    @Test
    public void searchAll() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        handlerResponse(request);
    }
    
    @Test
    public void searchMatch() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        handlerResponse(request);
    }

    //布尔查询
    @Test
    public void searchBool() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termQuery("city", "上海"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(500));
        request.source().query(boolQueryBuilder);
        handlerResponse(request);
    }

    //排序
    @Test
    public void pageAndSort() throws IOException {
        int pageNum = 1;
        int pageSize = 5;
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from((pageNum - 1)*pageSize).size(5).sort("price", SortOrder.ASC);
        handlerResponse(request);
    }

    //结果高亮
    @Test
    public void highLight() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        //高亮
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        handlerResponse(request);
    }
    //文档聚合
    @Test
    public void aggsTest() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms("brandAggs").field("brand").size(20));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms brandTerms = aggregations.get("brandAggs");
        for (Terms.Bucket bucket : brandTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();
            System.out.println(brandName);
        }

    }

    //自动补全
    @Test
    public void suggestionTest() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().suggest(new SuggestBuilder().addSuggestion("title_suggest"
                ,SuggestBuilders.completionSuggestion("suggestion").prefix("stj")
                        .skipDuplicates(true)
                        .size(10)));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Suggest suggest = response.getSuggest();
        CompletionSuggestion suggestion = suggest.getSuggestion("title_suggest");
        for (CompletionSuggestion.Entry.Option option : suggestion.getOptions()) {
            String text = option.getText().string();
            System.out.println(text);
        }

    }


    private void handlerResponse(SearchRequest request) throws IOException {
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        long value = search.getHits().getTotalHits().value;
        System.out.println("总条数："+value);
        for (SearchHit hit : search.getHits().getHits()) {
            String sourceAsString = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightField = highlightFields.get("name");
                String name = highlightField.getFragments()[0].string();
                hotelDoc.setName(name);
            }
            System.out.println("hotelDoc:"+hotelDoc);
        }
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
