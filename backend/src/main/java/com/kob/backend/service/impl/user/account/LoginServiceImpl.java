package com.kob.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.account.LoginService;
import com.kob.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserMapper userMapper;

    @Override
    public Map<String, String> getToken(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        try {
            authenticationManager.authenticate(authenticationToken); // 相当于去数据库认证了，原理未知
        }catch (Exception e) {
            Map<String, String> map = new HashMap<>();
            map.put("error_message", "failure");
            return map;
        }

        /* y总写的，强制类型转换出错
            UserDetailsImpl userDetails = (UserDetailsImpl) authenticationToken.getPrincipal();
            User user = userDetails.getUser();
        */
        String authenticateUesrname = (String) authenticationToken.getPrincipal();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", authenticateUesrname);
        User user = userMapper.selectOne(queryWrapper);
        String jwtToken = JwtUtil.createJWT(user.getId().toString());

        Map<String, String> map = new HashMap<>();
        map.put("error_message", "success");
        map.put("token", jwtToken);

        return map;
    }
}
