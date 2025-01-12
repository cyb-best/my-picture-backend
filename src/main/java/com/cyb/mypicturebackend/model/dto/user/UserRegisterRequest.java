package com.cyb.mypicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册DTO
 * @author cyb
 * @date 2025/01/12 15:11
 **/
@Data
public class UserRegisterRequest implements Serializable {

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;

    private static final long serialVersionUID = 714517701561964132L;
}

