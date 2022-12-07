package com.kob.backend.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.security.core.parameters.P;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {
    public static final int[] dx = {-1, 0, 1, 0};
    public static final int[] dy = {0, 1, 0, -1};
    private static final String addBotUrl = "http://127.0.0.1:3002/bot/add/";


    private final int rows;
    private final int cols;
    private final int inner_walls_count;
    private final int[][] g;
    private final Random random;
    private final Player playerA;
    private final Player playerB;
    private Integer nextStepA = null;
    private Integer nextStepB = null;
    private final ReentrantLock lock = new ReentrantLock();
    private String status = "playing"; // playing -> finished
    private String loser = ""; // all:平局, A, B

    public Game(int rows, int cols, int inner_walls_count, Integer idA, Bot botA, Integer idB, Bot botB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        this.random = new Random();

        Integer botIdA = -1, botIdB = -1;
        String botCodeA = "", botCodeB = "";

        if (botA != null) {
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }

        if (botB != null) {
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }

        this.playerA = new Player(idA, botIdA, botCodeA, rows - 2, 1, new ArrayList<>());
        this.playerB = new Player(idB, botIdB, botCodeB, 1, cols - 2, new ArrayList<>());

        createMap();
    }

    public int[][] getG() {
        return g;
    }

    private boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i ++) {
            int nx = sx + dx[i], ny = sy + dy[i];
            if (nx < 0 || nx >= rows || ny < 0 || ny >= cols) continue;
            if (g[nx][ny] == 1) continue;
            if (check_connectivity(nx, ny, tx, ty)) {
                g[sx][sy] = 0;
                return true;
            }
        }

        g[sx][sy] = 0;
        return false;
    }

    private boolean tryGetMap() {
        for (int r = 0; r < rows; r ++)
            Arrays.fill(g[r], 0);

        for (int r = 0; r < rows; r ++)
            g[r][cols - 1] = g[r][0] = 1;

        for (int c = 0; c < cols; c ++)
            g[0][c] = g[rows - 1][c] = 1;

        for (int i = 0; i < inner_walls_count / 2; i ++) {
            for (int j = 0; j < 1000; j ++) {
                int r = random.nextInt(rows), c = random.nextInt(cols);
                if (g[r][c] == 1 || g[rows - r - 1][cols - c - 1] == 1) continue;
                if (r == 1 && c == cols - 2 || r == rows - 2 && c == 1) continue;

                g[r][c] = g[rows - r - 1][cols - c - 1] = 1;
                break;
            }
        }

        return check_connectivity(rows - 2, 1, 1, cols - 2);
    }

    public void createMap() {
        for (int i = 0; i < 1000; i ++) {
            if (tryGetMap())
                break;
        }
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    /**
     * 返回当前游戏的局面信息
     * @param player 操作本体 me
     * @return 地图字符串编码#我的sx#我的sy#(我的操作)#对手的sx#对手的sy#(对手的操作)
     */
    private String getGameStatus(Player player) {
        Player me, you;
        if (playerA.getUserId().equals(player.getUserId())) {
            me = playerA;
            you = playerB;
        }else {
            me = playerB;
            you = playerA;
        }

        return getMapString() + "#" +
                me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepsString() + ")#" +
                you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepsString() + ")";
    }

    private void sendBotCode(Player player) {
        if (player.getBotId().equals(-1)) {
            return;
        }
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getUserId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("game_status", getGameStatus(player));
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);
    }

    private boolean nextStep() { // 等待两名玩家的下一步操作
        try {
            // 为什么先睡250ms？因为前端需要200ms移动到下一格，期间所有操作全部丢失，而后端却保存了所有操作。前后端不统一了。
            // 睡200ms以上，是为了保证此时后端向前端传移动指令时，前端的蛇一定处于idle状态。
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendBotCode(playerA);
        sendBotCode(playerB);

        for (int i = 0; i < 50; i ++) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        nextStepA = null;
                        nextStepB = null;
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void sendMessageToAllPlayer(String message) {
        if (WebSocketServer.user_connections.get(playerA.getUserId()) != null) {
            WebSocketServer.user_connections.get(playerA.getUserId()).sendMessage(message);
        }
        if (WebSocketServer.user_connections.get(playerB.getUserId()) != null) {
            WebSocketServer.user_connections.get(playerB.getUserId()).sendMessage(message);
        }
    }

    private String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i ++) {
            for (int j = 0; j < cols; j++) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    private void updateUserRating(Player player, Integer rating) {
        User user = WebSocketServer.userMapper.selectById(player.getUserId());
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }

    private void saveToDatabase() {
        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getUserId()).getRating();
        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getUserId()).getRating();

        if ("A".equals(loser)) {
            updateUserRating(playerA, ratingA - 2);
            updateUserRating(playerB, ratingB + 5);
        }else if ("B".equals(loser)) {
            updateUserRating(playerA, ratingA + 5);
            updateUserRating(playerB, ratingB - 2);
        }

        Record record = new Record(
                null,
                playerA.getUserId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getUserId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );

        WebSocketServer.recordMapper.insert(record);
    }

    private void sendResult() {
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDatabase();
        sendMessageToAllPlayer(resp.toJSONString());
    }

    private void sendMove() {
        JSONObject resp = new JSONObject();
        resp.put("event", "move");
        resp.put("a_direction", playerA.getSteps().get(playerA.getSteps().size() - 1));
        resp.put("b_direction", playerB.getSteps().get(playerB.getSteps().size() - 1));
        sendMessageToAllPlayer(resp.toJSONString());
    }

    private boolean check_valid(List<Cell> cells1, List<Cell> cells2) {
        int n = cells1.size();
        Cell checkCell = cells1.get(n - 1);

        if (g[checkCell.getX()][checkCell.getY()] == 1) return false;

        for (int i = 0; i < n - 1; i ++) {
            Cell cell = cells1.get(i);
            if (checkCell.getX().equals(cell.getX()) && checkCell.getY().equals(cell.getY()))
                return false;
        }

        for (int i = 0; i < n - 1; i ++) {
            Cell cell = cells2.get(i);
            if (checkCell.getX().equals(cell.getX()) && checkCell.getY().equals(cell.getY()))
                return false;
        }

        return true;
    }

    private void judge() {
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);

        if (!validA || !validB) {
            status = "finished";
            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i ++) {
            if (nextStep()) {
                judge();
                if ("playing".equals(status)) {
                    sendMove();
                }else {
                    sendResult();
                    break;
                }
            }else {
                status = "finished";
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else if (nextStepB == null) {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();
                break;
            }
        }
    }

    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }
}
