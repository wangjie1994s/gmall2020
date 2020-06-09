package com.aisino.gmall.passport.controller;

import com.aisino.gmall.bean.UmsMember;
import com.aisino.gmall.service.UserService;
import com.aisino.gmall.util.JwtUtil;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp){

        //通过jwt校验token真假
        Map<String, String> map = new HashMap<>();
        Map<String, Object> decode = JwtUtil.decode(token, "20200604gmall2020", currentIp);
        if (decode != null){
            map.put("status", "success");
            map.put("memberId", (String)decode.get("memberId"));
            map.put("nickname", (String)decode.get("nickname"));
        } else {
            map.put("status", "fail");
        }
        return JSON.toJSONString(map);
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){

        String token = "";

        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);

        if(umsMemberLogin != null){
            //登陆成功
            //用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", memberId);
            userMap.put("nickname", nickname);
            //通过nginx转发获得的ip
            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                //从request中获得ip
                ip = request.getRemoteAddr();
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
            //需要按照设计的算法对参数进行加密后，生成token
            token = JwtUtil.encode("20200604gmall2020", userMap, ip);
            //将token存入redis一份
            userService.addUserToken(token, memberId);
        } else {
            //登陆失败，返回类似用户名密码错误的错误信息
            token = "fail";
        }
        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map){

        if (StringUtils.isNotBlank(ReturnUrl)){

            map.put("ReturnUrl", ReturnUrl);
        }


        return "index";
    }
}
