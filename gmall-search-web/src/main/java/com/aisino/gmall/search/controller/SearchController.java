package com.aisino.gmall.search.controller;

import com.aisino.gmall.bean.PmsSearchParam;
import com.aisino.gmall.bean.PmsSearchSkuInfo;
import com.aisino.gmall.service.SearchService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;


    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap){

        //调用elasticsearch服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);

        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        return "list";

    }

    @RequestMapping("index")
    public String index(){

        return "index";

    }


}
