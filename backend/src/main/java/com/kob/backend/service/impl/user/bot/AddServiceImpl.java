package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.bot.AddService;
import com.kob.backend.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> add(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = userDetails.getUser();

        String title = data.get("title");
        String description = data.get("description");
        String content = data.get("content");

        Map<String, String> result = new HashMap<>();

        if (!StringUtils.hasText(title)) {
            result.put("error_message", "标题不能为空");
            return result;
        }

        if (!StringUtils.hasText(content)) {
            result.put("error_message", "代码不能为空");
            return result;
        }

        if (!StringUtils.hasText(description))
            description = "这个人很懒，什么都没留下~";

        if (title.length() > 100) {
            result.put("error_message", "Bot标题长度不能超过100");
            return result;
        }

        if (description.length() > 300) {
            result.put("error_message", "Bot描述长度不能超过300");
            return result;
        }

        if (content.length() > 10000) {
            result.put("error_message", "Bot代码长度不能超过10000");
            return result;
        }

        Date curDate = new Date();
        Bot bot = new Bot(null, user.getId(), title, description, content, null, curDate, curDate);

        botMapper.insert(bot);
        result.put("error_message", "success");

        return result;
    }
}
