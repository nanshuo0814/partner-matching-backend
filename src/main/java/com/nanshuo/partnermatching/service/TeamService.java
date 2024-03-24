package com.nanshuo.partnermatching.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.nanshuo.partnermatching.model.domain.Team;
import com.nanshuo.partnermatching.model.domain.User;
import com.nanshuo.partnermatching.model.request.team.TeamJoinRequest;
import com.nanshuo.partnermatching.model.request.team.TeamQueryRequest;
import com.nanshuo.partnermatching.model.request.team.TeamQuitRequest;
import com.nanshuo.partnermatching.model.request.team.TeamUpdateRequest;
import com.nanshuo.partnermatching.model.vo.team.TeamUserVO;

import java.util.List;

/**
* @author dell
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-03-18 19:31:55
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加团队
     *
     * @param team      团队
     * @param loginUser 登录用户
     * @return long
     */
    long addTeam(Team team, User loginUser);

    /**
     * 更新团队
     *
     * @param teamUpdateRequest 团队更新请求
     * @param loginUser         登录用户
     * @return boolean
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 删除团队
     *
     * @param id        id
     * @param loginUser 登录用户
     * @return boolean
     */
    boolean deleteTeam(long id, User loginUser);

    /**
     * 加入团队
     *
     * @param teamJoinRequest 团队加入请求
     * @param loginUser       登录用户
     * @return boolean
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出团队
     *
     * @param teamQuitRequest 团队退出请求
     * @param loginUser       登录用户
     * @return boolean
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 列出团队
     *
     * @param teamQuery 团队查询
     * @param b         b
     * @return {@code List<TeamUserVO>}
     */
    List<TeamUserVO> listTeams(TeamQueryRequest teamQuery, boolean b);
}
