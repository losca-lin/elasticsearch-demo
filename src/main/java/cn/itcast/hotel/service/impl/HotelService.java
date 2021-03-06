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
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
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
        //???????????? ??????????????????
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //???????????????????????????
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
        //??????
        //request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        request.source().query(boolQueryBuilder);
        request.source().from((page - 1) * size).size(size);
        //if (!sortBy.equals("default")){
        //    request.source().sort(sortBy, SortOrder.ASC);
        //}
        //????????????
        if (StringUtils.isNotBlank(location)){
            request.source().sort(SortBuilders
                    .geoDistanceSort("location", new GeoPoint(location)).order(SortOrder.ASC)
                    .unit(DistanceUnit.KILOMETERS));
        }
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(QueryBuilders.functionScoreQuery(boolQueryBuilder, new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                        //????????????
                        QueryBuilders.termQuery("isAD", true),
                        //????????????
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
            //??????????????????
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
