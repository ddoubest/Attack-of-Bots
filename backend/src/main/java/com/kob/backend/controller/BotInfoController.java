package com.kob.backend.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pk/")
public class BotInfoController {
    @RequestMapping("getbotinfo/")
    public Map<String, String> getBotInfo() {
        // List<int[]> list = new ArrayList<>();
        // list.add(new int[]{1, 2});
        // list.add(new int[]{3, 4});

        // List<Pair> list = new ArrayList<>(); 报错，因为Pair无法解析
        // list.add(new Pair(2, 3));
        // list.add(new Pair(6, 66));

        Map<String, String> map = new HashMap<>();
        map.put("name", "tiger");
        map.put("rating", "1500");

        return map;
    }
}

// class Pair {
//     int x, y;
//     Pair(int x, int y) {
//         this.x = x;
//         this.y = y;
//     }
// }