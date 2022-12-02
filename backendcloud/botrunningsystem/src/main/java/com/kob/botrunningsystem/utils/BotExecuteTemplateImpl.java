package com.kob.botrunningsystem.utils;

import java.util.*;

public class BotExecuteTemplateImpl implements com.kob.botrunningsystem.utils.BotExecuteTemplate{
    private static final int rows = 13, cols = 14;
    private static final int[] dx = {-1, 0, 1, 0};
    private static final int[] dy = {0, 1, 0, -1};

    private int[][] g;
    private List<Cell> meSnake;
    private List<Cell> youSnake;

    private static class Cell {
        int x, y;
        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private int[][] getMap(String map) {
        int[][] g = new int[rows][cols];
        for (int i = 0, k = 0; i < rows; i ++) {
            for (int j = 0; j < cols; j ++, k ++) {
                if (map.charAt(k) == '1') {
                    g[i][j] = 1;
                }
            }
        }
        return g;
    }

    private boolean check_snake_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    private List<Cell> getCells(int x, int y, String directions) {
        List<Cell> snake = new ArrayList<>();
        snake.add(new Cell(x, y));
        int step = 0;
        for (char ch : directions.toCharArray()) {
            int d = ch - '0';
            int nx = x + dx[d], ny = y + dy[d];
            snake.add(new Cell(nx, ny));
            x = nx;
            y = ny;
            step ++;
            if (!check_snake_increasing(step)) {
                snake.remove(0);
            }
        }
        return snake;
    }

    private void parseGameStatus(String[] args) {
        g = getMap(args[0]);
        int meSx = Integer.parseInt(args[1]);
        int meSy = Integer.parseInt(args[2]);
        meSnake = getCells(meSx, meSy, args[3].substring(1, args[3].length() - 1));
        int youSx = Integer.parseInt(args[4]);
        int youSy = Integer.parseInt(args[5]);
        youSnake = getCells(youSx, youSy, args[6].substring(1, args[6].length() - 1));
    }

    private boolean checkValid(int nextDirection) {
        Cell head = meSnake.get(meSnake.size() - 1);
        int nx = head.x + dx[nextDirection], ny = head.y + dy[nextDirection];

        if (nx < 0 || nx >= rows || ny < 0 || ny >= cols) return false;
        if (g[nx][ny] == 1) return false;

        for (Cell cell : meSnake) {
            if (cell.x == nx && cell.y == ny)
                return false;
        }

        for (Cell cell : youSnake) {
            if (cell.x == nx && cell.y == ny)
                return false;
        }

        return true;
    }

    @Override
    public Integer nextMove(String gameStatus) {
        String[] args = gameStatus.split("#");
        parseGameStatus(args);

        List<Integer> tryDirections = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(tryDirections);

        for (Integer d : tryDirections) {
            if (checkValid(d)) {
                return d;
            }
        }

        return 0;
    }
}
