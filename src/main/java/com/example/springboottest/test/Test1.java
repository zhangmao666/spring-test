package com.example.springboottest.test;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author: zm
 * @Created: 2025/12/10 上午9:53
 * @Description:
 */

public class Test1 {

    /**
     * JSON字符串转JSONObject
     * @param jsonObjectStr
     * @return
     */
    public static JSONObject function(String jsonObjectStr) {
        return JSONObject.parseObject(jsonObjectStr);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zm");
        map.put("age", 18);
        map.put("sex", "男性");
        map.put("address", "北京市朝阳区");
        map.put("phone", "13800138000");


        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}