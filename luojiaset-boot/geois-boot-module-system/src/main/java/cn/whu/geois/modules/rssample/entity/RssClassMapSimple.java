package cn.whu.geois.modules.rssample.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czp
 * @version 1.0
 * @date 2021/5/10 11:26
 */
@Data
@TableName("rss_class_map")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="rss_class_map对象", description="数据集类别关系映射表")
public class RssClassMapSimple {
    private Integer id;
    private String uniClassName;
    private String selfClassName;
    private String taskType;
}
