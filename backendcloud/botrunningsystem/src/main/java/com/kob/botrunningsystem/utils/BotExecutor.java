package com.kob.botrunningsystem.utils;

import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class BotExecutor extends Thread {
    private static final String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";
    private static RestTemplate restTemplate;

    private Bot bot;

    @Autowired
    private void getRestTemplate(RestTemplate restTemplate) {
        BotExecutor.restTemplate = restTemplate;
    }

    public void startTimeOut(long timeout, Bot _bot) {
        bot = _bot;
        this.start();
        try {
            this.join(timeout); // 在这一行阻塞，当join的归属线程执行结束或者超时就结束阻塞
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }

    private String addUid(String code, String uid) {
        final String mark = " implements java.util.function.Supplier<Integer>";
        int keyIdx = code.indexOf(mark);
        if (keyIdx == -1) return "";
        return code.substring(0, keyIdx) + uid + code.substring(keyIdx);
    }

    @Override
    public void run() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().substring(0, 8);

        Supplier<Integer> runningBotInterface = Reflect.compile(
                "RunningBot" + uid,
                addUid(bot.getBotCode(), uid))
                .create()
                .get();

        File file = new File("input.txt");
        try (PrintWriter fout = new PrintWriter(file)) {
            fout.println(bot.getGameStatus());
            fout.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Integer direction = runningBotInterface.get();
        System.out.println("move direciton: " + bot.getUserId() + " " + direction);

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());

        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
