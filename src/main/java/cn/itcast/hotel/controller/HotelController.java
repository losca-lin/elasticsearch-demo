package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.Page;
import cn.itcast.hotel.pojo.RequestParam;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Losca
 * @date 2022/5/28 14:33
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {
    @Autowired
    IHotelService hotelService;

    @PostMapping("/list")
    public Page list(@RequestBody RequestParam requestParam){
        return hotelService.list(requestParam);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> filters(@RequestBody RequestParam requestParam){
        return hotelService.filters(requestParam);
    }
}
