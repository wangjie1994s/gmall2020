package com.aisino.gmall.interceptors;

import com.aisino.gmall.annotations.LoginRequired;
import com.aisino.gmall.util.CookieUtil;
import com.aisino.gmall.util.HttpclientUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 拦截代码

        //判断被拦截的请求访问的方法的注解（是否是需要被拦截的）
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequired methodAnnotion = hm.getMethodAnnotation(LoginRequired.class);
        //是否拦截
        if(methodAnnotion == null){
            //不需要拦截，直接return true
            return true;
        }

        String token = "";

        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        if(StringUtils.isNotBlank(oldToken)){
            token = oldToken;
        }

        String newToken = request.getParameter("token");
        if(StringUtils.isNotBlank(newToken)){
            token = newToken;
        }

        //已经拦截，判断是否必须登陆
        //获得该请求是否登陆成功
        boolean loginSuccess = methodAnnotion.loginSuccess();

        //调用认证中心，验证token
        String success = "fail";
        Map<String, String> successMap = new HashMap<>();
        if(StringUtils.isNotBlank(token)){
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
            String successJson = HttpclientUtil.doGet("http://127.0.0.1:8086/verify?token="+token+"&currentIp="+ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }

        if(loginSuccess){
            //必须登陆成功才能使用
            if(!success.equals("success")){
                //重定向会password登陆
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://127.0.0.1:8086/index?ReturnUrl="+requestURL);
                return false;
            } else {
                //需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                return true;
            }
        } else {
            //可以不用登陆就可以访问,但是token必须验证
            if(success.equals("success")){
                //需要将token携带的用户信息写入
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
            }
        }
        //验证通过，覆盖cookie中的token
        if(StringUtils.isNotBlank(token)){
            CookieUtil.setCookie(request, response, "oldToken", token, 60*60*2, true);
        }

        System.out.println("进入拦截器的拦截方法");
        return true;
    }
}