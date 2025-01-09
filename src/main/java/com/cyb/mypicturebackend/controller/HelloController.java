package com.cyb.mypicturebackend.controller;

import com.cyb.mypicturebackend.common.BaseResponse;
import com.cyb.mypicturebackend.common.ResultUtils;
import com.cyb.mypicturebackend.exception.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cyb
 * @date 2025/01/07 14:23
 **/
@RestController
@RequestMapping("/")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello, world";
    }

    @GetMapping("/error")
    public BaseResponse getError() {
        int a = 1 / 0;
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"错误");
    }

    @GetMapping("/success")
    public BaseResponse<String> success() {
        String data =  "成功！！！";
        return ResultUtils.success(data);
    }
}

