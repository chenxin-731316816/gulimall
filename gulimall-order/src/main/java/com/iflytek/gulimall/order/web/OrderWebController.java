package com.iflytek.gulimall.order.web;

import com.iflytek.gulimall.common.exception.RRException;

import com.iflytek.gulimall.common.feign.vo.MemberVO;
import com.iflytek.gulimall.order.entity.OrderEntity;
import com.iflytek.gulimall.order.interceptor.LoginInterceptor;
import com.iflytek.gulimall.order.service.OrderService;
import com.iflytek.gulimall.order.vo.OrderConfirmVO;
import com.iflytek.gulimall.order.vo.OrderSubmitResposeVO;
import com.iflytek.gulimall.order.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{action}.html")
    public String orderHtml(@PathVariable("action") String action) {
        return action;
    }


    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVO orderConfirmVO = orderService.toTrade();
        model.addAttribute("orderConfirmVO", orderConfirmVO);
        return "confirm";
    }


    //在订单支付页面试可以刷新页面的,在提交订单之后,重定向到订单支付页
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVO orderSubmitVO,
                              RedirectAttributes redirectAttributes) {
        try {
            OrderSubmitResposeVO reposeVO = orderService.submitOrder(orderSubmitVO);
            redirectAttributes.addAttribute("orderSn", reposeVO.getOrderEntity().getOrderSn());
            return "redirect:http://order.gulimall.com/payDetail.html";
        } catch (RRException e) {
            redirectAttributes.addFlashAttribute("error", e.getMsg());
            return "redirect:http://order.gulimall.com/toTrade";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "系统异常");
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }

    //在订单支付页面试可以刷新页面的,在提交订单之后,重定向到订单支付页
    @GetMapping("/payDetail.html")
    public String payDetailHtml(@RequestParam("orderSn") String  orderSn, Model model) {
        MemberVO memberVO = LoginInterceptor.getMemberVO();
        OrderEntity orderEntity = orderService.getOrderEntityByOrderSnAndUserId(orderSn, memberVO.getUserId());
        model.addAttribute("orderEntity", orderEntity);
        return "pay";
    }
    @GetMapping("/city/consignee/editConsignee")
    public String editConsignee(Model model){

       return "editConsignee";
    }



}
