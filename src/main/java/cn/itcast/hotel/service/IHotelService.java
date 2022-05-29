package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.Page;
import cn.itcast.hotel.pojo.RequestParam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {
    Page list(RequestParam requestParam);

    Map<String, List<String>> filters(RequestParam requestParam);

    List<String> suggestion(String prefix);

    void deleteById(Long id);

    void insertById(Long id);
}
