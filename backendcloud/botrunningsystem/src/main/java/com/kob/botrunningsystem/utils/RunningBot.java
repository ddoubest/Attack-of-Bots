package com.kob.botrunningsystem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class RunningBot implements java.util.function.Supplier<Integer> {
    public static int INT = 0x3f3f3f3f;
    public static int[][] path;
    public static int[][] g = new int[13][14];
    public static int pathLen = -1;
    public static boolean flag = true;
    public static int nextDirection = -1;

    static class Cell {//蛇身体（单格）
        public int x, y;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private boolean check_tail_increasing(int step) {//检查蛇什么时候会变长
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    public List<Cell> getCells(int sx, int sy, String steps) {//获取游戏中两条蛇的身体位置
        steps = steps.substring(1, steps.length() - 1);
        List<Cell> res = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x, y));
        for (int i = 0; i < steps.length(); i++) {
            int d = steps.charAt(i) - '0';
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x, y));
            if (!check_tail_increasing(++step)) {
                res.remove(0);
            }
        }
        return res;
    }

    public Integer nextMove(String input) {
        String[] strs = input.split("#");
        for (int i = 0, k = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++, k++) {
                if (strs[0].charAt(k) == '1') {//找到地图中所有的墙
                    g[i][j] = 1;//1：障碍物，0：空地
                }
            }
        }

        int aSx = Integer.parseInt(strs[1]), aSy = Integer.parseInt(strs[2]);
        int bSx = Integer.parseInt(strs[4]), bSy = Integer.parseInt(strs[5]);

        List<Cell> aCells = getCells(aSx, aSy, strs[3]);
        List<Cell> bCells = getCells(bSx, bSy, strs[6]);

        for (Cell c : aCells) g[c.x][c.y] = 2;//将地图中两条蛇身体的位置标记成障碍物
        for (Cell c : bCells) g[c.x][c.y] = 3;

        //        a蛇头坐标
        int aHeadX = aCells.get(aCells.size() - 1).x;
        int aHeadY = aCells.get(aCells.size() - 1).y;
        //        b蛇头坐标
        int bHeadX = bCells.get(bCells.size() - 1).x;
        int bHeadY = bCells.get(bCells.size() - 1).y;

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

        //顶点数
        int vertex = 13 * 14;
        //边数
        int edge = 0;

        int[][] matrix = new int[vertex][vertex];
        //初始化邻接矩阵
        for (int i = 0; i < vertex; i++) {
            for (int j = 0; j < vertex; j++) {
                matrix[i][j] = INT;
            }
        }

        //初始化路径数组
        path = new int[matrix.length][matrix.length];

        //初始化边权值
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 14; j++) {
                if (g[i][j] == 1 || g[i][j] == 2) continue;

                int dxx = 0, dyy = 1;
                int mx = i + dxx, my = j + dyy;
                if (my < 14) {
                    buidPath(matrix, i, j, mx, my);
                }

                dxx = 1;
                dyy = 0;
                mx = i + dxx;
                my = j + dyy;
                if (mx < 13) {
                    buidPath(matrix, i, j, mx, my);
                }

            }
        }

        for (int i = 0; i < 4; i++) {
            int mx = aHeadX + dx[i], my = aHeadY + dy[i];
            if (g[mx][my] == 0) {
                matrix[aHeadX * 14 + aHeadY][mx * 14 + my] = 1;
            } else {
                matrix[aHeadX * 14 + aHeadY][mx * 14 + my] = INT;
                matrix[aHeadX * 14 + aHeadY][mx * 14 + my] = INT;
            }
        }

        //调用算法计算最短路径
        floyd(matrix, aHeadX * 14 + aHeadY);

        if (nextDirection != -1) return nextDirection;

        for (int i = 0; i < 4; i++) {
            int x = aCells.get(aCells.size() - 1).x + dx[i];
            int y = aCells.get(aCells.size() - 1).y + dy[i];
            if (x >= 0 && x < 13 && y >= 0 && y < 14 && g[x][y] == 0) {
                return i;//选择一个合法的方向前进一格
            }
        }

        return 0;
    }

    private void buidPath(int[][] matrix, int i, int j, int mx, int my) {
        if (g[mx][my] == 0 && g[i][j] == 0) {
            matrix[i * 14 + j][mx * 14 + my] = 1;
            matrix[mx * 14 + my][i * 14 + j] = 1;
        } else if (g[mx][my] == 3 && g[i][j] == 0) {
            matrix[i * 14 + j][mx * 14 + my] = 1;
        } else if (g[mx][my] == 0 && g[i][j] == 3) {
            matrix[mx * 14 + my][i * 14 + j] = 1;
        }
    }

    public static void floyd(int[][] matrix, Integer sources) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                path[i][j] = -1;
            }
        }

        for (int m = 0; m < matrix.length; m++) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    if (matrix[i][m] + matrix[m][j] < matrix[i][j]) {
                        matrix[i][j] = matrix[i][m] + matrix[m][j];
                        //记录经由哪个点到达
                        path[i][j] = m;
                    }
                }
            }
        }

        int minLength = INT, position = -1;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (i != j && i == sources && g[j / 14][j % 14] == 3) {
                    if (matrix[i][j] == INT) {
                        ;
                    } else {
                        findPath(i, j);
                        if (matrix[i][j] < minLength) {
                            minLength = matrix[i][j];
                            position = pathLen;
                        }
                    }
                }
            }
        }
        if (minLength != INT) {
            int headX = sources / 14, headY = sources % 14;
            int nextX = position / 14, nextY = position % 14;
            int dx = nextX - headX, dy = nextY - headY;
            if (dx == -1 && dy == 0) {
                nextDirection = 0;
            } else if (dx == 0 && dy == 1) {
                nextDirection = 1;
            } else if (dx == 1 && dy == 0) {
                nextDirection = 2;
            } else if (dx == 0 && dy == -1) {
                nextDirection = 3;
            }
        }
    }

    public static void findPath(int i, int j) {
        int m = path[i][j];
        if (m == -1) {
            return;
        }

        findPath(i, m);
        if (flag) {
            pathLen = m;
            flag = false;
        }

        findPath(m, j);
    }

    @Override
    public Integer get() {
        File file = new File("input.txt");
        try {
            Scanner sc = new Scanner(file);
            return nextMove(sc.next());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

