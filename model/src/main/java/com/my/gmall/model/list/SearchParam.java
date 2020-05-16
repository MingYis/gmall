package com.my.gmall.model.list;

import lombok.Data;

/**
 * <p>
 * 商品搜索参数
 * 参数说明：
 *      1，商标品牌：trademark=2:华为
 *              2：为品牌id，搜索字段
 *              华为：品牌名称，页面回显属性
 *      2，平台属性：props=23:4G:运行内存
 *              23：平台属性id，搜索字段
 *              运行内存：平台属性名称，页面回显属性
 *              4G：平台属性值，搜索字段与页面回显属性
 *      3，排序：order=1:asc
 *              1：排序类型（1：综合排序/热点  2：价格）
 *              asc：升序（desc为降序）
 * </p>
 *
 * @author qy
 */
@Data
public class SearchParam {

    // ?category3Id=61&trademark=2:华为&props=23:4G:运行内存&order=1:desc
    //category3Id=61
    private Long category1Id;;//三级分类id
    private Long category2Id;
    private Long category3Id;

    //trademark=2:华为
    private String trademark;//品牌id

    private String keyword;//检索的关键字

    // order=1:asc  排序规则   0:asc
    private String order = "";// 1：综合排序/热点  2：价格

    //props=23:4G:运行内存
    private String[] props;//页面提交的数组

    private Integer pageNo = 1;//分页信息
    private Integer pageSize = 4;
}
