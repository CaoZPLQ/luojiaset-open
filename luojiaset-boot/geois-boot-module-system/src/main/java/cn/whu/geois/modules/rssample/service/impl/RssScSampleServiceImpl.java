package cn.whu.geois.modules.rssample.service.impl;

import cn.whu.geois.modules.rssample.entity.RssScSample;
import cn.whu.geois.modules.rssample.mapper.RssScSampleMapper;
import cn.whu.geois.modules.rssample.service.IRssScSampleService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 场景分类样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-08-15
 * @Version: V1.0
 */
@Service
@DS("postgres")
public class RssScSampleServiceImpl extends ServiceImpl<RssScSampleMapper, RssScSample> implements IRssScSampleService {
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertBatch(List<RssScSample> entityList){
        return insertBatch(entityList,1000);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean insertBatch(List<RssScSample> entityList,int batchSize){
        long startTime = System.currentTimeMillis();
        if(CollectionUtils.isEmpty(entityList)){
            throw new IllegalArgumentException("Error: entityList must not be empty");
        }
        try (SqlSession batchSqlSession = sqlSessionBatch()) {
            int size = entityList.size();
            System.out.println("=====sc_class开始插入共"+size+"个数据=====");
            String sqlStatement = sqlStatement(SqlMethod.INSERT_ONE);
            for (int i = 0; i < size; i++) {
                batchSqlSession.insert(sqlStatement, entityList.get(i));
                if (i >= 1 && i % batchSize == 0) {
                    batchSqlSession.flushStatements();
                }
                long endTime=System.currentTimeMillis();
                if (i%1000==0){
                    System.out.println("插入"+i+"个耗时"+(endTime-startTime));
                }
                if (i==size-1){
                    System.out.println("总共插入"+size+"个耗时"+(endTime-startTime));
                }
            }
            batchSqlSession.flushStatements();
        } catch (Throwable e) {
            throw new MybatisPlusException("Error: Cannot execute insertBatch Method. Cause", e);
        }
        return true;

    }
}
