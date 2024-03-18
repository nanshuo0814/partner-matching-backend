package com.nanshuo.springboot.model.request.user;

import com.nanshuo.springboot.annotation.CheckParam;
import com.nanshuo.springboot.model.enums.user.UserRegexEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册 Request
 *
 * @author nanshuo
 * @date 2023/12/23 19:00:34
 */
@Data
@ApiModel(value = "UserRegisterRequest", description = "用户注册信息Request")
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -3801105286374526414L;

    @ApiModelProperty(value = "账号", required = true)
    @CheckParam(nullErrorMsg = "账号不能为空", minLength = 3, maxLength = 11, lenghtErrorMsg = "账号长度必须在3-11之间", regex = UserRegexEnums.ACCOUNT, regexErrorMsg = "账号必须以字母开头且只能包含字母、数字或下划线")
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    @CheckParam(nullErrorMsg = "密码不能为空", minLength = 6, maxLength = 18, lenghtErrorMsg = "密码长度必须在6-18之间", regex = UserRegexEnums.PASSWORD, regexErrorMsg = "密码必须包含字母、数字或特殊字符")
    private String userPassword;

    @ApiModelProperty(value = "第二遍输入的密码", required = true)
    @CheckParam(nullErrorMsg = "第二遍输入的密码不能为空", minLength = 6, maxLength = 18, lenghtErrorMsg = "第二遍输入的密码长度必须在6-18之间", regex = UserRegexEnums.PASSWORD, regexErrorMsg = "第二遍输入的密码必须包含字母、数字或特殊字符")
    private String checkPassword;

    @ApiModelProperty(value = "星球编号", required = true)
    @CheckParam(nullErrorMsg = "星球编号", minLength = 1, maxLength = 5, lenghtErrorMsg = "星球编号1-5之间", regex = UserRegexEnums.NUMBER, regexErrorMsg = "星球编号只能是数字")
    private String planetCode;

}
