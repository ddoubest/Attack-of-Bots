package com.kob.matchingsystem.utils;

import com.kob.matchingsystem.service.impl.MatchingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchingPool extends Thread {
    private static List<Player> players = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();
    public static final String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";

    private static RestTemplate restTemplate;

    @Autowired
    private void getRestTemplate(RestTemplate restTemplate) {
        MatchingPool.restTemplate = restTemplate;
    }

    public void addPlayer(Integer userId, Integer rating) {
        lock.lock();
        try {
            players.add(new Player(userId, rating, 0));
        } finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId) {
        lock.lock();
        try {
            List<Player> newPlayers = new ArrayList<>();
            for (Player player : players) {
                if (!userId.equals(player.getUserId())) {
                    newPlayers.add(player);
                }
            }
            MatchingPool.players = newPlayers;
        } finally {
            lock.unlock();
        }
    }

    private void increaseAllWaitingTime() {
        for (Player player : players) {
            player.setWaitingTime(player.getWaitingTime() + 1);
        }
    }

    private boolean matchCheck(Player a, Player b) {
        int ratingA = a.getRating(), ratingB = b.getRating();
        int dt = Math.abs(ratingA - ratingB);
        int watingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return dt <= watingTime * 10;
    }

    private void sendResult(Player a, Player b) {
        System.out.println("send result: " + a.toString() + " " + b.toString());

        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUserId().toString());
        data.add("b_id", b.getUserId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }

    private void matchPlayers() {
        System.out.println("match players: " + players.toString());

        boolean[] used = new boolean[players.size()];
        for (int i = 0; i < players.size(); i ++) {
            if (used[i]) continue;
            for (int j = i + 1; j < players.size(); j ++) {
                if (used[j]) continue;
                Player a = players.get(i), b = players.get(j);
                if (matchCheck(a, b)) {
                    used[i] = used[j] = true;
                    sendResult(a, b);
                    break; // i已经匹配成功了
                }
            }
        }

        List<Player> newPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i ++) {
            if (!used[i]) {
                newPlayers.add(players.get(i));
            }
        }
        MatchingPool.players = newPlayers;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                lock.lock();
                try {
                    increaseAllWaitingTime();
                    matchPlayers();
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
