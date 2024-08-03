package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.domain.dto.PayOrderDTO;
import com.hmall.trade.constant.MqConstant;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
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
            exchange = @Exchange(name = MqConstant.DELAY_EXCHANGE_NAME,delayed = "true",type = ExchangeTypes.DIRECT),
            key = MqConstant.DELAY_ORDER_KEY
    ))
    public void listenOrderDelayMessage(Long orderId){
        //1.查询
        Order order = orderService.getById(orderId);
        //2.监测订单状态status if已支付（不作处理）
        //tips : order status=1未付款
        if (null == order || 1 != order.getStatus()){
            //订单不存在或已经支付
            return;
        }
        //tips : payOrder=3支付成功
        // 2.1 未支付；查询支付单状态（业务幂等性，前面相当于已经判断过一次了，这里再判断一次支付单）
        PayOrderDTO payOrder = payClient.queryPayOrderByBizOrderNo(orderId);
        // 2.2 判断状态
        if (null != payOrder && 3 == payOrder.getStatus()){
        //3.1 已支付 标记已支付（出于严谨）
            orderService.markOrderPaySuccess(orderId);
        }else {
        //3.2 未支付 取消关闭订单，回复库存（其实核心逻辑就这一步）
            orderService.cancelOrder(orderId);
        }
    }
}
