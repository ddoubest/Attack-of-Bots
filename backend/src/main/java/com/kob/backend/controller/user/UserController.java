package com.kob.backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private UserMapper userMapper;

    @RequestMapping("all/")
    public List<User> getAll() {
        return userMapper.selectList(null);
    }

    @RequestMapping("{userId}/")
    public User getById(@PathVariable Integer userId) {
        return userMapper.selectById(userId);
    }

    @RequestMapping("range/{userId1}/{userId2}/")
    public List<User> getByIdRange(@PathVariable Integer userId1, @PathVariable Integer userId2) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("id", userId1).le("id", userId2);
        return userMapper.selectList(queryWrapper);
    }

    @GetMapping("add/{userId}/{username}/{password}/")
    public String addUser(@PathVariable Integer userId, @PathVariable String username, @PathVariable String password) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePwd = passwordEncoder.encode((password));
        User user = new User(userId, username, encodePwd, null);
        userMapper.insert(user);
        return "Add User Successfully!";
    }

    @GetMapping("delete/{userId}/")
    public String deleteUser(@PathVariable Integer userId) {
        userMapper.deleteById(userId);
        return "Delete User Successfully!";
    }
}
