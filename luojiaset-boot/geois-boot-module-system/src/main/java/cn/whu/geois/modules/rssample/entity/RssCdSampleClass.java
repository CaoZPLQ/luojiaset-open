package cn.whu.geois.modules.rssample.entity;

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
 * @Description: 样本_变化要素类别关联表
 * @Author: jeecg-boot
 * @Date:   2021-01-29
 * @Version: V1.0
 */
@Data
@TableName("rss_cd_sample_class")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_cd_sample_class对象", description="样本_变化要素类别关联表")
public class RssCdSampleClass {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private Integer id;
	/**sampleId*/
	@Excel(name = "sampleId", width = 15)
    @ApiModelProperty(value = "sampleId")
	private Integer sampleId;
	/**uniClassId*/
	@Excel(name = "uniClassId", width = 15)
    @ApiModelProperty(value = "uniClassId")
	private String uniClassId;
	/**selfClassId*/
	@Excel(name = "selfClassId", width = 15)
    @ApiModelProperty(value = "selfClassId")
	private String selfClassId;
	/**selfClassName*/
	@Excel(name = "selfClassName", width = 15)
    @ApiModelProperty(value = "selfClassName")
	private String selfClassName;
	/**createBy*/
	@Excel(name = "createBy", width = 15)
    @ApiModelProperty(value = "createBy")
	private String createBy;
	/**createTime*/
    @ApiModelProperty(value = "createTime")
	private Date createTime;
	/**updateBy*/
	@Excel(name = "updateBy", width = 15)
    @ApiModelProperty(value = "updateBy")
	private String updateBy;
	/**updateTime*/
    @ApiModelProperty(value = "updateTime")
	private Date updateTime;
}
