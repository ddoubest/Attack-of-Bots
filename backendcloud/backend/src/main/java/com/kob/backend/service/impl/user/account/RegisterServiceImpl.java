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

@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

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
        user.setPhoto("https://www.acwing.com/user/profile/index/");
        user.setRating(1500);

        userMapper.insert(user);

        map.put("error_message", "success");
        return map;
    }
}
