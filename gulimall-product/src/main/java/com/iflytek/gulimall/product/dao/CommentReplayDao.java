package com.iflytek.gulimall.product.dao;

import com.iflytek.gulimall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author rclin
 * @email rclin@iflytek.com
 * @date 2020-06-07 01:12:52
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
