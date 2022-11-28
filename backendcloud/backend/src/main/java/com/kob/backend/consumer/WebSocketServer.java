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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
    public static RecordMapper recordMapper;
    private static RestTemplate restTemplate;

    private static final String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private static final String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";

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
    @Autowired
    private void getRestTemplate(RestTemplate restTemplate) {
        WebSocketServer.restTemplate = restTemplate;
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
        }
    }

    public static void startGame(Integer aId, Integer bId) {
        User a = userMapper.selectById(aId), b = userMapper.selectById(bId);

        Game game = new Game(13, 14, 20, a.getId(), b.getId());
        game.createMap();
        if (user_connections.get(a.getId()) != null) {
            user_connections.get(a.getId()).game = game;
        }
        if (user_connections.get(b.getId()) != null) {
            user_connections.get(b.getId()).game = game;
        }

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
        if (user_connections.get(a.getId()) != null) {
            user_connections.get(a.getId()).sendMessage(resp2A.toJSONString());
        }

        JSONObject resp2B = new JSONObject();
        resp2B.put("event", "match-success");
        resp2B.put("opponent_photo", a.getPhoto());
        resp2B.put("opponent_username", a.getUsername());
        resp2B.put("game", respGame);
        if (user_connections.get(b.getId()) != null) {
            user_connections.get(b.getId()).sendMessage(resp2B.toJSONString());
        }
    }

    private void startMathcing() {
        System.out.println("start mathcing!");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", user.getId().toString());
        data.add("rating", user.getRating().toString());
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    private void stopMathcing() {
        System.out.println("stop matching!");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
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
