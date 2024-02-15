package com.minister.component.utils.verdict;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 分支处理loader
 *
 * @author QIUCHANGQING620
 * @date 2021-12-06 10:15
 */
public class VerdictLoader<T> {

    protected VerdictLoader(T obj) {
        this.obj = obj;
    }

    private final T obj;

    private boolean match = false;

    /**
     * 载入符合条件的分支处理逻辑
     *
     * @param predicate 断言
     * @return 分支处理
     */
    public BranchHandle<T> handle(Predicate<T> predicate) {
        return (handle) -> {
            if (predicate.test(this.obj)) {
                this.match = true;
                handle.accept(this.obj);
            }
            return this;
        };
    }

    /**
     * 默认处理逻辑，相当于 else
     *
     * @param handle 此时需要进行的操作
     */
    public void last(Consumer<T> handle) {
        if (!this.match) {
            handle.accept(this.obj);
        }
    }

}
