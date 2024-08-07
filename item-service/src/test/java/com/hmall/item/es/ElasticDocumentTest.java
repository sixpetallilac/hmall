package com.hmall.item.es;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest(properties = "spring.profiles.active=local")//这里是对应application-local.yaml的数据库属性,不添加你会看到mysql connector的相关错误
public class ElasticDocumentTest {
    private RestHighLevelClient client;
    @Autowired //spring相关注解出现时候需要注入spring的注解
    private IItemService itemService;

    @Test
    void testIndexDoc() throws IOException {//添加一条文档
        Item byId = itemService.getById(40305713537L);
        ItemDoc itemDoc1 = BeanUtil.copyProperties(byId, ItemDoc.class);
        IndexRequest indexRequest = new IndexRequest("items").id(itemDoc1.getId());
        indexRequest.source(JSONUtil.toJsonStr(itemDoc1), XContentType.JSON);
        client.index(indexRequest,RequestOptions.DEFAULT);



        //0.准备文档数据（这里是从数据库搬一个转json）
        Item item = itemService.getById(40305713537L);
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        //1.request 准备
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        //2.准备参数
        request.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        //3.发送请求
        client.index(request, RequestOptions.DEFAULT);

    }

    @Test
    void testGetDoc() throws IOException {
        GetRequest request = new GetRequest("items","40305713537");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println(itemDoc);
    }
    @Test
    void testDeleteDoc() throws IOException {
        DeleteRequest request = new DeleteRequest("items","40305713537");
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateDoc() throws IOException {
        UpdateRequest request = new UpdateRequest("items","40305713537");
        request.doc(
                "price",200
        //可以是多个键值对，用逗号换行隔开
        );
        client.update(request,RequestOptions.DEFAULT);
    }

    @Test
    void testBulkDoc() throws IOException {
        //全部数据搬到es，分页目的防止一次性查完爆内存
        int pageNo = 1, pageSize = 500;
        while (true){
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if (records == null || records.isEmpty()){
                return;//查完就停了
            }


            BulkRequest request = new BulkRequest();
            for (Item item:records){
                request.add(new IndexRequest("items")
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDoc.class)),XContentType.JSON));
            }
            pageNo++;
            client.bulk(request,RequestOptions.DEFAULT);
        }


//  egs:      request.add(new IndexRequest("items").id("1").source("json",XContentType.JSON));
//        request.add(new UpdateRequest("items","1").doc("price",2000));
//        request.add(new DeleteRequest("items").id("1"));
    }

    @BeforeEach
    void setUp(){
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.6.130:9200")
        ));
    }
    @AfterEach
    void tearDown() throws IOException {
        if (client != null){
            client.close();
        }
    }
}
