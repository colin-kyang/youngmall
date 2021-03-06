package com.example.youngmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.renren.common.utils.Query;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;

import com.example.youngmall.product.dao.BrandDao;
import com.example.youngmall.product.entity.BrandEntity;
import com.example.youngmall.product.service.BrandService;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //首先判断 key
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper=new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("brand_id",key).or().like("name",key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void updateShoeStatus(Long brandId, int showStatus) {
        UpdateWrapper<BrandEntity> updateWrapper=new UpdateWrapper<>();
        //注意这里应该使用数据库字段
        updateWrapper.eq("brand_id",brandId).set("show_status",showStatus);
        baseMapper.update(null,updateWrapper);
    }
}