package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "trade.pay.success.queue",durable = "true"),
            exchange = @Exchange("pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId){
        //幂等性逻辑判断
        //ib plus 1.查询订单
        Order byId = orderService.getById(orderId);
        //2.判断状态 未支付状态：1
        if (byId == null || byId.getStatus() != 1){
            //不做处理
            return;
        }
        //3.标记订单的状态为为已支付
        orderService.markOrderPaySuccess(orderId);
    }

}
