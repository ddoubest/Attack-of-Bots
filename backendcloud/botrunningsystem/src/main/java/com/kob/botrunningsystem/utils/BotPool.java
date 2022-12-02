package com.kob.botrunningsystem.utils;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition condition = lock.newCondition();
    private static final Queue<Bot> bots = new ArrayDeque<>();

    public void addBot(Integer botId, String botCode, String gameStatus) {
        Bot bot = new Bot(botId, botCode, gameStatus);
        lock.lock();
        try {
            bots.add(bot);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void consume(Bot bot) {
        BotExecutor botExecutor = new BotExecutor();
        botExecutor.startTimeOut(2000, bot);
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            if (bots.isEmpty()) {
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            }else {
                Bot bot = bots.poll();
                lock.unlock();
                consume(bot); // 比较耗时
            }
        }
    }
}
