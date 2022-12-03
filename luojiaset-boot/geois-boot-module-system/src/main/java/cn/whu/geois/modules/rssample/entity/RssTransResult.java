package cn.whu.geois.modules.rssample.entity;

import lombok.Data;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/4/28
 */
@Data
public class RssTransResult {
    private String from;
    private String to;
    private trans_result trans_result;

    public class trans_result{
        private String src;
        private String dst;

        public trans_result(String src, String dst){
            this.src = src;
            this.dst = dst;
        }
        public String getDst(){
            return this.dst;
        }
    }
}
