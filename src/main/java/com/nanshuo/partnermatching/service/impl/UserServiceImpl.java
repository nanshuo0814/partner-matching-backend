package com.nanshuo.partnermatching.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nanshuo.partnermatching.common.ErrorCode;
import com.nanshuo.partnermatching.constant.RedisKeyConstant;
import com.nanshuo.partnermatching.constant.UserConstant;
import com.nanshuo.partnermatching.exception.BusinessException;
import com.nanshuo.partnermatching.mapper.UserMapper;
import com.nanshuo.partnermatching.model.domain.User;
import com.nanshuo.partnermatching.model.request.user.UserLoginRequest;
import com.nanshuo.partnermatching.model.request.user.UserRegisterRequest;
import com.nanshuo.partnermatching.model.request.user.UserUpdateInfoRequest;
import com.nanshuo.partnermatching.model.vo.user.UserLoginVO;
import com.nanshuo.partnermatching.service.UserService;
import com.nanshuo.partnermatching.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.nanshuo.partnermatching.constant.UserConstant.USER_LOGIN_STATE;


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
            user.setUsername(UserConstant.DEFAULT_USER_NAME + System.currentTimeMillis());
            user.setAvatarUrl(UserConstant.DEFAULT_USER_AVATAR);
            user.setGender(UserConstant.DEFAULT_USER_GENDER);
            user.setUserRole(UserConstant.USER_ROLE);
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
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
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
     * 获取用户信息（脱敏）
     *
     * @param user 用户
     * @return {@code UserVO}
     */
    @Override
    public UserLoginVO getUserSafetyVO(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVO userSafetyVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userSafetyVO);
        return userSafetyVO;
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
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
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
        redisTemplate.opsForValue().set(cacheKey, user, UserConstant.SAVE_USER_TO_CACHE_TIME, TimeUnit.HOURS);
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
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.FAIL, "未登录");
        }
        // 删除缓存
        redisTemplate.delete(RedisKeyConstant.USER_LOGIN_STATE_CACHE + this.getLoginUser(request).getId());
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return "退出登录成功！";
    }

    // end domain 用户登录相关

    // domain 用户增删改查相关

    /**
     * 按标签搜索用户
     *
     * @param tagNameList 标签名称列表
     * @return {@code List<UserSafetyVO>}
     */
    @Override
    public List<UserLoginVO> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_NULL);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = this.baseMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getUserSafetyVO).collect(Collectors.toList());
    }

    @Override
    public Integer updateUser(UserUpdateInfoRequest user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User oldUser = this.baseMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUserAccount(!ObjectUtils.isEmpty(user.getUserAccount()) ? user.getUserAccount() : oldUser.getUserAccount());
        updateUser.setUsername(!ObjectUtils.isEmpty(user.getUsername()) ? user.getUsername() : oldUser.getUsername());
        updateUser.setAvatarUrl(!ObjectUtils.isEmpty(user.getAvatarUrl()) ? user.getAvatarUrl() : oldUser.getAvatarUrl());
        updateUser.setEmail(!ObjectUtils.isEmpty(user.getEmail()) ? user.getEmail() : oldUser.getEmail());
        updateUser.setPhone(!ObjectUtils.isEmpty(user.getPhone()) ? user.getPhone() : oldUser.getPhone());
        updateUser.setGender(!ObjectUtils.isEmpty(user.getGender()) ? user.getGender() : oldUser.getGender());
        updateUser.setUserPassword(!ObjectUtils.isEmpty(user.getUserPassword()) ? user.getUserPassword() : oldUser.getUserPassword());
        return this.baseMapper.updateById(updateUser);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && Objects.equals(user.getUserRole(), UserConstant.ADMIN_ROLE);
    }

    /**
     * 匹配用户
     *
     * @param num       num
     * @param loginUser 登录用户
     * @return {@code List<UserLoginVO>}
     */
    @Override
    public List<UserLoginVO> matchUsers(long num, User loginUser) {
        // 查询特定的字段来提高性能,排除不需要的字段
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.select("id", "tags");
        // 查询有标签的用户
        qw.isNotNull("tags");
        List<User> userList = this.list(qw);
        // 获取当前用户的tags标签
        String tags = loginUser.getTags();
        // 使用gson来将tags标签转换为List数组
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User,Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId()))
                continue;
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream().sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num).collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<UserLoginVO>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(this::getUserSafetyVO)
                .collect(Collectors.groupingBy(UserLoginVO::getId));
        List<UserLoginVO> finalUserList = new ArrayList<>();
        // 将 userIdList 中的 userId 与 userIdUserListMap 中的 userIdUserListMap 的 key 相对应
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        // 返回结果
        return finalUserList;
    }

}
