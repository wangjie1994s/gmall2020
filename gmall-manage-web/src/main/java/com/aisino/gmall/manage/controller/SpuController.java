package com.aisino.gmall.manage.controller;

import com.aisino.gmall.bean.PmsProductImage;
import com.aisino.gmall.bean.PmsProductInfo;
import com.aisino.gmall.bean.PmsProductSaleAttr;
import com.aisino.gmall.manage.util.PmsUploadUtil;
import com.aisino.gmall.service.SpuService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    //http://127.0.0.1:8082/spuImageList?spuId=24
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){


        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);

        return pmsProductImages;
    }

    //http://127.0.0.1:8082/spuSaleAttrList?spuId=24
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){


        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);

        return pmsProductSaleAttrs;
    }

    //http://127.0.0.1:8082/fileUpload
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){

        //将图片或者音视频上传到分布式的文件存储系统
        String imgUrl = PmsUploadUtil.uploadImage(multipartFile);

        System.out.println(imgUrl);
        //将图片的存储路径返回给页面
        return  imgUrl;
    }

    //http://127.0.0.1:8082/saveSpuInfo
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){


        spuService.saveSpuInfo(pmsProductInfo);

        return  "success";
    }

    //http://127.0.0.1:8082/spuList?catalog3Id=61
    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){


        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);

        return  pmsProductInfos;
    }




}
