package cn.whu.geois.modules.rssample.service.impl;

import cn.whu.geois.modules.rssample.entity.RssCdSample;
import cn.whu.geois.modules.rssample.mapper.RssCdSampleMapper;
import cn.whu.geois.modules.rssample.service.IRssCdSampleService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 变化检测样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-08-12
 * @Version: V1.0
 */
@Service
@DS("postgres")
public class RssCdSampleServiceImpl extends ServiceImpl<RssCdSampleMapper, RssCdSample> implements IRssCdSampleService {
    @Override
    public boolean insertBatch(List<RssCdSample> entityList) {
        return insertBatch(entityList,1000);
    }

    @Override
    public boolean insertBatch(List<RssCdSample> entityList, int batchSize) {
        long startTime = System.currentTimeMillis();
        if(CollectionUtils.isEmpty(entityList)){
            throw new IllegalArgumentException("Error: entityList must not be empty");
        }
        try(SqlSession batchSqlSession = sqlSessionBatch()){
            int size = entityList.size();
            System.out.println("=========rss_sc_sample开始插入共"+size+"个数据===========");
            String sqlStatement = sqlStatement(SqlMethod.INSERT_ONE);
            for (int i = 0; i < size; i++) {
                batchSqlSession.insert(sqlStatement , entityList.get(i));
                if(i >= 1&& i%batchSize==0){
                    batchSqlSession.flushStatements();
                }
                long endTime = System.currentTimeMillis();
                if (i%1000==0){
                    System.out.println("插入"+i+"个耗时"+(endTime-startTime)+"ms");
                }
                if (i==size-1){
                    System.out.println("总共插入"+size+"个耗时"+(endTime-startTime)+"ms");
                }
            }
            batchSqlSession.flushStatements();
        }catch(Throwable e){
            throw new MybatisPlusException("Error: Cannot execute insertBatch Method. Cause");
        }
        return true;
    }


}
