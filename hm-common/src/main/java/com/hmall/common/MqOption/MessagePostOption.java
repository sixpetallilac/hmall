package com.hmall.common.MqOption;

import org.springframework.amqp.core.MessagePostProcessor;

public class MessagePostOption {
    public static MessagePostProcessor messageDelaySet(Integer millisecond){
        return message -> {
            message.getMessageProperties().setDelay(millisecond);
            return message;
        };
    }
}
