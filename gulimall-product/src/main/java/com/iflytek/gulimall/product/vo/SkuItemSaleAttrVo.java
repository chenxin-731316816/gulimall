package com.iflytek.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ToString
public class SkuItemSaleAttrVo {

    private Long attrId;

    private String attrName;

    private String attrValue;

    private String skuIds;

    private List<AttrValueWithSkuIdVo> attrValues;

}
