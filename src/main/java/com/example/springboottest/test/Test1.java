package com.example.springboottest.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: zm
 * @Created: 2025/12/10 上午9:53
 * @Description:
 */

public class Test1 {

    public static void main(String[] args) {
        Map<String, Object> map =new HashMap<>();
        map.put("name", "zm");
        map.put("age", 18);
        map.put("sex", "男性");
        

        for (Map.Entry<String, Object> entry : map.entrySet()){
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}