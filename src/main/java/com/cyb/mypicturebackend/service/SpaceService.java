package com.cyb.mypicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyb.mypicturebackend.model.dto.space.SpaceQueryRequest;
import com.cyb.mypicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cyb.mypicturebackend.model.entity.User;
import com.cyb.mypicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 78570
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-02-05 10:15:13
 */
public interface SpaceService extends IService<Space> {


    /**
     * 校验空间(更新空间，添加空间公共一个校验)
     *
     * @param space
     * @param add
     */
    void validSpace(Space space, boolean add);

    /**
     * 在更新或添加空间的时候自动填充空间限额及大小
     *
     * @param space
     */
    void fillSpaceParams(Space space);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 删除空间（创建人或管理员可删除）
     *
     * @param spaceId
     * @param loginUser
     * @return
     */
    Integer deleteSpace(Long spaceId, User loginUser);
}
