package com.example.springboottest.controller;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.beans.PropertyEditorSupport;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zm
 * @Created: 2025/11/4 下午5:09
 * @Description:
 */
@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ExecutorService executer = Executors.newCachedThreadPool();

    public enum Gender{MALE,FEMALE}

    @InitBinder
    public void initBinder(WebDataBinder binder){
        binder.registerCustomEditor(Gender.class,new PropertyEditorSupport(){

            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                super.setValue("male".equalsIgnoreCase(text)?Gender.MALE:Gender.FEMALE);
            }
        });
    }


    @GetMapping("/progress")
    public SseEmitter getProgress() {
        SseEmitter emitter = new SseEmitter(60_000L);

        // 异步执行任务
        executer.submit(() -> {
            try {
                for (int progress = 0; progress <= 100; progress += 10) {
                    Thread.sleep(1000);

                    //发送进度
                    emitter.send(SseEmitter.event().name("progress").data(progress));
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;

    }

    @GetMapping("/heart")
    public SseEmitter heartStream() {
        // 10分钟连接时间
        SseEmitter emitter = new SseEmitter(60_000L * 10);
        // 回调：客户端断开、超时、完成时清理资源
        emitter.onCompletion(() -> System.out.println("SSE complete"));
        emitter.onError(t -> System.out.println("SSE error:" + t));
        emitter.onTimeout(() -> System.out.println("SSE timeout"));

        var scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(()->{
            try{
                emitter.send(SseEmitter.event().name("ping").data("💗"));
            } catch (Exception e){
                emitter.complete();
            }
        },0,30, TimeUnit.SECONDS);

        new Thread(()->{
            try{
                for(int i=0;i<100;i++){
                    emitter.send(SseEmitter.event().name("msg").data("data-"+i));
                    Thread.sleep(2000);
                }
                emitter.complete();
            }catch (Exception e){
                emitter.completeWithError(e);
            }finally {
                scheduler.shutdown();
            }
        }).start();
        return emitter;
    }

    @GetMapping("/getUserInfo")
    public void getUserInfo(){

    }
}