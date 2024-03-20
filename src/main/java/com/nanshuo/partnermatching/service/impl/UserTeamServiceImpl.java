package com.nanshuo.partnermatching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nanshuo.partnermatching.mapper.UserTeamMapper;
import com.nanshuo.partnermatching.model.domain.UserTeam;
import com.nanshuo.partnermatching.service.UserTeamService;
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




