package com.aisino.gmall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJwt {

    /**
     * 验证加密
     * @param args
     */
    public static void main(String[] args) {

        Map<String, Object> map = new HashMap<>();
        map.put("memeberId", "1");
        map.put("nickname", "zhangsan");
        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyyMMdd HHmm").format(new Date());

        String encode = JwtUtil.encode("20200604gmall2020", map, ip + time);
        System.out.println(encode);
    }

}
