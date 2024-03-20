package com.nanshuo.partnermatching.model.request.user;

import com.nanshuo.partnermatching.annotation.CheckParam;
import com.nanshuo.partnermatching.constant.NumberConstant;
import com.nanshuo.partnermatching.model.enums.user.UserRegexEnums;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新信息Request
 *
 * @author nanshuo
 * @date 2024/01/23 16:51:11
 */
@Data
@ApiModel(value = "UserUpdateInfoRequest", description = "用户更新信息Request")
public class UserUpdateInfoRequest implements Serializable {

    private static final long serialVersionUID = 7658342535926195857L;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    @CheckParam(alias = "用户昵称", required = NumberConstant.FALSE_VALUE, regex = UserRegexEnums.USERNAME)
    private String userName;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    @CheckParam(alias = "用户头像", required = NumberConstant.FALSE_VALUE)
    private String avatarUrl;

    /**
     * 用户邮箱
     */
    @ApiModelProperty(value = "用户邮箱")
    @CheckParam(alias = "用户邮箱", required = NumberConstant.FALSE_VALUE, regex = UserRegexEnums.EMAIL)
    private String userEmail;

    /**
     * 用户性别
     */
    @ApiModelProperty(value = "用户性别")
    @CheckParam(alias = "用户性别", required = NumberConstant.FALSE_VALUE, regex = UserRegexEnums.USER_GENDER)
    private Integer userGender;

    /**
     * 用户密码
     */
    @ApiModelProperty(value = "密码", required = true)
    @CheckParam(alias = "密码", required = NumberConstant.FALSE_VALUE, minLength = 6, maxLength = 18, regex = UserRegexEnums.PASSWORD)
    private String userPassword;

}
