package com.nanshuo.partnermatching.service.impl;

import com.nanshuo.partnermatching.model.vo.user.UserLoginVO;
import com.nanshuo.partnermatching.model.vo.user.UserSafetyVO;
import com.nanshuo.partnermatching.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    void searchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<UserLoginVO> userList = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}