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
 * @Description: 订单明细表
 * @Author: jeecg-boot
 * @Date:   2021-06-06
 * @Version: V1.0
 */
@Data
@TableName("rss_order_item")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_order_item对象", description="订单明细表")
public class RssOrderItem {
    
	/**id*/
	@TableId(type = IdType.UUID)
    @ApiModelProperty(value = "id")
	private java.lang.Integer id;
	/**orderNum*/
	@Excel(name = "orderNum", width = 15)
    @ApiModelProperty(value = "orderNum")
	private java.lang.String orderNum;
	/**sampleId*/
	@Excel(name = "sampleId", width = 15)
    @ApiModelProperty(value = "sampleId")
	private java.lang.Integer sampleId;
	/**taskType*/
	@Excel(name = "taskType", width = 15)
    @ApiModelProperty(value = "taskType")
	private java.lang.String taskType;
	/**createBy*/
	@Excel(name = "createBy", width = 15)
    @ApiModelProperty(value = "createBy")
	private java.lang.String createBy;
	/**createTime*/
    @ApiModelProperty(value = "createTime")
	private java.util.Date createTime;
	/**updateBy*/
	@Excel(name = "updateBy", width = 15)
    @ApiModelProperty(value = "updateBy")
	private java.lang.String updateBy;
	/**updateTime*/
    @ApiModelProperty(value = "updateTime")
	private java.util.Date updateTime;
	/**datasetName*/
	@Excel(name = "datasetName", width = 15)
    @ApiModelProperty(value = "datasetName")
	private java.lang.String datasetName;
}
