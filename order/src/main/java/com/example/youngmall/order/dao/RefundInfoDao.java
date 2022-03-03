package com.example.youngmall.order.dao;

import com.example.youngmall.order.entity.RefundInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款信息
 * 
 * @author colinyang
 * @email colin.kyang@outlook.com
 * @date 2022-03-03 09:30:21
 */
@Mapper
public interface RefundInfoDao extends BaseMapper<RefundInfoEntity> {
	
}
