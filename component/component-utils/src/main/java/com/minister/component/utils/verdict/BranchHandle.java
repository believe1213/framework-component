package com.minister.component.utils.verdict;

import java.util.function.Consumer;

/**
 * 分支处理接口
 *
 * @author QIUCHANGQING620
 * @date 2021-12-06 09:44
 */
@FunctionalInterface
public interface BranchHandle<T> {

    /**
     * 分支操作
     *
     * @param handle 此时需要进行的操作
     * @return 分支处理loader
     */
    VerdictLoader<T> consumer(Consumer<T> handle);

}
