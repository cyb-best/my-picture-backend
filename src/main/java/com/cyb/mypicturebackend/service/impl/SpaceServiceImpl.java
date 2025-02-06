package com.cyb.mypicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyb.mypicturebackend.exception.BusinessException;
import com.cyb.mypicturebackend.exception.ErrorCode;
import com.cyb.mypicturebackend.exception.ThrowUtils;
import com.cyb.mypicturebackend.mapper.PictureMapper;
import com.cyb.mypicturebackend.model.dto.space.SpaceQueryRequest;
import com.cyb.mypicturebackend.model.entity.Picture;
import com.cyb.mypicturebackend.model.entity.Space;
import com.cyb.mypicturebackend.model.entity.User;
import com.cyb.mypicturebackend.model.enums.SpaceLevelEnum;
import com.cyb.mypicturebackend.model.vo.SpaceVO;
import com.cyb.mypicturebackend.model.vo.UserVO;
import com.cyb.mypicturebackend.service.PictureService;
import com.cyb.mypicturebackend.service.SpaceService;
import com.cyb.mypicturebackend.mapper.SpaceMapper;
import com.cyb.mypicturebackend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 78570
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-02-05 10:15:13
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private PictureMapper pictureMapper;

    @Override
    public void validSpace(Space space, boolean add) {
        if (space == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = space.getId();
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        // 如果是添加
        if (add) {
            if (StringUtils.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "空间级别不能为空");
            }
        }
        // 校验名称
        if (StringUtils.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR.getCode(), "空间名称过长");
        }
        if (SpaceLevelEnum.getEnumByValue(spaceLevel) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法的空间级别");
        }
    }

    @Override
    public void fillSpaceParams(Space space) {
        SpaceLevelEnum spaceLevel = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevel != null) {
            // 因为添加，更新都用这个方法，所以需要判断原来的是否为空，如果为空才自动赋值，否则用自己传的
            long maxSize = spaceLevel.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevel.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 分页获取空间封装
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public Integer deleteSpace(Long spaceId, User loginUser) {
        // 空间是否存在
        Space oldSpace = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 仅本人或管理员可删除
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 事务
        Integer deleteCount = transactionTemplate.execute(status -> {
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("spaceId", spaceId);
            List<Picture> pictureList = pictureMapper.selectList(queryWrapper);
            for (Picture picture : pictureList) {
                pictureMapper.deleteById(picture.getId());
            }
            boolean removeSpace = this.removeById(spaceId);
            ThrowUtils.throwIf(!removeSpace, ErrorCode.OPERATION_ERROR, "删除空间失败");
            return pictureList.size();
        });
        // 返回结果
        return deleteCount;
    }

}




