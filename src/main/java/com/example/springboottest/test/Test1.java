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
        final JSONObject jsonObject = JSONObject.parseObject(jsonObjectStr);
        return jsonObject;
    }


    public static void function1(){
        Map<String,Object> map =new HashMap<>();
        map.put("name","zm");
        map.put("age",18);
        map.put("address","Beijing");

        for(Map.Entry<String,Object> entry:map.entrySet()){
            System.out.println(entry.getKey()+":"+entry.getValue());
        }

    }

    public static void function2() {
        String prompt = """
                     你是一个基金分析专家，请分析以下基金的基本情况和投资价值：
                """;
        System.out.println(prompt);
    }


    public static void function3(){
        String prompt = """
                你是一个基金分析专家，请分析以下基金的基本情况和投资价值：
                """;

        System.out.println(prompt);
    }

    public static void main(String[] args) {
        function1();
    }
}