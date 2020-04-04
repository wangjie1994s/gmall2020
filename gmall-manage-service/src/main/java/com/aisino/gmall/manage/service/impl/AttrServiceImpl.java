package com.aisino.gmall.manage.service.impl;

import com.aisino.gmall.bean.PmsBaseAttrInfo;
import com.aisino.gmall.bean.PmsBaseAttrValue;
import com.aisino.gmall.manage.mapper.PmsAttrInfoListMapper;
import com.aisino.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.aisino.gmall.service.AttrService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService{

    @Autowired
    PmsAttrInfoListMapper pmsAttrInfoListMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;


    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {

        //封装对象
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);

        return pmsAttrInfoListMapper.select(pmsBaseAttrInfo);
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {

        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);

        return pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
    }

    /**
    @Override
    public Boolean saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        true；

    }
    **/

}