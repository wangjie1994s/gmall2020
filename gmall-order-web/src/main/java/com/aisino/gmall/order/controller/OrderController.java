package com.aisino.gmall.order.controller;

import com.aisino.gmall.annotations.LoginRequired;
import com.aisino.gmall.bean.OmsCartItem;
import com.aisino.gmall.bean.OmsOrder;
import com.aisino.gmall.bean.OmsOrderItem;
import com.aisino.gmall.bean.UmsMemberReceiveAddress;
import com.aisino.gmall.service.CartService;
import com.aisino.gmall.service.OrderService;
import com.aisino.gmall.service.SkuService;
import com.aisino.gmall.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserService userService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

   @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true)
    public String submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {


        String memberId = (String) request.getAttribute("memberId");
        //String memberId = "1";
        String nickname = (String) request.getAttribute("nickname");
        //String nickname = "windir";

        //防止一个订单重复提交的解决方法：一个订单提交页只允许使用一个交易码，且每次提交订单后该交易码失效，下次得重新获取新的交易码
        //根据检查结果提交订单，如果检查通过，允许提交订单； 如果检查失败，不允许提交订单
        // 1.检查交易码
       //todo 请求submitOrder方法参数为空问题需要排查
        tradeCode = "gmall1592032836168202006165152036";
        //String success = orderService.checkTradeCode(memberId, tradeCode);
       String success = "success";

        if (success.equals("success")) {
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            // 订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount(); 运费，支付后，在生成物流信息时
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("快点发货");
            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();// 将毫秒时间戳拼接到外部订单号
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + sdf.format(new Date());// 将时间字符串拼接到外部订单号

            omsOrder.setOrderSn(outTradeNo);//外部订单号
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            //调用用户服务获得收货人信息
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getReceiveAddressById("4");
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            // 当前日期加一天，一天后配送
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            Date time = c.getTime();
            omsOrder.setReceiveTime(time);
            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);
            omsOrder.setOrderType(0);
            omsOrder.setTotalAmount(totalAmount);

            // 2.根据用户id获得要购买的商品列表(从购物车中获取)，和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);

            //系统不替用户做决定
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    // 3.获得订单详情列表,需要校验价格和校验库存
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    // 检价,验的是单个商品的价格
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if (b == false) {
                        return "tradeFail";
                    }
                    // 验库存,远程调用库存系统
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    // 外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setOrderSn(outTradeNo);
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("仓库对应的商品编号");// 在仓库中的skuId

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);

            // 4.将订单和订单详情写入数据库，同时删除购物车的对应商品
            orderService.saveOrder(omsOrder);


            // 5.重定向到支付系统
            return "redirect:http://payment.gmall.com:8075/index";
        } else {
            return "tradeFail";
        }
    }


    @RequestMapping("toTrade")
    //必须登陆成功才能访问
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        String memberId = (String)request.getAttribute("memberId");
        //String memberId = "1";
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

                omsOrderItems.add(omsOrderItem);
            }
        }

        modelMap.put("omsOrderItems", omsOrderItems);
        modelMap.put("userAddressList", umsMemberReceiveAddresses);
        modelMap.put("totalAmount", getTotalAmount(omsCartItems));

        // 生成交易码，为了在提交订单时做交易码的校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);
        return "trade";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {

        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if("1".equals(omsCartItem.getIsChecked())){
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        return totalAmount;
    }


}
