package com.kob.backend.service.impl.pk;

import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.service.pk.ReceiveBotMoveService;
import com.kob.backend.utils.Game;
import org.springframework.stereotype.Service;

@Service
public class ReceiveBotMoveServiceImpl implements ReceiveBotMoveService {
    @Override
    public String receiveBotMove(Integer userId, Integer direction) {
        System.out.println("receive bot move: " + userId + " " + direction);
        WebSocketServer webSocketServer = WebSocketServer.user_connections.get(userId);
        if (webSocketServer != null && webSocketServer.getGame() != null) {
            Game game = webSocketServer.getGame();
            if (game.getPlayerA().getUserId().equals(userId)) {
                game.setNextStepA(direction);
            } else if (game.getPlayerB().getUserId().equals(userId)) {
                game.setNextStepB(direction);
            }
        }
        return "receive bot move success!";
    }
}
