package com.iflytek.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.iflytek.gulimall.common.utils.PageUtils;
import com.iflytek.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author rclin
 * @email rclin@iflytek.com
 * @date 2020-06-07 01:12:52
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void delete(List<Long> asList);
}

