package com.aisino.gmall.search.service.impl;

import com.aisino.gmall.bean.PmsSearchParam;
import com.aisino.gmall.bean.PmsSearchSkuInfo;
import com.aisino.gmall.bean.PmsSkuAttrValue;
import com.aisino.gmall.service.SearchService;
import com.alibaba.dubbo.config.annotation.Service;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;



    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {

        //调用getSearchDsl方法
        String dslString = getSearchDsl(pmsSearchParam);

        System.out.println(dslString);

        //用api执行复杂查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        Search search = new Search.Builder(dslString).addIndex("gmall2020").addType("PmsSkuInfo").build();

        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {

            PmsSearchSkuInfo source = hit.source;

            Map<String,List<String>> highlight = hit.highlight;
            if(highlight != null){

                String skuName = highlight.get("skuName").get(0);
                source.setSkuName(skuName);
            }
            pmsSearchSkuInfos.add(source);

        }

        System.out.println(pmsSearchSkuInfos.size());


        return pmsSearchSkuInfos;
    }


    private String getSearchDsl(PmsSearchParam pmsSearchParam){

        String[] skuAttrValueList = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();

        //jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //filter
        if (StringUtils.isNotBlank(catalog3Id)){

            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);

        }
        //保证pmsSkuAttrValueList不为空时才做循环
        if (skuAttrValueList != null){

            for (String pmsSkuAttrValue : skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", pmsSkuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);
                //TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("","");
            }
        }

        //must
        if (StringUtils.isNotBlank(keyword)){

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuName");
        searchSourceBuilder.highlight(highlightBuilder);
        //sort
        searchSourceBuilder.sort("id", SortOrder.DESC);

        return searchSourceBuilder.toString();
    }

}