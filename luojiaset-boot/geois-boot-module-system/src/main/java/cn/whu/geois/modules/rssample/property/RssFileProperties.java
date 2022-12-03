package cn.whu.geois.modules.rssample.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author czp
 * @version 1.0
 * @date 2021/1/28 21:01
 */
@ConfigurationProperties(prefix = "file")
@Component
public class RssFileProperties {
    private String sampleDir;

    public String getSampleDir() {
        return sampleDir;
    }
    public void setSampleDir(String sampleDir) {
        this.sampleDir = sampleDir;
    }
}
