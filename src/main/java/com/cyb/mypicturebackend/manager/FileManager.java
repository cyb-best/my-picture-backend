package com.cyb.mypicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.cyb.mypicturebackend.common.ResultUtils;
import com.cyb.mypicturebackend.config.CosClientConfig;
import com.cyb.mypicturebackend.exception.BusinessException;
import com.cyb.mypicturebackend.exception.ErrorCode;
import com.cyb.mypicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author cyb
 * @date 2025/01/18 15:20
 **/
@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀（eg: /public/userId）
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validatePicture(multipartFile);
        // 自定义文件路径 uuid_时间戳.后缀
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        // 为什么不将用户自定义的文件名添加到文件名中，主要是因为，这个文件名会被拼接到url中，被访问
        // 如果用户加了特殊符号，可能会存在解析错误的情况，导致用户无法访问该图片，还有就是出于安全考量了
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        // 创建临时文件multipartFile传入到本地，便于后续的操作
        File file = null;
        try {
            // 创建一个临时文件用于接受传来的资源
            file = File.createTempFile(uploadFileName, null);
            multipartFile.transferTo(file);
            // 图片上传到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 获取图片基础信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            String format = imageInfo.getFormat();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            // 将图片信息返回
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(file));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(format);
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("upload file failed, failPath: {}", uploadFileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 关闭文件
            deleteFile(file);
        }
    }


    private void validatePicture(MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件为空");
        }
        // 校验图片大小
        final long ONE_MB = 1024 * 1024;
        if (multipartFile.getSize() > 2 * ONE_MB) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
        }
        // 校验图片后缀
        final List<String> ALLOW_FILE_SUFFIX = Arrays.asList("jpeg", "jpg", "png", "webp");
        if (!ALLOW_FILE_SUFFIX.contains(FileUtil.getSuffix(multipartFile.getOriginalFilename()))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件类型");
        }
    }

    public void deleteFile(File file) {
        if (file == null) {
            return;
        }
        // 关闭文件
        boolean result = file.delete();
        if (!result) {
            log.error("file delete failed, filePath: {}", file.getAbsoluteFile());
        }
    }

}

