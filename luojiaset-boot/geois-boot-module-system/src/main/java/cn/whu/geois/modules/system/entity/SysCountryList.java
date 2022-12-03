package cn.whu.geois.modules.system.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 国家地区表
 * @Author: jeecg-boot
 * @Date:   2021-12-08
 * @Version: V1.0
 */
@Data
@TableName("sys_country_list")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="sys_country_list对象", description="国家地区表")
public class SysCountryList {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private Integer id;
	/**countryCode*/
	@Excel(name = "countryCode", width = 15)
    @ApiModelProperty(value = "countryCode")
	private String countryCode;
	/**countryZh*/
	@Excel(name = "countryZh", width = 15)
    @ApiModelProperty(value = "countryZh")
	private String countryZh;
	/**countryEn*/
	@Excel(name = "countryEn", width = 15)
    @ApiModelProperty(value = "countryEn")
	private String countryEn;
}
