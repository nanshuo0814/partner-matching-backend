package com.nanshuo.partnermatching.model.vo.user;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户登录vo
 *
 * @author nanshuo
 * @date 2024/01/04 19:08:31
 */
@Data
@ApiModel(value = "UserLoginVO", description = "用户登录VO")
public class UserLoginVO implements Serializable {

    /**
     * 用户 id
     */
    @ApiModelProperty(value = "用户 id", required = true)
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称", required = true)
    private String username;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像", required = true)
    private String avatarUrl;

    /**
     * 用户账号
     */
    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;

    /**
     * 用户性别
     */
    @ApiModelProperty(value = "用户性别", required = true)
    private Integer gender;

    /**
     * 用户邮箱
     */
    @ApiModelProperty(value = "用户邮箱", required = true)
    private String email;


    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色：user/admin/ban", required = true)
    private String userRole;

    private String planetCode;
    private String phone;

    /**
     * 标签 json 列表
     */
    private String tags;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间", required = true)
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}