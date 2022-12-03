package cn.whu.geois.modules.rssample.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author czp
 * @version 1.0
 * @date 2021/3/27 11:37
 */
/**
 * minio 属性值
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class RssMinioProp {
    /**
     * 连接url
     */
    private String endpoint;
    /**
     * 用户名
     */
    private String accesskey;
    /**
     * 密码
     */
    private String secretKey;
}
