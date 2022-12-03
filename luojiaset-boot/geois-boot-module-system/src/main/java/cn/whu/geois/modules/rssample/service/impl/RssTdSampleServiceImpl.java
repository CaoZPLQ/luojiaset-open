package cn.whu.geois.modules.rssample.service.impl;

import cn.whu.geois.modules.rssample.entity.RssTdSample;
import cn.whu.geois.modules.rssample.mapper.RssTdSampleMapper;
import cn.whu.geois.modules.rssample.service.IRssTdSampleService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 三维多视样本元数据表
 * @Author: jeecg-boot
 * @Date:   2021-03-14
 * @Version: V1.0
 */
@Service
@DS("postgres")
public class RssTdSampleServiceImpl extends ServiceImpl<RssTdSampleMapper, RssTdSample> implements IRssTdSampleService {

}
