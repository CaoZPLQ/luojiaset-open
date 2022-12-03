package cn.whu.geois.config;

import cn.whu.geois.common.util.DySmsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 设置静态参数初始化
 */
@Configuration
public class StaticConfig {

    @Value("${jeecg.sms.accessKeyId}")
    private String accessKeyId;

    @Value("${jeecg.sms.accessKeySecret}")
    private String accessKeySecret;

    @Bean
    public void initStatic() {
        DySmsHelper.setAccessKeyId(accessKeyId);
        DySmsHelper.setAccessKeySecret(accessKeySecret);
    }
}
