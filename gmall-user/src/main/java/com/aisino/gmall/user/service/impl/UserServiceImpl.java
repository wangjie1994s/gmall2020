package com.aisino.gmall.user.service.impl;

import com.aisino.gmall.service.UserService;
import com.aisino.gmall.bean.UmsMember;
import com.aisino.gmall.bean.UmsMemberReceiveAddress;
import com.aisino.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.aisino.gmall.user.mapper.UserMapper;
import com.aisino.gmall.util.RedisUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> umsMemberList = userMapper.selectAll();//userMapper.selectAllUser();

        return umsMemberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        // 封装的参数对象
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);

        //List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.selectByExample(umsMemberReceiveAddress);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {

        //从redis中取数据，redis中没有查询数据库然后入缓存
        Jedis jedis = null;
        try{
            //先查询缓存，获取用户信息
            jedis = redisUtil.getJedis();

            if(jedis != null){
                //缓存不为空
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + ":info");
                if(StringUtils.isNotBlank(umsMemberStr)){
                    //密码正确
                    UmsMember umsMemberFromCache = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMemberFromCache;
                } else {
                    //密码错误或者缓存中没有，需要查询数据库
                    UmsMember umsMemberFromDb = loginFromDb(umsMember);
                    if(umsMemberFromDb != null){
                        jedis.setex("user:" + umsMember.getPassword() + ":info", 60*60*24, JSON.toJSONString(umsMemberFromDb));
                    }
                    return umsMemberFromDb;
                }
            } else {
                //缓存为空(连接redis失败)，需要查询db获取用户信息，然后存入缓存中
                UmsMember umsMemberFromDb = loginFromDb(umsMember);
                if(umsMemberFromDb != null){
                    //设置过期时间为24小时
                    jedis.setex("user:" + umsMember.getPassword() + ":info", 60*60*24, JSON.toJSONString(umsMemberFromDb));
                }
                return umsMemberFromDb;
            }
        }finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {

    }

    private UmsMember loginFromDb(UmsMember umsMember) {

        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if(umsMembers != null){
            return umsMembers.get(0);
        }
        return null;
    }
}
