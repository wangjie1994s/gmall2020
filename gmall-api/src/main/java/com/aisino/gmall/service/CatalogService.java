package com.aisino.gmall.service;

import com.aisino.gmall.bean.*;

import java.util.List;

public interface CatalogService {

    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);

    //List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    //List<PmsBaseAttrValue> getAttrValueList(String attrId);

    //Boolean saveAttrInfo();

}
