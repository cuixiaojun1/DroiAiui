package com.droi.aiui.util;


import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuixiaojun on 17-12-7.
 * Json�������������
 */

public class JsonParserUtil {

    /**
     * ����JsonObject����
     *
     * @param jsonString
     *            Json��ʽ�ַ���
     * @param clazz
     *            ��װ��
     * @return ����T
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
     * ����JsonArray����
     *
     * @param jsonString
     *              Json��ʽ�ַ���
     * @param clazz
     *              ��װ��
     * @return
     *              �����������ͼ���
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