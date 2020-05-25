package com.aisino.gmall.search.controller;

import com.aisino.gmall.bean.*;
import com.aisino.gmall.service.AttrService;
import com.aisino.gmall.service.SearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){

        //调用elasticsearch服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);

        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        //抽取检索结果所包含的平台属性集合
        Set<String> valueIdSet = new HashSet<>();

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId =  pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        //根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);

        //对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
        String[] delValueIds = pmsSearchParam.getValueId();
        if(delValueIds != null){

            //面包屑功能代码实现
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();

            for (String delValueId : delValueIds) {
                //iterator非迭代器本身，游标不对集合产生影响，不可使用三层for循环删除数据，索引下表发生修改
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();

                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                //生成面包屑的参数
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParamForCrumb(pmsSearchParam,delValueId));

                while(iterator.hasNext()){
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if(delValueId.equals(valueId)){
                            //查找面包屑的属性值名称，处理属性值名字显示问题
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }
        String urlParam = getUrlParam(pmsSearchParam);
        //计算当前请求的urlParam值
        modelMap.put("urlParam", urlParam);

        String keyword = pmsSearchParam.getKeyword();
        if(StringUtils.isNotBlank(keyword)){
            modelMap.put("keyword", keyword);
        }
        return "list";

    }

    private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam,String delValueId) {

        //关键词keyword和三级分类idcatalog3Id至少有一个是不为空的
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        //不为空校验，以及urlParam拼接
        String urlParam = "";
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;
        }

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }

        if(skuAttrValueList != null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if(!pmsSkuAttrValue.equals(delValueId)){
                    urlParam = urlParam+"&valueId="+pmsSkuAttrValue;
                }
            }
        }
        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {

        //关键词keyword和三级分类idcatalog3Id至少有一个是不为空的
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        //不为空校验，以及urlParam拼接
        String urlParam = "";
        if(StringUtils.isNotBlank(keyword)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"keyword="+keyword;
        }

        if(StringUtils.isNotBlank(catalog3Id)){
            if(StringUtils.isNotBlank(urlParam)){
                urlParam = urlParam+"&";
            }
            urlParam = urlParam+"catalog3Id="+catalog3Id;
        }

        if(skuAttrValueList != null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam+"&valueId="+pmsSkuAttrValue;
            }
        }
        return urlParam;
    }

    @RequestMapping("index")
    public String index(){

        return "index";

    }


}
