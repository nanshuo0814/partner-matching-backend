package com.nanshuo.partnermatching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nanshuo.partnermatching.model.domain.Team;
import com.nanshuo.partnermatching.service.TeamService;
import com.nanshuo.partnermatching.mapper.TeamMapper;
import org.springframework.stereotype.Service;

/**
* @author dell
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-03-18 19:31:55
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

}




