package com.cyb.mypicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录DTO
 * @author cyb
 * @date 2025/01/12 15:11
 **/
@Data
public class UserLoginRequest implements Serializable {

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    private static final long serialVersionUID = 714517701561964132L;
}

