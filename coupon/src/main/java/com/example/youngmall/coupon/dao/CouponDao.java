package com.example.youngmall.coupon.dao;

import com.example.youngmall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author colinyang
 * @email colin.kyang@outlook.com
 * @date 2022-03-02 19:24:56
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
