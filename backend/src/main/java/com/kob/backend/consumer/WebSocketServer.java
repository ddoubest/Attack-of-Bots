package com.kob.backend.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.utils.GameMap;
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
    private static final ConcurrentHashMap<Integer, WebSocketServer> user_connections = new ConcurrentHashMap<>();
    private static UserMapper userMapper;
    private static final CopyOnWriteArraySet<User> match_pool = new CopyOnWriteArraySet<>();

    private Session session;
    private User user;

    @Autowired
    private void getUserMapper(UserMapper userMapper) {
        WebSocketServer.userMapper = userMapper;
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

            GameMap gameMap = new GameMap(13, 14, 20);
            gameMap.createMap();

            JSONObject resp2A = new JSONObject();
            resp2A.put("event", "match-success");
            resp2A.put("opponent_photo", b.getPhoto());
            resp2A.put("opponent_username", b.getUsername());
            resp2A.put("gamemap", gameMap.getG());
            user_connections.get(a.getId()).sendMessage(resp2A.toJSONString());

            JSONObject resp2B = new JSONObject();
            resp2B.put("event", "match-success");
            resp2B.put("opponent_photo", a.getPhoto());
            resp2B.put("opponent_username", a.getUsername());
            resp2B.put("gamemap", gameMap.getG());
            user_connections.get(b.getId()).sendMessage(resp2B.toJSONString());
        }
    }

    private void stopMathcing() {
        System.out.println("stop matching!");
        match_pool.remove(user);
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
