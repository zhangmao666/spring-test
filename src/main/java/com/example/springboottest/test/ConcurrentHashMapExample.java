package com.example.springboottest.test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: zm
 * @Created: 2025/11/3 下午5:24
 * @Description:
 */

public class ConcurrentHashMapExample {



    public static void fun1(){
        ConcurrentHashMap<String, List<String>> groupMap = new ConcurrentHashMap<>();
        groupMap.computeIfAbsent("teamA",k-> new CopyOnWriteArrayList<>()).add("Alice");
        groupMap.computeIfAbsent("teamA",k-> new CopyOnWriteArrayList<>()).add("Bob");

        System.out.println(groupMap.get("teamA"));

    }

    public static void fun2(){
        ConcurrentHashMap<String,Integer> userVisits = new ConcurrentHashMap<>();

        Thread[] threads = new Thread[100];

        for (int i = 0; i < 100; i++) {
            threads[i] = new Thread(() -> {
                userVisits.merge("userA",2,Integer::sum);
                userVisits.merge("userB",1,Integer::sum);
                userVisits.merge("userC",1,Integer::sum);
            });
            threads[i].start();
        }

        for (int i = 0; i < 100; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("UserA visits: " + userVisits.get("userA")); // 输出: 100
        System.out.println("UserB visits: " + userVisits.get("userB")); // 输出: 100
        System.out.println("UserC visits: " + userVisits.get("userC")); // 输出: 100
    }


    public static void fun3(){
        ConcurrentHashMap<String, Double> prices = new ConcurrentHashMap<>();

        prices.merge("apple",100.0,(oldPrice,newPrice)-> (oldPrice + newPrice)/2);
        prices.merge("apple",50.0,(oldPrice,newPrice)-> (oldPrice + newPrice)/2);
        prices.merge("apple",50.0,(oldPrice,newPrice)-> (oldPrice + newPrice)/2);

        System.out.println(prices.get("apple")); // 输出: 75.0
    }

    public static void main(String[] args) {
        fun3();
    }
}