package com.aisino.gmall.manage.controller;

import com.aisino.gmall.bean.PmsBaseAttrInfo;
import com.aisino.gmall.bean.PmsBaseAttrValue;
import com.aisino.gmall.bean.PmsBaseSaleAttr;
import com.aisino.gmall.service.AttrService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
public class AttrController {


    @Reference
    AttrService attrService;

    ////http://127.0.0.1:8082/baseSaleAttrList
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = attrService.baseSaleAttrList();

        return pmsBaseSaleAttrs;

    }


    //http://127.0.0.1:8082/attrInfoList?catalog3Id=1
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){

        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.attrInfoList(catalog3Id);

        return pmsBaseAttrInfoList;

    }

    //http://127.0.0.1:8082/getAttrValueList?attrId=12
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){

        List<PmsBaseAttrValue> pmsBaseAttrValueList =  attrService.getAttrValueList(attrId);

        return pmsBaseAttrValueList;

    }

    //http://127.0.0.1:8082/saveAttrInfo
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        String success = attrService.saveAttrInfo(pmsBaseAttrInfo);

        return "success";

    }

}
