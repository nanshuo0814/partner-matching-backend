package com.nanshuo.partnermatching.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nanshuo.partnermatching.model.domain.Tag;
import com.nanshuo.partnermatching.service.TagService;
import com.nanshuo.partnermatching.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author dell
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-03-18 19:31:55
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




