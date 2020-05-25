package com.aisino.gmall.manage.service.impl;

import com.aisino.gmall.bean.PmsBaseAttrInfo;
import com.aisino.gmall.bean.PmsBaseAttrValue;
import com.aisino.gmall.bean.PmsBaseSaleAttr;
import com.aisino.gmall.manage.mapper.PmsAttrInfoListMapper;
import com.aisino.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.aisino.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.aisino.gmall.service.AttrService;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService{

    @Autowired
    PmsAttrInfoListMapper pmsAttrInfoListMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;


    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {

        //封装对象
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsAttrInfoListMapper.select(pmsBaseAttrInfo);

        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos){

            List<PmsBaseAttrValue> pmsBaseAttrValues = new ArrayList<>();
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);


        }

        return pmsBaseAttrInfos;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {

        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);

        return pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();

        if (StringUtils.isBlank(id)){
            //id为空，执行保存操作

            //保存属性
            pmsAttrInfoListMapper.insertSelective(pmsBaseAttrInfo);

            //保存属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());

                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        } else {
           //id不为空，修改属性
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id", pmsBaseAttrInfo.getId());
            pmsAttrInfoListMapper.updateByExampleSelective(pmsBaseAttrInfo, example);

            //按照属性id删除所有属性值
            PmsBaseAttrValue pmsBaseAttrValueDel = new PmsBaseAttrValue();
            pmsBaseAttrValueDel.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValueDel);

            //删除后，将新的属性值插入(修改属性值)
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }


        }



        return "success";

    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {

        String valueIdStr = StringUtils.join(valueIdSet, ",");
        //通用mapper无法实现多表联查，故需要使用自己手写sql
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsAttrInfoListMapper.selectAttrValueListByValueId(valueIdStr);

        return pmsBaseAttrInfos;
    }

}