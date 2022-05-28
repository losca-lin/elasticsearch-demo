package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * @author Losca
 * @date 2022/5/28 14:37
 */
@Data
public class RequestParam {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String city;
    private String starName;
    private String brand;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;
}
