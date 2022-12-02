package com.kob.botrunningsystem.service.impl;

import com.kob.botrunningsystem.service.BotRunningService;
import com.kob.botrunningsystem.utils.BotPool;
import org.springframework.stereotype.Service;

@Service
public class BotRunningServiceImpl implements BotRunningService {
    public static final BotPool botPool = new BotPool();

    @Override
    public String addBot(Integer userId, String botCode, String gameStatus) {
        System.out.println("add bot: " + userId + " " + botCode + " " + gameStatus);
        botPool.addBot(userId, botCode, gameStatus);
        return "add bot success!";
    }
}
