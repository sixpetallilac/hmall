package com.hmall.api.config;

import com.hmall.api.fallback.ItemClientFallbackFactory;
import com.hmall.api.fallback.PayClientFallback;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.HEADERS;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Long userId = UserContext.getUser();
                if (null != userId){
                    template.header("user-info",userId.toString());
                }
            }
        };
    }

    @Bean
    public ItemClientFallbackFactory itenClientFallbackFactory(){
            return new ItemClientFallbackFactory();
    }
    @Bean
    public PayClientFallback payClientFallback(){
        return new PayClientFallback();
    }
}
