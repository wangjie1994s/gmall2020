package com.aisino.gmall.cart.service.impl;

import com.aisino.gmall.bean.OmsCartItem;
import com.aisino.gmall.cart.mapper.OmsCartItemMapper;
import com.aisino.gmall.service.CartService;
import com.aisino.gmall.util.RedisUtil;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);

        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);

        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        System.out.println(omsCartItem.getQuantity());
        if(StringUtils.isNotBlank(omsCartItem.getMemberId())){
            //insertSelective对存在空值的属性不添加
            omsCartItemMapper.insertSelective(omsCartItem);
        }

    }
    //相关代码实现

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, e);

    }

    @Override
    public void flushCartCache(String memberId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        //同步到redis缓存中
        Jedis jedis = redisUtil.getJedis();

        //将omsCartItems集合转换成map
        Map<String, String> map = new HashMap<>();
        for (OmsCartItem cartItem : omsCartItems) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }

        //"user:memberId:cart"是对应的key值
        jedis.del("user:"+ memberId +":cart");
        //使用hash进行存储，将数据存入缓存中
        jedis.hmset("user:"+ memberId +":cart", map);

        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String memberId) {

        //先查缓存，缓存没有查询数据库；数据库查完后，返回数据，更新缓存
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        try{
            jedis = redisUtil.getJedis();

            List<String> hvals = jedis.hvals("user:"+ memberId + ":cart");
            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        } finally {
            jedis.close();
        }
        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId", omsCartItem.getMemberId()).andEqualTo("productSkuId", omsCartItem.getProductSkuId());
        omsCartItemMapper.updateByExampleSelective(omsCartItem,e);
        //缓存同步
        flushCartCache(omsCartItem.getMemberId());
    }

}
