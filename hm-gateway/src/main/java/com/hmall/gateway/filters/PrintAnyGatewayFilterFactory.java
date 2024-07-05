package com.hmall.gateway.filters;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
@Component
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory<PrintAnyGatewayFilterFactory.Config> {
    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                String a = config.getA();
                String b = config.getB();
                String c = config.getC();
                System.out.println(": "+ a);
                System.out.println(": "+ b);
                System.out.println(": "+ c);
                System.out.println("PrintAnyGatewayFilterFactory done ");
                return chain.filter(exchange);
            }
        },1);
    }

    @Data
    public static class Config{
        private String a;
        private String b;
        private String c;
    }

    public PrintAnyGatewayFilterFactory(){
        super(Config.class);//转换为字节码
    }//
    public List<String> shortcutFieldOrder(){
        return List.of("a","b","c");
    }

}
