package com.nanshuo.springboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nanshuo.springboot.mapper.UserTeamMapper;
import com.nanshuo.springboot.model.domain.UserTeam;
import com.nanshuo.springboot.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author dell
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-03-18 19:31:55
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




