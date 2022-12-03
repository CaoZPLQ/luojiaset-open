package cn.whu.geois.modules.rssample.service;

import org.springframework.stereotype.Service;

/**
 * @author czp
 * @version 1.0
 * @date 2021/7/13 17:47
 */
@Service
public interface RssMysqlService {
    String getUserRole(String userId);
}
