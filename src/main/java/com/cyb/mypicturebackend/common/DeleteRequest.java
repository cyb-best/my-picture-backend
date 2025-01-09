package com.cyb.mypicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求类
 *
 * @author cyb
 * @date 2025/01/07 14:41
 **/
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
