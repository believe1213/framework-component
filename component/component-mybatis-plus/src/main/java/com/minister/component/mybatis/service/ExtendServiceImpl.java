package com.minister.component.mybatis.service;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.binding.MapperMethod;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.function.Function;

/**
 * ServiceImpl扩展
 *
 * @author QIUCHANGQING620
 * @date 2022-07-12 16:27
 */
public class ExtendServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 批量条件更新
     * TODO 优化条件入参
     *
     * @param entityList      数据
     * @param wrapperFunction 更新条件
     * @return 更新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatchByWrapper(Collection<T> entityList, Function<T, LambdaQueryChainWrapper<T>> wrapperFunction) {
        return updateBatchByWrapper(entityList, wrapperFunction, DEFAULT_BATCH_SIZE);
    }

    /**
     * 批量条件更新
     * TODO 优化条件入参
     *
     * @param entityList      数据
     * @param wrapperFunction 更新条件
     * @param batchSize       每批次更新数量
     * @return 更新状态
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatchByWrapper(Collection<T> entityList, Function<T, LambdaQueryChainWrapper<T>> wrapperFunction, int batchSize) {
        String sqlStatement = this.getSqlStatement(SqlMethod.UPDATE);
        return this.executeBatch(entityList, batchSize, (sqlSession, entity) -> {
            MapperMethod.ParamMap<Object> param = new MapperMethod.ParamMap<>();
            param.put(Constants.ENTITY, entity);
            param.put(Constants.WRAPPER, wrapperFunction.apply(entity).getWrapper());
            sqlSession.update(sqlStatement, param);
        });
    }

}
