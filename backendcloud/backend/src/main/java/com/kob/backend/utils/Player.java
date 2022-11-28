package com.kob.backend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private Integer userId;
    private Integer sx;
    private Integer sy;
    private List<Integer> steps;

    private boolean check_snake_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    public List<Cell> getCells() {
        List<Cell> cells = new ArrayList<>();
        Integer x = sx, y = sy;
        int step = 0;
        cells.add(new Cell(x, y));
        for (Integer d : steps) {
            x = x + Game.dx[d];
            y = y + Game.dy[d];
            cells.add(new Cell(x, y));
            step ++;
            if (!check_snake_increasing(step)) {
                cells.remove(0);
            }
        }
        return cells;
    }

    public String getStepsString() {
        StringBuilder res = new StringBuilder();
        for (int d : steps)
            res.append(d);
        return res.toString();
    }
}
