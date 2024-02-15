package com.minister.component.utils.exception;

import com.minister.component.utils.enums.HttpCodeEnum;

/**
 * 接口响应码
 *
 * @author QIUCHANGQING620
 * @date 2024-02-12 12:14
 */
public interface AppCode {

    String getCode();

    String getMsg();

    default boolean isSuccess() {
        return HttpCodeEnum.OK.getCode().equals(this.getCode());
    }

}
