package com.nanshuo.partnermatching.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nanshuo.partnermatching.annotation.Check;
import com.nanshuo.partnermatching.annotation.CheckAuth;
import com.nanshuo.partnermatching.annotation.CheckParam;
import com.nanshuo.partnermatching.common.BaseResponse;
import com.nanshuo.partnermatching.common.ErrorCode;
import com.nanshuo.partnermatching.common.ResultUtils;
import com.nanshuo.partnermatching.constant.UserConstant;
import com.nanshuo.partnermatching.exception.BusinessException;
import com.nanshuo.partnermatching.exception.ThrowUtils;
import com.nanshuo.partnermatching.model.domain.User;
import com.nanshuo.partnermatching.model.request.user.UserLoginRequest;
import com.nanshuo.partnermatching.model.request.user.UserRegisterRequest;
import com.nanshuo.partnermatching.model.request.user.UserUpdateInfoRequest;
import com.nanshuo.partnermatching.model.vo.user.UserLoginVO;
import com.nanshuo.partnermatching.model.vo.user.UserSafetyVO;
import com.nanshuo.partnermatching.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.nanshuo.partnermatching.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户控制器
 *
 * @author nanshuo
 * @date 2023/12/23 16:33:46
 */
@Slf4j
@Api(tags = "普通用户模块")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserController(UserService userService, RedisTemplate<String,Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    // domain 用户登录相关

    /**
     * 获取当前用户
     *
     * @param request 请求
     * @return {@code BaseResponse<User>}
     */
    @GetMapping("/current")
    @ApiOperation(value = "获取当前用户", notes = "获取当前用户")
    public BaseResponse<UserLoginVO> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        UserLoginVO safetyUser = userService.getUserSafetyVO(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 删除用户(管理员权限)
     *
     * @param userId 用户id
     * @return {@code BaseResponse<Boolean>}
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除用户", notes = "删除用户")
    @Check(checkParam = true, checkAuth = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> deleteUser(@RequestBody @ApiParam(value = "用户id", required = true) Long userId) {
        ThrowUtils.throwIf(!userService.removeById(userId), ErrorCode.FAIL, "删除用户失败,无该用户");
        return ResultUtils.success(userId);
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册 Request
     * @return {@code BaseResponse<Long>}
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户注册")
    @Check(checkParam = true)
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 调用用户注册服务方法,返回注册结果
        return ResultUtils.success(userService.userRegister(userRegisterRequest));
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录Request
     * @return {@code BaseResponse<UserLoginVO>}
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "用户登录")
    @Check(checkParam = true)
    public BaseResponse<UserLoginVO> userLogin(HttpServletRequest request, @RequestBody UserLoginRequest userLoginRequest) {
        return ResultUtils.success(userService.userLogin(request, userLoginRequest));
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return {@code BaseResponse<Boolean>}
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销", notes = "用户注销")
    public BaseResponse<String> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIfNull(request);
        return ResultUtils.success(userService.userLogout(request));
    }

    // end domain 用户登录相关

    // domain 用户的增删改查相关

    /**
     * 修改用户信息
     *
     * @param request               请求
     * @return {@code BaseResponse<Boolean>}
     */
    @PostMapping("/update")
    @Check(checkParam = true)
    @ApiOperation(value = "修改用户信息", notes = "修改用户信息")
    public BaseResponse<Integer> updateUserInfo(@RequestBody UserUpdateInfoRequest user,
                                               HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 按标签搜索用户
     *
     * @param tagNameList 标签名称列表
     * @return {@code BaseResponse<List<User>>}
     */
    @GetMapping("/search/tags")
    @Check(checkParam = true)
    @ApiOperation(value = "按标签搜索用户", notes = "按标签搜索用户")
    public BaseResponse<List<UserLoginVO>> searchUsersByTags(@RequestParam(required = false)
                                                              List<String> tagNameList) {
        return ResultUtils.success(userService.searchUsersByTags(tagNameList));
    }

    /**
     * 搜索用户
     *
     * @param username 用户名
     * @return {@code BaseResponse<List<UserSafetyVO>>}
     */
    @GetMapping("/search")
    @ApiOperation(value = "搜索用户", notes = "搜索用户")
    @CheckAuth(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<UserLoginVO>> searchUsers(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<UserLoginVO> list = userList.stream().map(userService::getUserSafetyVO).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 推荐用户
     *
     * @param pageSize 页面大小
     * @param pageNum  页码
     * @param request  请求
     * @return {@code BaseResponse<Page<User>>}
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("partner:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 写缓存
        try {
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }



}