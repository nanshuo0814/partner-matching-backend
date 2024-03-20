package com.nanshuo.partnermatching.constant;

/**
 * 用户常量
 *
 * @author nanshuo
 * @date 2024/01/03 19:33:58
 */
public interface UserConstant {


    /**
     * 盐值，混淆密码
     */
    String SALT = "ydg0814";

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";


    /**
     * 默认角色
     */
    String USER_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    /**
     * 默认用户昵称
     */
    String DEFAULT_USER_NAME = "user";

    /**
     * 默认用户头像
     */
    String DEFAULT_USER_AVATAR = "https://img.ydg.icu/nanshuo.png";

    /**
     * 默认用户性别(0:女 1:男 2:未知)
     */
    Integer DEFAULT_USER_GENDER = 2;

    /**
     * 将用户保存到缓存时间
     */
    Integer SAVE_USER_TO_CACHE_TIME = 1;
}
