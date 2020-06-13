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
import tk.mybatis.mapper.util.StringUtil;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10*1000);//需设置分布式锁的超时时间，防止其他线程拿不到分布式锁
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

                //删除锁之前先get到lockToken
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                //用token确认的是删除的自己的锁
                if(StringUtil.isNotEmpty(lockToken) && lockToken.equals(token)){
                    //可以用lua脚本，在查询到key的同时删除该key，防止高并发下的意外的发生
                    String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList("lock"),Collections.singletonList(token));
                    //在访问完数据库之后，需要释放redis分布式锁
                    jedis.del("sku:" + skuId + ":lock");
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
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {

            String skuId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);

        }

        return pmsSkuInfos;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {

        boolean b = false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        BigDecimal skuPrice = pmsSkuInfo1.getPrice();
        if (skuPrice.compareTo(price) == 0){
            b = true;
        }

        return b;
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
