package com.nanshuo.partnermatching.model.request.team;

import com.nanshuo.partnermatching.annotation.CheckParam;
import com.nanshuo.partnermatching.constant.NumberConstant;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍添加请求
 *
 * @author nanshuo
 */
@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = -2027932642566110492L;

    /**
     * 队伍名称
     */
    @CheckParam(alias = "队伍名称",maxLength = 20)
    private String name;

    /**
     * 描述
     */
    @CheckParam(alias = "队伍简介",maxLength = 512)
    private String description;

    /**
     * 最大人数
     */
    @CheckParam(alias = "队伍人数")
    private Integer maxNum;

    /**
     * 过期时间
     */
    @CheckParam(required = NumberConstant.FALSE_VALUE,alias = "队伍过期时间")
    private Date expireTime;

    /**
     * 用户id
     */
    @CheckParam(alias = "用户id")
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    @CheckParam(required = NumberConstant.FALSE_VALUE,alias = "队伍状态")
    private Integer status;

    /**
     * 密码
     */
    @CheckParam(required = NumberConstant.FALSE_VALUE,alias = "队伍密码")
    private String password;
}
