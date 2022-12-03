package cn.whu.geois.modules.rssample.service.impl;

import cn.whu.geois.modules.rssample.entity.RssOdSample;
import cn.whu.geois.modules.rssample.mapper.RssOdSampleMapper;
import cn.whu.geois.modules.rssample.service.IRssOdSampleService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 目标检测样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-07-17
 * @Version: V1.0
 */
@Service
@DS("postgres")
public class RssOdSampleServiceImpl extends ServiceImpl<RssOdSampleMapper, RssOdSample> implements IRssOdSampleService {

}
