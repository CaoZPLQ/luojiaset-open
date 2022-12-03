package cn.whu.geois.modules.system.service;

import cn.whu.geois.modules.system.entity.SysCountryList;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 国家地区表
 * @Author: jeecg-boot
 * @Date:   2021-12-08
 * @Version: V1.0
 */
public interface ISysCountryListService extends IService<SysCountryList> {
    List<JSONObject> getCountryList(String locale);

}
