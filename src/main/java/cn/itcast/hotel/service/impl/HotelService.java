package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.Page;
import cn.itcast.hotel.pojo.RequestParam;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
    @Autowired
    private RestHighLevelClient client;

    @Override
    public Page list(RequestParam requestParam) {
        try {
            SearchRequest request = new SearchRequest("hotel");
            hanlerSearch(requestParam, request);
            return handlerResponse(request);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, List<String>> filters(RequestParam requestParam) {
        try {
            SearchRequest request = new SearchRequest("hotel");
            Map<String, List<String>> res = new HashMap<>();
            //限定聚合范围
            hanlerSearch(requestParam, request);
            List<String> brandList = basicAggs(request,"brandAgge","brand");
            List<String> cityList = basicAggs(request,"cityAgge","city");
            List<String> starList = basicAggs(request,"starAgge","starName");
            res.put("brand", brandList);
            res.put("city", cityList);
            res.put("starName", starList);
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> basicAggs(SearchRequest request,String aggsName,String field) throws IOException {
        List<String> list = new ArrayList<>();
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms(aggsName).field(field).size(10));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms brandTerms = aggregations.get(aggsName);

        for (Terms.Bucket bucket : brandTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();
            list.add(brandName);
        }
        return list;
    }

    private void hanlerSearch(RequestParam requestParam, SearchRequest request) {
        String key = requestParam.getKey();
        int page = requestParam.getPage();
        int size = requestParam.getSize();
        String city = requestParam.getCity();
        String starName = requestParam.getStarName();
        String brand = requestParam.getBrand();
        Integer minPrice = requestParam.getMinPrice();
        Integer maxPrice = requestParam.getMaxPrice();
        String sortBy = requestParam.getSortBy();
        String location = requestParam.getLocation();
        //复合查询 构建布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //搜索关键词参与算分
        if (StringUtils.isNotBlank(key)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", key));
        } else {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        if (StringUtils.isNotBlank(city)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("city", city));
        }
        if (StringUtils.isNotBlank(starName)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("starName", starName));
        }
        if (StringUtils.isNotBlank(brand)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", brand));
        }
        if (minPrice != null && maxPrice != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(minPrice).lte(maxPrice));
        }
        //高亮
        //request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        request.source().query(boolQueryBuilder);
        request.source().from((page - 1) * size).size(size);
        //if (!sortBy.equals("default")){
        //    request.source().sort(sortBy, SortOrder.ASC);
        //}
        //位置排序
        if (StringUtils.isNotBlank(location)){
            request.source().sort(SortBuilders
                    .geoDistanceSort("location", new GeoPoint(location)).order(SortOrder.ASC)
                    .unit(DistanceUnit.KILOMETERS));
        }
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(QueryBuilders.functionScoreQuery(boolQueryBuilder, new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        //过滤条件
                        QueryBuilders.termQuery("isAD", true),
                        //算分函数
                        ScoreFunctionBuilders.weightFactorFunction(10)
                )
        }));
        request.source().query(functionScoreQuery);
    }

    private Page handlerResponse(SearchRequest request) throws IOException {
        Page page = new Page();
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        long value = search.getHits().getTotalHits().value;
        page.setTotal(value);
        List<HotelDoc> hotels = new ArrayList<>();
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
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length != 0){
                hotelDoc.setDistance(sortValues[0]);
            }
            hotels.add(hotelDoc);
        }
        page.setHotels(hotels);
        return page;
    }
}
