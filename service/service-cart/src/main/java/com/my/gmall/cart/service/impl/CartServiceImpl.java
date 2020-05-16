package com.my.gmall.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.my.gmall.cart.mapper.CartInfoMapper;
import com.my.gmall.cart.service.CartService;
import com.my.gmall.common.util.AuthContextHolder;
import com.my.gmall.model.cart.CartInfo;
import com.my.gmall.model.product.SkuInfo;
import com.my.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mmy
 * @create 2020-05-09 19:27
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private ProductFeignClient productFeignClient;

    //加入购物车
    @Override
    public CartInfo attToCart(Long skuId, Integer skuNum, String userId) {
        //1.判断加入购物车时 当前库存是否在购物车中已经存在
        // 根据ID、SkuId查找
        CartInfo cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>()
                .eq("user_id", userId)
                .eq("sku_id", skuId));
        //2.存在了 更新此购物车的数量
        if (null != cartInfo){
            //追加数量
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //设置选中
            cartInfo.setIsChecked(1);
            //更新
            cartInfoMapper.updateById(cartInfo);
        }else {
            //3.不存在 添加此购物车到数据库
            cartInfo = new CartInfo();
            //用户ID
            cartInfo.setUserId(userId);
            //库存ID
            cartInfo.setSkuId(skuId);
            //数量
            cartInfo.setSkuNum(skuNum);
            //加入购物车时的价格 购物车价格
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            //标题
            cartInfo.setSkuName(skuInfo.getSkuName());
            //图片
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            //默认选中

            cartInfoMapper.insert(cartInfo);
        }

        return cartInfo;
    }

    //查询购物车集合 真实用户ID 临时用户Id
    @Override
    public List<CartInfo> cartList(String userId, String userTempId) {
        //1.判断两个用户ID是否存在
        if (!StringUtils.isEmpty(userId)){
            //表示已经登录
            if (!StringUtils.isEmpty(userTempId)){
                //临时用户存在
                //合并临时用户与已登录用户
                return mergeCartInfoList(userId, userTempId);
            }else{
                //临时用户不存在
                return cartInfoListByUserId(userId);
            }
        }else{
            //表示未登录
            //直接获取临时用户对应的购物车集合
            if (!StringUtils.isEmpty(userTempId)){
                return cartInfoListByUserId(userTempId);
            }
        }

        return null;
    }

    //选中或取消购物车
    @Override
    public void checkCart(Long skuId, Integer isChecked) {
        //获取用户ID
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String userId = AuthContextHolder.getUserId(request);
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        //更新选中或取消状态
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        cartInfoMapper.update(cartInfo,new QueryWrapper<CartInfo>()
                .eq("user_id",userId).eq("sku_id",skuId));

    }
    //查询当前登录的用户的选中了的购物车集合
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(new QueryWrapper<CartInfo>()
                .eq("user_id", userId).eq("is_checked", 1));
        if(!CollectionUtils.isEmpty(cartInfoList)){
            cartInfoList.forEach(cartInfo -> {
                BigDecimal price = productFeignClient.getPrice(cartInfo.getSkuId());
                cartInfo.setSkuPrice(price);
            });
        }
        return cartInfoList;
    }

    //用户ID、临时用户UUID  合并二大购物车集合
    public List<CartInfo> mergeCartInfoList(String userId, String userTempId) {
        //1.查询真实用户的购物车集合
        List<CartInfo> cartInfoListByUserId = cartInfoListByUserId(userId);
        //2.临时用户的购物车集合
        List<CartInfo> cartInfoListByUserTempId = cartInfoListByUserId(userTempId);
        //3.合并
        //判断真实用户的购物车是否有值
        if(CollectionUtils.isEmpty(cartInfoListByUserId)){
            //真实用户没有购物车
            //判断临时用户的购物车有没有
            if(!CollectionUtils.isEmpty(cartInfoListByUserTempId)){
                //有临时购物车  将临时用户的购物车在Mysql数据表中进行更新操作）
                CartInfo cartInfo = new CartInfo();
                cartInfo.setUserId(userId);
                //一条Sql语句  批量更新操作
                // update cart_info set user_id = 2 where user_id = 2b73986c408f432088ba1c6a1fc8ca22
                cartInfoMapper.update(cartInfo,
                        new QueryWrapper<CartInfo>().eq("user_id",userTempId));
                return cartInfoListByUserTempId;
            }
        }else{
            //真实用户有购物车
            //判断临时用户的购物车有没有
            if(!CollectionUtils.isEmpty(cartInfoListByUserTempId)){
                //Lomda表达式
                Map<Long, CartInfo> cartInfoMapByUserId = cartInfoListByUserId.stream().collect(
                        Collectors.toMap(CartInfo::getSkuId, cartInfo -> {
                            return cartInfo;
                        }));
                //临时用户有购物车  合并  有可能是更新 user_id 有可能删除 有可能追加数量
                for (CartInfo cartInfoByUserTempId : cartInfoListByUserTempId) {
                    //判断临时购物车是否在真实购物车集合中已经存在了
                    CartInfo cartInfoByUserId = cartInfoMapByUserId.get(cartInfoByUserTempId.getSkuId());
                    if(null != cartInfoByUserId){
                        //--1 存在了 追加数量
                        cartInfoByUserId.setSkuNum(cartInfoByUserId.getSkuNum()
                                + cartInfoByUserTempId.getSkuNum());
                        //操作Mysql数据库
                        //更新
                        cartInfoMapper.updateById(cartInfoByUserId);
                        //删除
                        cartInfoMapper.delete(
                                new QueryWrapper<CartInfo>().eq("user_id",userTempId));
                    }else{
                        //--2 不存在 直接添加
                        cartInfoMapByUserId.put(cartInfoByUserTempId.getSkuId(),cartInfoByUserTempId);
                        //操作Mysql数据库
                        //更新
                        cartInfoByUserTempId.setUserId(userId);
                        cartInfoMapper.updateById(cartInfoByUserTempId);
                    }
                }
                //合并后购物车
                return new ArrayList<>(cartInfoMapByUserId.values());
            }else{
                //临时用户没有购物车
                return cartInfoListByUserId;
            }
        }
        return null;
    }

    //根据用户Id（两个用户ID）查询用户集合
    public List<CartInfo> cartInfoListByUserId(String userId){
        if (!StringUtils.isEmpty(userId)){
            List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                    new QueryWrapper<CartInfo>().eq("user_id", userId));

            if (!CollectionUtils.isEmpty(cartInfoList)){
                cartInfoList.forEach(cartInfo -> {
                    BigDecimal price = productFeignClient.getPrice(cartInfo.getSkuId());
                    cartInfo.setSkuPrice(price);
                });
            }

            return cartInfoList;
        }
        return null;
    }
}
