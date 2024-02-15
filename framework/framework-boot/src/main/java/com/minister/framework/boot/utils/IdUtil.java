package com.minister.framework.boot.utils;

import com.minister.framework.boot.dto.BaseId;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;

/**
 * 实例化标识
 *
 * @author QIUCHANGQING620
 * @date 2019/6/28 10:21
 */
@Slf4j
public class IdUtil {

    public static <T> T get(Class<T> clazz, Object... objs) {
        try {
            // 判断是否继承BaseId.class
            if (!BaseId.class.isAssignableFrom(clazz)) {
                log.error(clazz.getName() + " is not extends BaseId!");
                return null;
            }
            // 判断传入参数是否为空
            if (objs == null || objs.length == 0) {
                return null;
            }
            Class<?>[] objClass = new Class[objs.length];
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] == null) {
                    return null;
                }
                objClass[i] = objs[i].getClass();
            }
            // 获取目标类构造器，objClass为即将实例化类clazz的构造参数的类型
            Constructor<T> constructor = clazz.getConstructor(objClass);
            // 传入参数进行实例化
            return constructor.newInstance(objs);
        } catch (Exception e) {
            log.error("can not get " + clazz.getName());
            return null;
        }
    }

}
