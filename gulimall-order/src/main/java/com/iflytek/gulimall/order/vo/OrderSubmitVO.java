package com.iflytek.gulimall.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderSubmitVO implements Serializable {

    private Long addressId;
    private Integer payType;
    //无需提交需要购买的商品，去购物车再获取一遍
    private BigDecimal payMoney;
    //防重令牌
    private String orderToken;

    //用户相关信息，直接去session取

}
