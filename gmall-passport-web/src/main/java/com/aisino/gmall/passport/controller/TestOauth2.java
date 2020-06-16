package com.aisino.gmall.passport.controller;

import com.aisino.gmall.util.HttpclientUtil;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {

    public static String getCode(){

        // 1 获得授权码
        // App Key = 2355710819
        // 回调地址 = http://127.0.0.1:8086/vlogin
        String s1 = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=2355710819&response_type=code&redirect_uri=http://passport.gmall.com:8086/vlogin");
        System.out.println(s1);
        // 在第一步和第二部返回回调地址之间,有一个用户操作授权的过程
        // 2 返回授权码到回调地址
        return null;
    }

    public static String getAccess_token(){
        // 3 换取access_token
        // client_secret=26c702e47902a58d157d92d30767b878
        String s3 = "https://api.weibo.com/oauth2/access_token?";
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","2355710819");
        paramMap.put("client_secret","26c702e47902a58d157d92d30767b878");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8086/vlogin");
        paramMap.put("code","06b904635d3e491bd3ce2fb114373d13");// 授权有效期内可以使用，没新生成一次授权码，说明用户对第三方数据进行重启授权，之前的access_token和授权码全部过期
        String access_token_json = HttpclientUtil.doPost(s3, paramMap);
        Map<String,String> access_map = JSON.parseObject(access_token_json,Map.class);
        System.out.println(access_map.get("access_token"));
        System.out.println(access_map.get("uid"));
        return access_map.get("access_token");
    }

    public static Map<String,String> getUser_info(){

        // 4 用access_token查询用户信息
        String s4 = "https://api.weibo.com/2/users/show.json?access_token=2.00OVNM_Gnz_7ZC85c345508e2cBoaB&uid=5780459220";
        String user_json = HttpclientUtil.doGet(s4);
        Map<String,String> user_map = JSON.parseObject(user_json,Map.class);
        System.out.println(user_map.get("1"));
        return user_map;
    }

    public static void main(String[] args) {

        getCode();
        getAccess_token();
        getUser_info();
        getAccess_token();
    }
}
