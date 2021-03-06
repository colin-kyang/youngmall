package com.example.youngmall.product.service.impl;

import com.example.common.constant.ProductConstant;
import com.example.common.to.SkuHasStockTo;
import com.example.common.to.SkuReductionTO;
import com.example.common.to.es.SkuEsModel;
import com.example.common.utils.R;
import com.example.youngmall.product.entity.*;
import com.example.youngmall.product.entity.vo.*;
import com.example.youngmall.product.feign.CouponFeignService;
import com.example.youngmall.product.feign.EsFeignService;
import com.example.youngmall.product.feign.WareFeignService;
import com.example.youngmall.product.service.*;
import io.renren.common.utils.Query;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.PageUtils;

import com.example.youngmall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    EsFeignService esFeignService;




    /**
     * spu ?????????????????????
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        //1) ?????????
        String key =(String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((e)->{e.eq("id",key).or().like("spu_name",key);});
        }
        //2) ??????
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equals(brandId)){
            wrapper.eq("brand_id",Long.parseLong(brandId));
        }
        //3?????????
        String catlogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catlogId) && !"0".equals(catlogId)){
            wrapper.eq("catalog_id",Long.parseLong(catlogId));
        }
        //4?????????
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",Integer.parseInt(status));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }


    /**
     * ????????????????????????
     * @param spuInfo
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuInfo) {
        //?????????????????? pms_product_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);
        //?????? Spu ??????????????? pms_spu_info_desc
        List<String> descript = spuInfo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        log.info(infoEntity.toString());
        spuInfoDescEntity.setSpuId(infoEntity.getId());
        //String join ?????????????????????
        //- to do - :??????iamgeurl ??????????????????????????????
        spuInfoDescEntity.setDecript(String.join(",", descript));
        log.info(spuInfoDescEntity.toString());
        this.saveSpuInfoDesc(spuInfoDescEntity);
        //?????? Spu ????????? pms_spu_images
        List<String> images = spuInfo.getImages();
        spuImagesService.saveImages(images, infoEntity.getId());
        //?????? Spu ??????????????? pms_product_attr_name
        List<BaseAttrs> baseAttrsList = spuInfo.getBaseAttrs();
        List<ProductAttrValueEntity> AttrValueList = baseAttrsList.stream().map(
                attr -> {
                    ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
                    valueEntity.setAttrId(attr.getAttrId());
                    //?????????????????????
                    valueEntity.setAttrName(attrService.getById(attr.getAttrId()).getAttrName());
                    valueEntity.setAttrValue(attr.getAttrValues());
                    valueEntity.setQuickShow(attr.getShowDesc());
                    valueEntity.setSpuId(infoEntity.getId());
                    return valueEntity;
                }).collect(Collectors.toList());
        //????????????
        productAttrValueService.saveBatch(AttrValueList);

        // ??????spu??? ???????????? sms_spu_bounds
        Bounds bounds = spuInfo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds,spuBoundsTo);
        spuBoundsTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.save(spuBoundsTo);
        if((Integer) r.get("code") != 0){
            log.info("???????????? sku ????????????????????????");
        }

        // sku ??????????????? pms_sku_info
        List<Skus> SkuList = spuInfo.getSkus();
        //sku ????????????????????????
        if (SkuList != null && SkuList.size() > 0) {
            //?????????????????????
            SkuList.forEach(item -> {
                String defaultImg = "";
                for(Images image : item.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                // ??????????????????
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoService.save(skuInfoEntity);
                //?????????skuId
                Long skuId = skuInfoEntity.getSkuId();
                //?????????sku imagesEntity
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;

                }).collect(Collectors.toList());

                //sku ??????????????? pms_sku_images
                skuImagesService.saveBatch(imagesEntities);

                // sku ????????????????????? pms_sku_sale_attr_value
                List<Attr> saleAttrList = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntites = saleAttrList.stream()
                        .map(a -> {
                            SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                            BeanUtils.copyProperties(a,skuSaleAttrValueEntity);
                            skuSaleAttrValueEntity.setSkuId(skuId);
                            return skuSaleAttrValueEntity;
                        }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntites);

                //sku ??????????????????????????? : gulimall_sms
                //sms_sku_full_reduction
                SkuReductionTO skuReductionTo = new SkuReductionTO();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                skuReductionTo.setMemberPrice(item.getMemberPrice());
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1){
                    R r1 = couponFeignService.saveFullReduction(skuReductionTo);
                    if((Integer) r1.get("code") != 0){
                        log.info("sku ?????????????????????????????? ??????????????????");
                    }
                }
            });
            log.info("???????????????????????????");
        }
    }

    /**
     * ?????? spuInfoDesc ??????????????????
     *
     * @param spuInfoDescEntity
     */
    private void saveSpuInfoDesc(SpuInfoDescEntity spuInfoDescEntity) {
        spuInfoDescService.save(spuInfoDescEntity);
    }

    /**
     * ??????spu_info ????????????
     *
     * @param infoEntity
     */
    private void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        baseMapper.insert(infoEntity);
        return;
    }

    /**
     * ????????????
     * @param spuId
     */
    @Override
    @Transactional
    public void up(Long spuId) {
        //??????spu??????????????????????????????
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.findProductAttrBySpuId(spuId);
        //?????????????????????????????????
        List<Long> searchShow = attrService.findSearchShow()
                .stream()
                .map(item ->{
                    return item.getAttrId();
                })
                .collect(Collectors.toList());
        //???spu?????????????????????????????????????????????????????????????????????
        List<ProductAttrValueEntity> attrValues = productAttrValueEntities
                .stream()
                .filter(item ->{
                    // ?????????????????????????????????????????????????????????attrValues
                    return searchShow.contains(item.getAttrId());
                })
                .collect(Collectors.toList());
        // skuEsModel ??????attr??????
        List<SkuEsModel.Attr> attrsList = attrValues.stream()
                .map(item ->{
                    SkuEsModel.Attr temp = new SkuEsModel.Attr();
                    BeanUtils.copyProperties(item,temp);
                    return temp;
                })
                .collect(Collectors.toList());
        // ??????????????????
        List<SkuEsModel> upProducts = new ArrayList<>();
        // ??????????????????
        // 1. ??????skuInfo
        List<SkuInfoEntity> skuInfoEntityList = skuInfoService.findSkuInfoBySpuId(spuId);
        //????????????spu??????sku???????????????
        List<Long> skuIds = skuInfoEntityList.stream()
                        .map(item ->{
                            return item.getSkuId();
                        }).collect(Collectors.toList());
        // ??????????????????????????????????????????????????????
        List<SkuHasStockTo>  skuHasStockTos  = (List<SkuHasStockTo>) wareFeignService.getSkusHasStock(skuIds).get("hasStock");
        Map<Long,Boolean> skuStockMap = null;
        // ???????????????map???[skuId,Boolean]
        try {
            skuStockMap = skuHasStockTos.stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId,item -> item.getHasStock()));
        } catch(Exception e){
            log.error("????????????????????????",e.toString());
        }
        // 2.???????????????sku ?????????
        Map<Long, Boolean> finalSkuStockMap = skuStockMap;
        upProducts = skuInfoEntityList.stream()
               .map(item ->{
                    SkuEsModel skuEsModel = new SkuEsModel();
                    BeanUtils.copyProperties(item,skuEsModel);
                    skuEsModel.setSkuPrice(item.getPrice());
                    skuEsModel.setSkuImg(item.getSkuDefaultImg());
                    //2.1 ??????????????????
                   if(finalSkuStockMap != null){
                       skuEsModel.setHasStock(finalSkuStockMap.get(item.getSkuId()));
                   } else {
                       skuEsModel.setHasStock(false);
                   }
                    //2.2 ????????????
                   skuEsModel.setHotScore(0L);
                    //2.3 ??????????????? ??? ?????????
                   skuEsModel.setCatalogName(categoryService.getById(skuEsModel.getCatalogId()).getName());
                   BrandEntity brandEntity = brandService.getById(skuEsModel.getBrandId());
                   skuEsModel.setBrandName(brandEntity.getName());
                   skuEsModel.setBrandImg(brandEntity.getLogo());
                    // 3.????????????sku???????????????????????????????????????filter
                   skuEsModel.setAttrs(attrsList);
                    return skuEsModel;
               })
               .collect(Collectors.toList());
                //??????????????????es ????????????
        //?????????????????????????????? ??????
        baseMapper.updateSpuStatus(spuId, ProductConstant.SpuStatus.PUBLISHED.getCode());
        R result = esFeignService.productStatusUp(upProducts);
        if ((int)result.get("code") == 0) {
            //??????????????????
            baseMapper.updateSpuStatus(spuId,ProductConstant.SpuStatus.PUBLISHED.getCode());
        } else {
            //??????????????????
            //???????????????????????????????????????????????????
        }
    }
}