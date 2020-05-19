package com.aisino.gmall.bean;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.List;

public class PmsSearchParam implements Serializable {

    @Id
    private String catalog3Id;
    private List<PmsSkuAttrValue> skuAttrValueList;
    private String keyword;


    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public List<PmsSkuAttrValue> getSkuAttrValueList() {
        return skuAttrValueList;
    }

    public void setSkuAttrValueList(List<PmsSkuAttrValue> skuAttrValueList) {
        this.skuAttrValueList = skuAttrValueList;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
