package com.minister.component.utils.verdict;

/**
 * 替换 if/Switch 方法工具类
 *
 * @author QIUCHANGQING620
 * @date 2021-12-06 09:47
 */
public class VerdictUtil {

    private VerdictUtil() {
    }

    /**
     * 初始化分支处理loader
     *
     * @param obj 对象
     * @return 分支处理loader
     */
    public static <T> VerdictLoader<T> load(T obj) {
        return new VerdictLoader<>(obj);
    }

}
