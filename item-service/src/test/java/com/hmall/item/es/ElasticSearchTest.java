package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.hmall.item.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticSearchTest {
    private RestHighLevelClient client;
    @Test
    void matchAllTest() throws IOException {
        //基本操作
        //request
        SearchRequest request = new SearchRequest("items");
        //query,match
        request.source()
                .query(QueryBuilders.matchAllQuery());
        //client res
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        List<ItemDoc> docs = dataProcess(response);
    }

    @Test
    void esMatchTest() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("name","脱脂牛奶"))
                        .filter(QueryBuilders.termQuery("brand","德亚"))
                        .filter(QueryBuilders.rangeQuery("price").lt(30000))
        );
        SearchResponse response = client.search(request,RequestOptions.DEFAULT);
        //数据处理
        List<ItemDoc> docs = dataProcess(response);
        System.out.println(docs.toString());
    }
    @Test
    void esQuery() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source()
                .query(QueryBuilders.matchQuery("name","牛奶"))//查询
                .from(0).size(3)//分页
                .sort("sold", SortOrder.ASC)
                .sort("price",SortOrder.DESC)//排序
                .highlighter(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));//可以不指定标签，不指定标签默认为em
//              .highlighter(SearchSourceBuilder.highlight().field("xxx")) 这种和上面的new一样

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        System.out.println(dataProcess(response));
    }

    private List<ItemDoc> dataProcess(SearchResponse response){
        //get hit
        SearchHits searchHits = response.getHits();
        long value = searchHits.getTotalHits().value;
        SearchHit[] hitarr = searchHits.getHits();

        List<ItemDoc> docs = new ArrayList<>();

        for (SearchHit hit: hitarr){
            String json = hit.getSourceAsString();
            ItemDoc bean = JSONUtil.toBean(json, ItemDoc.class);

            //highlight处理
            Map<String, HighlightField> hfMap = hit.getHighlightFields();
            if (hfMap != null && !hfMap.isEmpty()){
                HighlightField hf = hfMap.get("name");//高亮结果
                String hfname = hf.getFragments()[0].string();//提取字段 name 在高亮查询中的第一个高亮片段的文本内容
//                StringBuilder sb = new StringBuilder();如果字段是需要拼接的情况可以这么写
//                for (Text text:hf.getFragments()){
//                    sb.append(text.string());
//                }
//                String appendConvert = sb.toString();
                bean.setName(hfname);
            }

            docs.add(bean);
            System.out.println(bean);
        }
        System.out.println(docs.size());
        return docs;
    }

    @BeforeEach
    void initES(){
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.6.130:9200")
        ));
    }
    @AfterEach
    void closeES() throws IOException {
        client.close();
    }
}
