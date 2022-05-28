package cn.itcast.hotel.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author Losca
 * @date 2022/5/28 14:35
 */
@Data
public class Page {
    private long total;
    private List<HotelDoc> hotels;
}
