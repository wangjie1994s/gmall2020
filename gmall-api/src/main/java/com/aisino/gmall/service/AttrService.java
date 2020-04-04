package com.aisino.gmall.service;

import com.aisino.gmall.bean.PmsBaseAttrInfo;
import com.aisino.gmall.bean.PmsBaseAttrValue;

import java.util.List;

public interface AttrService {

    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    /**
    Boolean saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);
     **/

}
