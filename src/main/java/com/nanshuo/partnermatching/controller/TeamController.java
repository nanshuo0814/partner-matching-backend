package com.nanshuo.partnermatching.controller;

import com.nanshuo.partnermatching.annotation.Check;
import com.nanshuo.partnermatching.common.BaseResponse;
import com.nanshuo.partnermatching.common.ErrorCode;
import com.nanshuo.partnermatching.common.ResultUtils;
import com.nanshuo.partnermatching.constant.UserConstant;
import com.nanshuo.partnermatching.exception.BusinessException;
import com.nanshuo.partnermatching.model.domain.Team;
import com.nanshuo.partnermatching.model.domain.User;
import com.nanshuo.partnermatching.model.request.team.DeleteRequest;
import com.nanshuo.partnermatching.model.request.team.TeamAddRequest;
import com.nanshuo.partnermatching.model.request.team.TeamUpdateRequest;
import com.nanshuo.partnermatching.service.TeamService;
import com.nanshuo.partnermatching.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 队伍接口
 *
 * @author nanshuo
 */
@Slf4j
@Api(tags = "队伍模块")
@RestController
@RequestMapping("/team")
public class TeamController {

    private final UserService userService;

    private final TeamService teamService;

    public TeamController(UserService userService, TeamService teamService) {
        this.userService = userService;
        this.teamService = teamService;
    }

    /**
     * 添加队伍
     *
     * @param teamAddRequest 团队添加请求
     * @param request        请求
     * @return {@code BaseResponse<Long>}
     */
    @PostMapping("/add")
    @Check(checkParam = true, checkAuth = UserConstant.USER_ROLE)
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }


    /**
     * 更新团队
     *
     * @param teamUpdateRequest 团队更新请求
     * @param request           请求
     * @return {@code BaseResponse<Boolean>}
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 按id获取团队
     *
     * @param id id
     * @return {@code BaseResponse<Team>}
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL);
        }
        return ResultUtils.success(team);
    }

    /**
     * 删除团队
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return {@code BaseResponse<Boolean>}
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

}
