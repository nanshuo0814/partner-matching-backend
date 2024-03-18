package com.nanshuo.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nanshuo.springboot.common.ErrorCode;
import com.nanshuo.springboot.constant.PageConstant;
import com.nanshuo.springboot.constant.RedisKeyConstant;
import com.nanshuo.springboot.constant.UserConstant;
import com.nanshuo.springboot.exception.BusinessException;
import com.nanshuo.springboot.exception.ThrowUtils;
import com.nanshuo.springboot.mapper.UserMapper;
import com.nanshuo.springboot.model.domain.User;
import com.nanshuo.springboot.model.request.user.*;
import com.nanshuo.springboot.model.vo.user.UserLoginVO;
import com.nanshuo.springboot.model.vo.user.UserSafety;
import com.nanshuo.springboot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户服务实现
 *
 * @author nanshuo
 * @date 2023/12/23 16:30:17
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RedisTemplate<String, Object> redisTemplate;

    public UserServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册 Request
     * @return long
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 获取参数
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        // 确认密码校验
        if (checkPassword != null && !checkPassword.equals(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        synchronized (userAccount.intern()) {
            // 账户不能重复
            LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
            qw.eq(User::getUserAccount, userAccount);
            long count = this.baseMapper.selectCount(qw);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已被注册,请重新输入一个");
            }

            // 星球编号不能重复
            qw = new LambdaQueryWrapper<>();
            qw.eq(User::getPlanetCode, planetCode);
            count = this.baseMapper.selectCount(qw);
            if (count > 0) {
                throw new BusinessException(ErrorCode.FAIL, "星球编号重复");
            }

            // MD5加密
            String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());

            // 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setPlanetCode(planetCode);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，系统内部错误");
            }
            return user.getId();
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录Request
     * @return {@code UserLoginVO}
     */
    @Override
    public UserLoginVO userLogin(HttpServletRequest request, UserLoginRequest userLoginRequest) {

        // 获取参数
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        // 校验图片验证码
        Object trueImageCaptcha = redisTemplate.opsForValue().get(RedisKeyConstant.IMAGE_CAPTCHA_KEY);
        if (ObjectUtils.isEmpty(trueImageCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片验证码已过期,请重新获取");
        }

        // 查询用户信息
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "账号或密码错误");
        }

        if (user.getUserRole().equals(UserConstant.BAN_ROLE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用,请联系管理员解封");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword).getBytes());
        if (!encryptPassword.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 记录用户的登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 缓存用户信息
        redisTemplate.opsForValue().set(RedisKeyConstant.USER_LOGIN_STATE_CACHE + user.getId(), user);

        // 返回用户登录信息
        return this.getLoginUserVO(user);
    }

    /**
     * 获取登录用户vo
     *
     * @param user 用户
     * @return {@code UserLoginVO}
     */
    @Override
    public UserLoginVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    /**
     * 获取登录用户
     *
     * @param request 请求
     * @return {@code User}
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {

        // 先判断是否已登录,获取用户信息
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 尝试从缓存redis中通过用户id获取用户信息
        User cachedUser = this.getUserCacheById(user.getId());

        if (cachedUser == null) {
            // 缓存中不存在，从数据库查询
            cachedUser = this.getById(user.getId());

            if (cachedUser != null) {
                // 将用户信息放入缓存
                this.saveUserToCache(cachedUser);
            } else {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
        }
        // 返回用户信息
        return cachedUser;
    }

    /**
     * 按id获取用户缓存
     *
     * @param userId 用户id
     * @return {@code User}
     */
    @Override
    public User getUserCacheById(Long userId) {
        String cacheKey = RedisKeyConstant.USER_LOGIN_STATE_CACHE + userId;
        return (User) redisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * 将用户保存到缓存
     *
     * @param user 用户
     */
    @Override
    public void saveUserToCache(User user) {
        String cacheKey = RedisKeyConstant.USER_LOGIN_STATE_CACHE + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user, 1, TimeUnit.HOURS);
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return {@code Boolean}
     */
    @Override
    public String userLogout(HttpServletRequest request) {
        // 判断是否已登录
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.FAIL, "未登录");
        }
        // 删除缓存
        redisTemplate.delete(RedisKeyConstant.USER_LOGIN_STATE_CACHE + this.getLoginUser(request).getId());
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return "退出登录成功！";
    }


    /**
     * 验证电子邮件代码
     *
     * @param email        电子邮件
     * @param emailCaptcha 电子邮件验证码
     */
    private void validateEmailCode(String email, String emailCaptcha) {
        Object trueEmailCaptcha = redisTemplate.opsForValue().get(RedisKeyConstant.EMAIL_CAPTCHA_KEY + email);
        if (ObjectUtils.isEmpty(trueEmailCaptcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱验证码已过期,请重新获取");
        }
        if (!emailCaptcha.equals(trueEmailCaptcha.toString())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的邮箱验证码");
        }
    }

    // end domain 用户登录相关

    // domain 用户增删改查相关

    /**
     * 获取用户vo
     *
     * @param user 用户
     * @return {@code UserVO}
     */
    @Override
    public UserSafety getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserSafety userSafety = new UserSafety();
        BeanUtils.copyProperties(user, userSafety);
        return userSafety;
    }
}
