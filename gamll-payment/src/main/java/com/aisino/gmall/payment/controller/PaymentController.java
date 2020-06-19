package com.aisino.gmall.payment.controller;

import com.aisino.gmall.annotations.LoginRequired;
import com.aisino.gmall.bean.OmsOrder;
import com.aisino.gmall.bean.PaymentInfo;
import com.aisino.gmall.payment.config.AlipayConfig;
import com.aisino.gmall.service.OrderService;
import com.aisino.gmall.service.PaymentService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService;


    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String aliPayCallBackReturn(HttpServletRequest request, ModelMap modelMap){

        //回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();

        //通过支付宝的paramsMap进行签名验证，但是2.0接口将paramsMap参数去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)){
            //验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);//支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());
            //更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
        }

        return "finish";
    }

    /**
     * 调用微信支付服务接口-目前暂不实现
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        return null;
    }

    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        // 获得一个支付宝请求的客户端(它并不是一个链接，而是一个封装好的http的表单请求)
        String form = null;
        //电脑端方法
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //手机端方法
        //AlipayMobilePublicMultiMediaDownloadRequest alipayRequest = new AlipayMobilePublicMultiMediaDownloadRequest();

        // 回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        //todo
        outTradeNo = "gmall1592033505676202006165153145";
        totalAmount = new BigDecimal("0.01");
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",totalAmount);
        map.put("subject","尚硅谷感光徕卡Pro300瞎命名系列手机");

        String param = JSON.toJSONString(map);

        alipayRequest.setBizContent(param);

        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //从订单信息中获得属性封装到paymentInfo中
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        //生成并保存用户的支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("谷粒商城商品一件");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo);
        //向消息中间件发送一个检查支付状态（支付服务消费）的延迟消息队列
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNo, 5);


        //提交请求到支付宝
        return form;
    }

    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap){

        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");
        //String outTradeNo = "gmall1592041632728202006165174712";
        //BigDecimal totalAmount = new BigDecimal("296426.00");

        modelMap.put("nickname", nickname);
        modelMap.put("outTradeNo", outTradeNo);
        modelMap.put("totalAmount", totalAmount);

        return "index";
    }
}
