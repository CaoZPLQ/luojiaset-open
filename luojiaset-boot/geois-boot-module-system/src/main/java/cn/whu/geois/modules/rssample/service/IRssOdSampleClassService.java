package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssOdSampleClass;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 样本-目标类别关联表
 * @Author: jeecg-boot
 * @Date:   2021-04-07
 * @Version: V1.0
 */
public interface IRssOdSampleClassService extends IService<RssOdSampleClass> {
    boolean insertBatch(List<RssOdSampleClass> entityList);
    boolean insertBatch(List<RssOdSampleClass> entityList, int batchSize);
}
