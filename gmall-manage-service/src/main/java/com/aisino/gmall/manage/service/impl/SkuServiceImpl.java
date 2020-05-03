package com.aisino.gmall.manage.service.impl;

import com.aisino.gmall.bean.PmsSkuAttrValue;
import com.aisino.gmall.bean.PmsSkuImage;
import com.aisino.gmall.bean.PmsSkuInfo;
import com.aisino.gmall.bean.PmsSkuSaleAttrValue;
import com.aisino.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.aisino.gmall.manage.mapper.PmsSkuImageMapper;
import com.aisino.gmall.manage.mapper.PmsSkuInfoMapper;
import com.aisino.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.aisino.gmall.service.SkuService;
import com.aisino.gmall.util.RedisUtil;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService{

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    RedisUtil redisUtil;


    public PmsSkuInfo getSkuByIdFromDb(String skuId) {

        //sku的商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //sku的图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);

        return skuInfo;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {

        //sku的商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接缓存
        Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuJson = jedis.get(skuKey);

        if(StringUtils.isNotEmpty(skuJson)){

            pmsSkuInfo = JSON.parseObject(skuJson, PmsSkuInfo.class);
        } else {
            //缓存没有查询数据库

            //设置分布式锁，解决缓存击穿问题
            String OK = jedis.set("sku:" + skuId + ":lock", "1", "nx", "px", 10);
            //设置成功，有权在10秒的过期时间内访问数据库
            if(StringUtils.isNotEmpty(OK) && "OK".equals(OK)){

                pmsSkuInfo = getSkuByIdFromDb(skuId);
                if (pmsSkuInfo != null){

                    //同步到缓存中
                    jedis.set(skuKey, JSON.toJSONString(pmsSkuInfo));
                } else {

                    //数据库中不存在该sku
                    //为了防止缓存穿透，将null值或者空字符串值设置给redis
                    jedis.setex(skuKey,60*3 , JSON.toJSONString(""));
                }
            } else {

                //设置失败，自旋（该线程在睡眠几秒后，重新尝试访问本方法）
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //加上return重新访问本方法，防止产生孤儿线程
                return getSkuById(skuId);
            }

        }
        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }


    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        //插入SkuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();
        //插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();

        for(PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList){
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);

        }
        //插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();

        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList){
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);

        }
        //插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();

        for (PmsSkuImage pmsSkuImage : skuImageList){
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);

        }

    }
}
