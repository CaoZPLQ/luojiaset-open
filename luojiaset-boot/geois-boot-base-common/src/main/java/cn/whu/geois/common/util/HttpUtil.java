package cn.whu.geois.common.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class HttpUtil {

    public static String doGet(String url, JSONObject paramsObj) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry entrySet : paramsObj.entrySet()) {
                uriBuilder.addParameter(entrySet.getKey().toString(), entrySet.getValue().toString());
            }
            HttpGet request = new HttpGet(uriBuilder.build());
            // 发送Get请求
            HttpResponse response = client.execute(request);
            // 请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() == 200) {
                // 读取服务器返回过来的数据
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String doPost(String url, JSONObject paramsObj) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(url);
            // 设置Header信息
            request.setHeader("Content-Type", "application/json");
            // 设置Body信息
            if (paramsObj != null) {
                StringEntity entity = new StringEntity(paramsObj.toJSONString(), "UTF-8");
                request.setEntity(entity);
            }
            // 发送Post请求
            HttpResponse response = client.execute(request);
            // 请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() == 200) {
                // 读取服务器返回过来的Json数据
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
