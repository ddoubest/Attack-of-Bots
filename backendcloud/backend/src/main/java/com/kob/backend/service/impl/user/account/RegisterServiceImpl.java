package com.kob.backend.service.impl.user.account;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String[] photos = {
            "https://cdn.acwing.com/media/user/profile/photo/28992_lg_b86d2fb279.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/209322_lg_38b11c1e0b.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/121744_lg_65d5c84cca.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/131391_lg_84a52e733c.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/2776_lg_0a28607e56.jpg",
            "https://cdn.acwing.com/media/user/profile/photo/55909_lg_8ccf359b36.jpeg"
    };

    private static final Random RANDOM = new Random();

    @Override
    public Map<String, String> register(String username, String password, String confirmedPassword) {
        Map<String, String> map = new HashMap<>();
        if (!StringUtils.hasText(username)) {
            map.put("error_message", "用户名不能为空");
            return map;
        }
        if (!StringUtils.hasText(password) || !StringUtils.hasText(confirmedPassword)) {
            map.put("error_message", "密码不能为空");
            return map;
        }
        if (username.length() > 100) {
            map.put("error_message", "用户名的长度不能大于100");
            return map;
        }

        if (password.length() > 100) {
            map.put("error_message", "密码的长度不能大于100");
            return map;
        }

        if (!password.equals(confirmedPassword)) {
            map.put("error_message", "两次输入的密码不一致");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            map.put("error_message", "用户名已存在");
            return map;
        }

        user = new User();
        user.setId(null);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        String randomPhoto = photos[RANDOM.nextInt(photos.length)];
        user.setPhoto(randomPhoto);
        user.setRating(1500);

        userMapper.insert(user);

        map.put("error_message", "success");
        return map;
    }
}
