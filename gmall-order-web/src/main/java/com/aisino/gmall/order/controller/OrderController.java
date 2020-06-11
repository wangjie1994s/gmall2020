package com.aisino.gmall.order.controller;

import com.aisino.gmall.annotations.LoginRequired;
import com.aisino.gmall.bean.OmsCartItem;
import com.aisino.gmall.bean.OmsOrderItem;
import com.aisino.gmall.bean.UmsMemberReceiveAddress;
import com.aisino.gmall.service.CartService;
import com.aisino.gmall.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @RequestMapping("toTrade")
    //必须登陆成功才能访问
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        //String memberId = (String)request.getAttribute("memberId");
        String memberId = "1";
        String nickname = (String)request.getAttribute("nickname");

        //通过memberId获得用户收货地址集合
        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses = userService.getReceiveAddressByMemberId(memberId);


        //通过memberId查询购物车列表
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);




        //将购物车集合转化成页面计算清单集合
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();

        for (OmsCartItem omsCartItem : omsCartItems) {
            //必须是选中的购物车对象
            if ("1".equals(omsCartItem.getIsChecked())){
                //每循环一次购物车对象，就封装一个订单详情对象OmsOrderItem
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItem.setOrderId(omsCartItem.getMemberId());
                omsOrderItem.setCouponAmount(omsCartItem.getQuantity());
                omsOrderItem.setProductBrand(omsCartItem.getProductBrand());
                omsOrderItem.setProductPrice(omsCartItem.getPrice().stripTrailingZeros().toString());
                omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                omsOrderItem.setProductAttr(omsCartItem.getProductAttr());

                omsOrderItems.add(omsOrderItem);
            }
        }

        modelMap.put("omsOrderItems", omsOrderItems);
        return "trade";
    }
}
