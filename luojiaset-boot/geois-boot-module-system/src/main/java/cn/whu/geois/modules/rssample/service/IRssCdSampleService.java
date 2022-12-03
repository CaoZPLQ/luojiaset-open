package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssCdSample;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 变化检测样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-08-12
 * @Version: V1.0
 */
public interface IRssCdSampleService extends IService<RssCdSample> {
    boolean insertBatch(List<RssCdSample> entityList);
    boolean insertBatch(List<RssCdSample> entityList, int batchSize);
}
