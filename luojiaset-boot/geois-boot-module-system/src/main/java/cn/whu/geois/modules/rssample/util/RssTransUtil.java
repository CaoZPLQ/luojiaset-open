package cn.whu.geois.modules.rssample.util;

import cn.whu.geois.modules.rssample.entity.RssTransResult;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author tan haofeng
 * @Version 1.0
 * @Date 2021/4/28
 */
public class RssTransUtil {
    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    private String appid;
    private String securityKey;

    public RssTransUtil(String appid,String securityKey){
        this.appid = appid;
        this.securityKey = securityKey;
    }

    public String getTransResult(String query, String from , String to){
        Map<String, String> params = buildParas(query, from, to);
        String result = HttpGet.get(TRANS_API_HOST, params);
        String replace = result.replace("[", "").replace("]", "");

        RssTransResult rssTransResult = JSON.parseObject(replace, RssTransResult.class);

        String dst = rssTransResult.getTrans_result().getDst();

        return dst;

    }

    private Map<String,String> buildParas(String query, String from, String to){
        HashMap<String, String> params = new HashMap<>();
        params.put("q",query);
        params.put("from",from);
        params.put("to",to);

        params.put("appid",appid);

        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt",salt);

        String src = appid + query + salt + securityKey;
        params.put("sign",RssMD5.md5(src));

        return params;

    }
}
