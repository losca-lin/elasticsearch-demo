package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.Page;
import cn.itcast.hotel.pojo.RequestParam;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IHotelService extends IService<Hotel> {
    Page list(RequestParam requestParam);
}
