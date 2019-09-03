package com.droi.aiui.util;


import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuixiaojun on 17-12-7.
 * Json结果解析工具类
 */

public class JsonParserUtil {

    /**
     * 解析JsonObject数据
     *
     * @param jsonString
     *            Json格式字符串
     * @param clazz
     *            封装类
     * @return 泛型T
     *
     */
    public static <T> T parseJsonObject(String jsonString, Class<T> clazz) {
        T t = null;
        try {
            t = JSON.parseObject(jsonString, clazz);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return t;
    }
    /**
     * 解析JsonArray数据
     *
     * @param jsonString
     *              Json格式字符串
     * @param clazz
     *              封装类
     * @return
     *              泛型数据类型集合
     */
    public static <T> List<T> parseArray(String jsonString, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        try {
            list = JSON.parseArray(jsonString, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}