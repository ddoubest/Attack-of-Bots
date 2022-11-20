package com.kob.backend.utils;

import java.util.Arrays;
import java.util.Random;

public class GameMap {
    private final int rows;
    private final int cols;
    private final int inner_walls_count;
    private final int[][] g;
    private final Random random;
    public static final int[] dx = {-1, 0, 1, 0};
    public static final int[] dy = {0, 1, 0, -1};

    public GameMap(int rows, int cols, int inner_walls_count) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        this.random = new Random();
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
}
