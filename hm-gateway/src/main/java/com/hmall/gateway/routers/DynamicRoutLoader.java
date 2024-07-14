package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRoutLoader {

    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter writer;
    private final String dataId = "gateway-routes.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();
    @PostConstruct//bean 初始化之后执行
    public void initRoutConfigListener() throws NacosException {
        //1.项目启动拉取配置；添加配置listener
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        //3.监听变更，更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        //2.第一次读取更新路由表
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo(String configInfo){
        log.info("路由配置信息：",configInfo);
        //1.0解析配置信息，转换为routedefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //2.0删除旧的路由表
        for (String str : routeIds){
            writer.delete(Mono.just(str)).subscribe();
        }
        //2.1删除旧的路由表的ID（set）
        routeIds.clear();
        //3.0更新路由表
        for (RouteDefinition routeDefinition : routeDefinitions){
            //3.0更新路由表
            writer.save(Mono.just(routeDefinition)).subscribe();
            //3.1 记录路由ID 便于下一次更新时候删除
            routeIds.add(routeDefinition.getId());
        }
    }
}
