package com.example.youngmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.PageUtils;
import com.example.youngmall.coupon.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author colinyang
 * @email colin.kyang@outlook.com
 * @date 2022-03-27 16:32:10
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

