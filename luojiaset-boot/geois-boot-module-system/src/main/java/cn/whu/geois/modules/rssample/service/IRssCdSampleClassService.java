package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.RssCdSampleClass;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 样本_变化要素类别关联表
 * @Author: jeecg-boot
 * @Date:   2021-01-29
 * @Version: V1.0
 */
public interface IRssCdSampleClassService extends IService<RssCdSampleClass> {
    boolean insertBatch(List<RssCdSampleClass> entityList);
    boolean insertBatch(List<RssCdSampleClass> entityList, int batchSize);

}
