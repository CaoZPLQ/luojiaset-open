package cn.whu.geois.modules.system.service.impl;

import cn.whu.geois.modules.system.entity.SysCountryList;
import cn.whu.geois.modules.system.mapper.SysCountryListMapper;
import cn.whu.geois.modules.system.service.ISysCountryListService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 国家地区表
 * @Author: jeecg-boot
 * @Date:   2021-12-08
 * @Version: V1.0
 */
@Service
@DS("postgres")
public class SysCountryListServiceImpl extends ServiceImpl<SysCountryListMapper, SysCountryList> implements ISysCountryListService {
    @Resource
    SysCountryListMapper sysCountryListMapper;


    @Override
    public List<JSONObject> getCountryList(String locale){
        List<SysCountryList> sysCountryListList = sysCountryListMapper.getCountryList();
        List<JSONObject> jsonObjectList = new ArrayList<>();
        for(int i = 0;i < sysCountryListList.size();i++){
            if(locale.equals("en")){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",sysCountryListList.get(i).getCountryCode());
                jsonObject.put("value", sysCountryListList.get(i).getCountryEn());
                jsonObjectList.add(jsonObject);
            }
            if(locale.equals("zh")){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("code",sysCountryListList.get(i).getCountryCode());
                jsonObject.put("value", sysCountryListList.get(i).getCountryZh());
                jsonObjectList.add(jsonObject);
            }

        }
        return jsonObjectList;
    }

}
