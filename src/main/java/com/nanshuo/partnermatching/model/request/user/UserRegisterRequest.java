package com.nanshuo.partnermatching.model.request.user;

import com.nanshuo.partnermatching.annotation.CheckParam;
import com.nanshuo.partnermatching.model.enums.user.UserRegexEnums;
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
    @CheckParam(alias = "账号", minLength = 3, maxLength = 11, regex = UserRegexEnums.ACCOUNT)
    private String userAccount;

    @ApiModelProperty(value = "密码", required = true)
    @CheckParam(alias = "密码", minLength = 6, maxLength = 18, regex = UserRegexEnums.PASSWORD)
    private String userPassword;

    @ApiModelProperty(value = "第二遍输入的密码", required = true)
    @CheckParam(alias = "第二遍输入的密码", minLength = 6, maxLength = 18, regex = UserRegexEnums.PASSWORD)
    private String checkPassword;

    @ApiModelProperty(value = "星球编号", required = true)
    @CheckParam(alias = "星球编号", minLength = 1, maxLength = 5, regex = UserRegexEnums.NUMBER)
    private String planetCode;

}
