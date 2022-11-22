package com.kob.backend.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.Game;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    public static final ConcurrentHashMap<Integer, WebSocketServer> user_connections = new ConcurrentHashMap<>();
    private static UserMapper userMapper;
    private static final CopyOnWriteArraySet<User> match_pool = new CopyOnWriteArraySet<>();
    public static RecordMapper recordMapper;

    private Session session;
    private User user;
    private Game game;

    @Autowired
    private void getUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
    }
    @Autowired
    private void getRecordMapper(RecordMapper recordMapper) {
        WebSocketServer.recordMapper = recordMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接
        this.session = session;
        System.out.println("connected!");

        String userId;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        user = userMapper.selectById(Integer.parseInt(userId));

        if (user == null) {
            this.session.close();
            return;
        }

        System.out.println(user);
        user_connections.put(user.getId(), this);
        System.out.println(user_connections);
    }

    @OnClose
    public void onClose() {
        // 关闭链接
        System.out.println("disconnected!");
        if (user != null) {
            user_connections.remove(user.getId());
            match_pool.remove(user);
        }
    }

    private void startMathcing() {
        System.out.println("start mathcing!");
        match_pool.add(user);

        while (match_pool.size() >= 2) {
            Iterator<User> iterator = match_pool.iterator();
            User a = iterator.next(), b = iterator.next();
            match_pool.remove(a);
            match_pool.remove(b);

            Game game = new Game(13, 14, 20, a.getId(), b.getId());
            game.createMap();

            user_connections.get(a.getId()).game = game;
            user_connections.get(b.getId()).game = game;

            game.start();

            JSONObject respGame = new JSONObject();
            respGame.put("a_id", game.getPlayerA().getUserId());
            respGame.put("a_sx", game.getPlayerA().getSx());
            respGame.put("a_sy", game.getPlayerA().getSy());
            respGame.put("b_id", game.getPlayerB().getUserId());
            respGame.put("b_sx", game.getPlayerB().getSx());
            respGame.put("b_sy", game.getPlayerB().getSy());
            respGame.put("gamemap", game.getG());

            JSONObject resp2A = new JSONObject();
            resp2A.put("event", "match-success");
            resp2A.put("opponent_photo", b.getPhoto());
            resp2A.put("opponent_username", b.getUsername());
            resp2A.put("game", respGame);
            user_connections.get(a.getId()).sendMessage(resp2A.toJSONString());

            JSONObject resp2B = new JSONObject();
            resp2B.put("event", "match-success");
            resp2B.put("opponent_photo", a.getPhoto());
            resp2B.put("opponent_username", a.getUsername());
            resp2B.put("game", respGame);
            user_connections.get(b.getId()).sendMessage(resp2B.toJSONString());
        }
    }

    private void stopMathcing() {
        System.out.println("stop matching!");
        match_pool.remove(user);
    }

    private void updateMoveDirection(int direction) {
        if (game.getPlayerA().getUserId().equals(user.getId())) {
            game.setNextStepA(direction);
        } else if (game.getPlayerB().getUserId().equals(user.getId())) {
            game.setNextStepB(direction);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息
        System.out.println("received message!");

        JSONObject data = JSON.parseObject(message);
        String event = data.getString("event");
        if ("start-matching".equals(event)) {
            startMathcing();
        }else if ("stop-matching".equals(event)) {
            stopMathcing();
        }else if ("move_direction".equals(event)) {
            updateMoveDirection(data.getInteger("direction"));
        }

    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) {
        synchronized (this) {
            try {
                this.session.getBasicRemote().sendText(message);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
