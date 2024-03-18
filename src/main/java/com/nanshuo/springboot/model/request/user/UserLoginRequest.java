package com.nanshuo.springboot.model.request.user;

import com.nanshuo.springboot.annotation.CheckParam;
import com.nanshuo.springboot.model.enums.user.UserRegexEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录Request
 *
 * @author nanshuo
 * @date 2024/01/04 18:58:48
 */
@Data
@ApiModel(value = "UserLoginRequest", description = "用户登录信息Request")
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -5262836669010105900L;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号", required = true)
    @CheckParam(nullErrorMsg = "账号不能为空", minLength = 3, maxLength = 16, lenghtErrorMsg = "账号长度必须在3-16之间", regex = UserRegexEnums.ACCOUNT)
    private String userAccount;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "密码", required = true)
    @CheckParam(nullErrorMsg = "密码不能为空", minLength = 6, maxLength = 18, lenghtErrorMsg = "密码长度必须在6-18之间",regex = UserRegexEnums.PASSWORD)
    private String userPassword;

}
