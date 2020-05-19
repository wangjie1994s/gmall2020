package com.aisino.gmall.service;

import com.aisino.gmall.bean.PmsSearchParam;
import com.aisino.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
