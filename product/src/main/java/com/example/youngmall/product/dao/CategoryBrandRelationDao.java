package com.example.youngmall.product.dao;

import com.example.youngmall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 * 
 * @author colinyang
 * @email colin.kyang@outlook.com
 * @date 2022-03-01 23:47:49
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {
    void updateBrand(@Param("brandId") Long brandId, @Param("brandName") String brandName);
	
}
