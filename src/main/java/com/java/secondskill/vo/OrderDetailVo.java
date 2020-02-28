package com.java.secondskill.vo;


import com.java.secondskill.beans.OrderInfo;

/**
 * Created by jiangyunxiong on 2018/5/28.
 */
public class OrderDetailVo {
    private GoodsVo goods;

    private OrderInfo order;

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public OrderInfo getOrder() {
        return order;
    }

    public void setOrder(OrderInfo order) {
        this.order = order;
    }
}
