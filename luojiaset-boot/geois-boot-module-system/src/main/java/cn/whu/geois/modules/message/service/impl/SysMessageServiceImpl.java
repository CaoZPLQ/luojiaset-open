package cn.whu.geois.modules.message.service.impl;

import cn.whu.geois.common.system.base.service.impl.JeecgServiceImpl;
import cn.whu.geois.modules.message.entity.SysMessage;
import cn.whu.geois.modules.message.mapper.SysMessageMapper;
import cn.whu.geois.modules.message.service.ISysMessageService;
import org.springframework.stereotype.Service;

/**
 * @Description: 消息
 * @Author: jeecg-boot
 * @Date:  2019-04-09
 * @Version: V1.0
 */
@Service
public class SysMessageServiceImpl extends JeecgServiceImpl<SysMessageMapper, SysMessage> implements ISysMessageService {

}
