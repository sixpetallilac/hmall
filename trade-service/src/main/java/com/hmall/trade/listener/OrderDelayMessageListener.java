package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.domain.dto.PayOrderDTO;
import com.hmall.trade.constant.MqConstant;
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
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(MqConstant.DELAY_QUEUE_NAME),
            exchange = @Exchange(name = MqConstant.DELAY_EXCHANGE_NAME,delayed = "true"),
            key = MqConstant.DELAY_ORDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId){
        //1.查询
        Order order = orderService.getById(orderId);
        //2.监测订单状态status 是否已支付

// order 1、未付款 2、已付款,未发货 3、已发货,未确认
// 4、确认收货，交易成功 5、交易取消，订单关闭
// 6、交易结束，已评价
        if (null == order || 1 != order.getStatus()){
            //订单不存在或已经支付
            return;
        }
        //payOrder=3支付成功 3 未支付，查询支付流水状态
        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(orderId);
        //3.1.判断是否支付
        if (null != payOrder && 3 == payOrder.getStatus()){
        //3.1 已支付 标记已支付
            orderService.markOrderPaySuccess(orderId);
        }else {
        //3.2 未支付，取消关闭订单，回复库存
            orderService.cancelOrder(orderId);
        }

    }
}
