package com.cyb.mypicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyb.mypicturebackend.model.dto.user.UserQueryRequest;
import com.cyb.mypicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyb.mypicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 78570
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-01-12 14:24:43
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册函数
     * <p>
     * 该函数用于处理用户注册请求，接收用户账号、用户密码和确认密码作为参数
     * 它的主要职责是验证用户信息的合法性（如密码的正确性和账号的唯一性），
     * 并在验证通过后将用户信息持久化到存储系统中
     *
     * @param userAccount   用户账号，应保证唯一性，用于登录系统
     * @param userPassword  用户密码，用于验证用户身份
     * @param checkPassword 密码确认，确保用户输入的密码一致
     * @return 返回一个Long类型的值，通常代表新注册用户的唯一标识符（如数据库中的自增ID）
     * 如果注册失败（例如，账号已存在或信息不合法），则可能返回null或特定的错误代码
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录函数
     * <p>
     * 该函数用于处理用户登录请求，接收用户账号和用户密码作为参数
     * 它的主要职责是验证用户身份，并返回一个包含用户信息的对象，
     * 以便后续的会话管理或业务逻辑处理
     *
     * @param userAccount  用户账号，用于登录系统
     * @param userPassword 用户密码，用于验证用户身份
     * @param request      HttpServletRequest对象，用于获取会话信息
     * @return 返回一个UserVO对象，该对象包含了用户登录成功后的相关信息，
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户退出登录函数
     * <p>
     * 该函数用于处理用户退出登录请求，接收HttpServletRequest对象作为参数
     * 它的主要职责是清除用户会话信息，并返回一个布尔值，表示退出登录操作是否成功
     *
     * @param request HttpServletRequest对象，用于获取会话信息
     * @return 返回一个布尔值，表示退出登录操作是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取加密后的密码
     * @param userPassword 用户密码
     * @return 加盐的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏的用户信息
     * @param originUser 当前登录用户信息
     * @return 脱敏的用户信息
     */
    UserVO getSafeUser(User originUser);

    /**
     * 用于业务内部共享用户信息，不能直接返回给前端！！
     * @param request
     * @return 未脱敏的用户信息
     */
    User getLoginUser(HttpServletRequest request);

    UserVO getUserVO(User user);

    /**
     * 获取用户列表
     * @param userList 用户列表
     * @return 脱敏的用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);
    /**
     * 根据请求参数构造查询条件
     * @param userQueryRequest 请求参数
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);
}
