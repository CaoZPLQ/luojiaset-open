package cn.whu.geois.modules.rssample.config;

/**
 * @author czp
 * @version 1.0
 * @date 2021/3/28 15:29
 */

import cn.whu.geois.modules.rssample.entity.RssMinioProp;
import io.minio.MinioClient;
//import io.minio.errors.InvalidEndpointException;
//import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * minio 核心配置类
 */
@Configuration
@EnableConfigurationProperties(RssMinioProp.class)
public class RssMinioConfig {

    @Autowired
    private RssMinioProp rssMinioProp;

    /**
     * 获取 MinioClient
     *
     * @return
     */
    @Bean
    public MinioClient minioClient() throws InvalidEndpointException, InvalidPortException {

        return new MinioClient(rssMinioProp.getEndpoint(), rssMinioProp.getAccesskey(), rssMinioProp.getSecretKey());
    }
}
