package cn.whu.geois.modules.rssample.service.impl;

import cn.whu.geois.modules.rssample.entity.RssLcSampleClass;
import cn.whu.geois.modules.rssample.mapper.RssLcSampleClassMapper;
import cn.whu.geois.modules.rssample.service.IRssLcSampleClassService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 样本-地物要素类别关联表
 * @Author: jeecg-boot
 * @Date:   2021-01-29
 * @Version: V1.0
 */
@Service
@DS("postgres")
public class RssLcSampleClassServiceImpl extends ServiceImpl<RssLcSampleClassMapper, RssLcSampleClass> implements IRssLcSampleClassService {

}
