package com.hmall.item.es;


import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ElasticIndexTest {
    private RestHighLevelClient restHighLevelClient;

    @Test
    void testConnection(){
        System.out.println("client test: " + restHighLevelClient);
    }

    @Test
    void testCreateIndex() throws IOException {
        //准备request 对象
        CreateIndexRequest request = new CreateIndexRequest("items");
        //准备请求参数
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //发送请求
        restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }
    @Test
    void testGetIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("items");
        restHighLevelClient.indices().get(request,RequestOptions.DEFAULT);
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);//确认是否存在
        System.out.println(exists);
    }
    @Test
    void testDelete() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("items");
        restHighLevelClient.indices().delete(request,RequestOptions.DEFAULT);
    }

    @BeforeEach
    void setUp(){
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.6.130:9200")
//                单点模式，所以只有一个，集群模式如果有多个可以这样做
//                HttpHost.create("192.168.6.130:9200"),
//                HttpHost.create("192.168.6.130:9200"),
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (restHighLevelClient != null){
            restHighLevelClient.close();
        }
    }

    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"stock\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":{\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
