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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
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
        SearchRequest request = new SearchRequest("hotel");
        String key = requestParam.getKey();
        int page = requestParam.getPage();
        int size = requestParam.getSize();
        if (StringUtils.isNotBlank(key)){
            request.source().query(QueryBuilders.matchQuery("all",key));
        } else {
            request.source().query(QueryBuilders.matchAllQuery());
        }
        request.source().from((page - 1)*size).size(size);
        //高亮
        //request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        try {
            return handlerResponse(request);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
            hotels.add(hotelDoc);
        }
        page.setHotels(hotels);
        return page;
    }
}
