package com.java.secondskill.redis;

/**
 * Created by jiangyunxiong on 2018/5/21.
 *
 * 缓冲key前缀
 */
public interface KeyPrefix {

    /**
     * 有效期
     */
    int expireSeconds();

    /**
     * 前缀
     */
    String getPrefix();
}
