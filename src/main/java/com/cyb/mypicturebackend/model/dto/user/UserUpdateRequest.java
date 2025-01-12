package com.cyb.mypicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 增加用户
 * @author cyb
 * @date 2025/01/12 15:11
 **/
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 714517701561964132L;
}

