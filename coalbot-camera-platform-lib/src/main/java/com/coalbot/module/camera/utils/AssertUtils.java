package com.coalbot.module.camera.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.coalbot.module.core.exception.CommonException;

import java.util.Collection;
import java.util.Map;

/**
 * @ClassName：AssertUtils
 * @Author: XinHai.Ma
 * @Date: 2025/11/25 15:25
 * @Description: 通用断言工具类
 */
public class AssertUtils {

// ==================== 对象相关断言 ====================

    /**
     * 断言对象不为null，否则抛异常
     *
     * @param obj     待校验对象
     * @param message 异常提示信息
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new CommonException(message);
        }
    }

    /**
     * 断言对象为null，否则抛异常
     *
     * @param obj     待校验对象
     * @param message 异常提示信息
     */
    public static void isNull(Object obj, String message) {
        if (obj != null) {
            throw new CommonException(message);
        }
    }

    // ==================== 字符串相关断言 ====================

    /**
     * 断言字符串不为空（非null且非空白字符），否则抛异常
     *
     * @param str     待校验字符串
     * @param message 异常提示信息
     */
    public static void notBlank(CharSequence str, String message) {
        if (StrUtil.isBlank(str)) {
            throw new CommonException(message);
        }
    }

    /**
     * 断言字符串为空（null或空白字符），否则抛异常
     *
     * @param str     待校验字符串
     * @param message 异常提示信息
     */
    public static void isBlank(CharSequence str, String message) {
        if (StrUtil.isNotBlank(str)) {
            throw new CommonException(message);
        }
    }

    // ==================== 集合/数组相关断言 ====================

    /**
     * 断言集合不为空（非null且元素数>0），否则抛异常
     *
     * @param collection 待校验集合
     * @param message    异常提示信息
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtil.isEmpty(collection)) {
            throw new CommonException(message);
        }
    }

    /**
     * 断言集合为空（null或元素数=0），否则抛异常
     *
     * @param collection 待校验集合
     * @param message    异常提示信息
     */
    public static void isEmpty(Collection<?> collection, String message) {
        if (CollectionUtil.isNotEmpty(collection)) {
            throw new CommonException(message);
        }
    }

    /**
     * 断言Map不为空（非null且元素数>0），否则抛异常
     *
     * @param map     待校验Map
     * @param message 异常提示信息
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtil.isEmpty(map)) {
            throw new CommonException(message);
        }
    }

    /**
     * 断言数组不为空（非null且长度>0），否则抛异常
     *
     * @param array   待校验数组
     * @param message 异常提示信息
     */
    public static void notEmpty(Object[] array, String message) {
        if (ArrayUtil.isEmpty(array)) {
            throw new CommonException(message);
        }
    }

    /**
     * 布尔值为true，为false抛异常
     * @param flag
     * @param message
     */
    public static void isTrue(boolean flag, String message) {
        if (!flag) {
            throw new CommonException(message);
        }
    }

    /**
     * 布尔值为false，为true抛异常
     * @param flag
     * @param message
     */
    public static void isFalse(boolean flag, String message) {
        if (flag) {
            throw new CommonException(message);
        }
    }

}
