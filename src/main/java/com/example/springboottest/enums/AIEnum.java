package com.example.springboottest.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @Author: zm
 * @Created: 2025/12/2 上午10:23
 * @Description: ai枚举类
 */
@Getter
@AllArgsConstructor
public enum AIEnum {

    BAIDU("ernie-bot", "文心一言"),
    SPARK("spark", "讯飞星火"),
        ;

    @EnumValue
    private final String key;

    private final String name;

    public static AIEnum findEnumByKey(String key) {
        for (AIEnum ai : AIEnum.values()) {
            if (ai.getKey().contains(key.toLowerCase())) {
                return ai;
            }
        }
        return AIEnum.BAIDU;
    }

    public static List<Map<String,String>> getModels() {
        List<Map<String, String>> list = new java.util.ArrayList<>();

        for(AIEnum ai : AIEnum.values()) {
            Map<String, String> map = new java.util.HashMap<>();
            map.put("value",ai.getKey());
            map.put("label",ai.getName());
            list.add(map);
        }
        return list;
    }

}