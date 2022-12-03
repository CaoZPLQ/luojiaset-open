package cn.whu.geois.modules.rssample.service.impl;

import cn.whu.geois.modules.rssample.mapper.RssMysqlMapper;
import cn.whu.geois.modules.rssample.service.RssMysqlService;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author czp
 * @version 1.0
 * @date 2021/7/13 17:48
 */
@Service
@DS("master")
public class RssMysqlServiceImpl  implements RssMysqlService {
    @Resource
    private RssMysqlMapper rssMysqlMapper;

    @Override
    public String getUserRole(String userId){
        return rssMysqlMapper.getUserRole(userId);
    }
}
