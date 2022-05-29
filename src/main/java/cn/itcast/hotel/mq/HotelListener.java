package cn.itcast.hotel.mq;

import cn.itcast.hotel.constants.SystemConstants;
import cn.itcast.hotel.service.impl.HotelService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Losca
 * @date 2022/5/29 20:47
 */
@Component
public class HotelListener {
    @Autowired
    HotelService hotelService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SystemConstants.HOTEL_INSERT_QUEUE),
            exchange = @Exchange(name = SystemConstants.HOTEL_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = SystemConstants.HOTEL_INSERT_KEY
    ))
    public void listenHotelInsertOrUpdate(long id){
        hotelService.insertById(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = SystemConstants.HOTEL_DELETE_QUEUE),
            exchange = @Exchange(name = SystemConstants.HOTEL_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = SystemConstants.HOTEL_DELETE_KEY
    ))
    public void listenHotelDelete(long id){
        hotelService.deleteById(id);
    }
}
