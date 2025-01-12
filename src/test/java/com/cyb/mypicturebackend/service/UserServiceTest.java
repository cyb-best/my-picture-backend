package com.cyb.mypicturebackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void test_UserRegister() {
        // 1. 参数校验
        //  a. 校验参数是否为空
        //  b. 校验账号是否包含特殊字符
        //  c. 校验账号长度是否小于4位
        //  d. 校验密码、二次密码是否小于8位
        //  e. 校验密码与二次密码是否相等
        // 2. 判断账号是否存在
        // 3. 密码加密
        // 4. 向数据库插入一条数据
        // 5. 返回
    }


}