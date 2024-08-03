package com.hmall.api.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.domain.dto.ItemDTO;
import com.hmall.api.domain.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallbackFactory implements FallbackFactory<ItemClient> {


    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {//相当于直接在client当中做决定不做数据库IO操作
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败！",cause);
                return CollUtils.emptyList();//返回假数据
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品库存失败",cause);
                throw new RuntimeException(cause);//todo 业务不知道怎么处理的时候抛出异常即可
            }

            @Override
            public void restoreStock(List<OrderDetailDTO> orderDetailDTOS) {
                log.error("恢复商品库存失败",cause);
                throw new RuntimeException(cause);
            }
        };
    }
}
