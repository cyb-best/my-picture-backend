package com.cyb.mypicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyb.mypicturebackend.auth.AuthCheck;
import com.cyb.mypicturebackend.common.BaseResponse;
import com.cyb.mypicturebackend.common.DeleteRequest;
import com.cyb.mypicturebackend.common.ResultUtils;
import com.cyb.mypicturebackend.constant.UserConstant;
import com.cyb.mypicturebackend.exception.BusinessException;
import com.cyb.mypicturebackend.exception.ErrorCode;
import com.cyb.mypicturebackend.exception.ThrowUtils;
import com.cyb.mypicturebackend.model.dto.picture.SpaceLevel;
import com.cyb.mypicturebackend.model.dto.space.SpaceAddRequest;
import com.cyb.mypicturebackend.model.dto.space.SpaceEditRequest;
import com.cyb.mypicturebackend.model.dto.space.SpaceQueryRequest;
import com.cyb.mypicturebackend.model.dto.space.SpaceUpdateRequest;
import com.cyb.mypicturebackend.model.entity.Space;
import com.cyb.mypicturebackend.model.entity.User;
import com.cyb.mypicturebackend.model.enums.SpaceLevelEnum;
import com.cyb.mypicturebackend.model.vo.SpaceVO;
import com.cyb.mypicturebackend.service.SpaceService;
import com.cyb.mypicturebackend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间接口
 *
 * @author cyb
 * @date 2025/01/29 10:54
 **/
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // region 增删改查

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param request
     * @return 空间id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest,
                                       HttpServletRequest request) {
        if (spaceAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String spaceName = spaceAddRequest.getSpaceName();
        Integer spaceLevel = spaceAddRequest.getSpaceLevel();
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 若用户未写空间名称，则默认给他一个
        if (StringUtils.isBlank(spaceName)) {
            spaceName = String.format("用户%s的空间", userId);
        }
        if (spaceLevel == null) {
            spaceLevel = SpaceLevelEnum.COMMON.getValue();
        }
        // 非管理员不允许传递非普通版空间等级
        if (SpaceLevelEnum.COMMON.getValue() != spaceLevel && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
        Space space = new Space();
        space.setSpaceName(spaceName);
        space.setSpaceLevel(spaceLevel);
        space.setUserId(userId);
        spaceService.fillSpaceParams(space);
        spaceService.validSpace(space, true);
        // 一个用户只能创建一个私有空间
        String lock = String.valueOf(userId).intern();
        Long spaceId;
        // 锁 + 事物
        synchronized (lock) {
            spaceId = transactionTemplate.execute(status -> {
                boolean exists = spaceService.lambdaQuery().eq(Space::getUserId, userId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户只能创建一个私有空间");
                boolean save = spaceService.save(space);
                ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "新增空间失败");
                return space.getId();
            });
        }
        return ResultUtils.success(spaceId);
    }


    /**
     * 删除空间 （仅管理员或创建人本身可用）
     */
    @PostMapping("/delete")
    public BaseResponse<Integer> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        long spaceId = deleteRequest.getId();
        Integer deleteResult = spaceService.deleteSpace(spaceId, loginUser);
        return ResultUtils.success(deleteResult);
    }

    /**
     * 更新空间（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,
                                             HttpServletRequest request) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 补充参数
        spaceService.fillSpaceParams(space);
        // 数据校验
        spaceService.validSpace(space, Boolean.FALSE);
        // 判断是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取空间（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(space);
    }

    /**
     * 根据 id 获取空间（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVO(space, request));
    }

    /**
     * 分页获取空间列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                         HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装类
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }


    /**
     * 编辑空间（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断是否存在
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 设置编辑时间
        space.setEditTime(new Date());
        User loginUser = userService.getLoginUser(request);
        // 仅本人或管理员可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }
}

