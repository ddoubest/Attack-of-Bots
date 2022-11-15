package com.kob.backend.service.impl.user.bot;

import com.kob.backend.mapper.BotMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import com.kob.backend.service.user.bot.UpdateService;
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
public class UpdateServiceImpl implements UpdateService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> update(Map<String, String> data) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authenticationToken.getPrincipal();
        User user = userDetails.getUser();

        Integer botId = Integer.parseInt(data.get("bot_id"));
        Bot bot = botMapper.selectById(botId);

        Map<String, String> result = new HashMap<>();

        if (bot == null) {
            result.put("error_message", "需要更新的Bot不存在或已被删除");
            return result;
        }

        if (!bot.getUserId().equals(user.getId())) {
            result.put("error_message", "您没有权限更新该Bot");
            return result;
        }

        String title = data.get("title");
        String description = data.get("description");
        String content = data.get("content");

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

        Bot newBot = new Bot(bot.getId(), bot.getUserId(), title, description, content, bot.getRating(), bot.getCreatetime(), new Date());
        botMapper.updateById(newBot);

        result.put("error_message", "success");
        return result;
    }
}
