package com.aisino.gmall.service;

import com.aisino.gmall.bean.PmsProductImage;
import com.aisino.gmall.bean.PmsProductInfo;
import com.aisino.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {

    List<PmsProductInfo> spuList(String catalog3Id);

    Boolean saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);
}
