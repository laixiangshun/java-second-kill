package com.java.secondskill.servers;

import com.java.secondskill.beans.User;
import com.java.secondskill.constants.Common;
import com.java.secondskill.exception.GlobalException;
import com.java.secondskill.mapper.UserMapper;
import com.java.secondskill.redis.UserKey;
import com.java.secondskill.result.CodeMsg;
import com.java.secondskill.utils.MD5Utils;
import com.java.secondskill.utils.UUIDUtil;
import com.java.secondskill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据token获取用户信息
     */
    public User getByToken(HttpServletResponse response, String token) {
        if (org.apache.commons.lang3.StringUtils.isBlank(token)) {
            return null;
        }
        User user = redisService.get(UserKey.token, token, User.class);
        if (Objects.nonNull(user)) {
            //延长有效期，有效期等于最后一次操作+有效期
            addCookie(response, token, user);
        }
        return user;

    }

    /**
     * 添加cookie
     */
    private void addCookie(HttpServletResponse response, String token, User user) {
        redisService.set(UserKey.token, token, user);
        Cookie cookie = new Cookie(Common.COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(UserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 登录
     */
    public String login(HttpServletResponse response, LoginVo loginVo) {
        if (Objects.isNull(loginVo)) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        User user = getUserById(Long.valueOf(mobile));
        if (Objects.isNull(user)) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String dbPaw = user.getPassword();
        String salt = user.getSalt();
        String calcPass = MD5Utils.formPassToDBPass(password, salt);
        if (!dbPaw.equals(calcPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成全局id作为token
        String uuid = UUIDUtil.uuid();
        addCookie(response, uuid, user);
        return uuid;
    }

    private User getUserById(long id) {
        User user = redisService.get(UserKey.getById, "" + id, User.class);
        if (Objects.nonNull(user)) {
            return user;
        }
        user = userMapper.getById(id);
        if (Objects.nonNull(user)) {
            redisService.set(UserKey.getById, "" + id, user);
        }
        return user;
    }
}
