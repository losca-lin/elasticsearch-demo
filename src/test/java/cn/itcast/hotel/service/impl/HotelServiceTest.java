package cn.itcast.hotel.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Losca
 * @date 2022/5/29 13:49
 */
@SpringBootTest
public class HotelServiceTest {

    @Autowired
    HotelService hotelService;

    @Test
    void filter() {
        //Map<String, List<String>> filter = hotelService.filters();
        //System.out.println(filter);
    }
}