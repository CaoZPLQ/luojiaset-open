package cn.whu.geois.modules.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import cn.whu.geois.modules.system.entity.SysCountryList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Description: 国家地区表
 * @Author: jeecg-boot
 * @Date:   2021-12-08
 * @Version: V1.0
 */
public interface SysCountryListMapper extends BaseMapper<SysCountryList> {
    @Select("select * from sys_country_list")
    List<SysCountryList> getCountryList();

}
