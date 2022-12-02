package com.kob.botrunningsystem.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bot {
    private Integer userId;
    private String botCode;
    private String gameStatus;
}
