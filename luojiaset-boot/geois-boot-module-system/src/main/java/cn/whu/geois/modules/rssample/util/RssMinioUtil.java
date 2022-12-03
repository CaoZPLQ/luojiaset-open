package cn.whu.geois.modules.rssample.util;


import cn.whu.geois.modules.rssample.entity.RssMinioProp;
import com.alibaba.fastjson.JSONObject;
import io.minio.*;
//import io.minio.ObjectStat;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/3/27 11:35
 */
@Slf4j
@Component
public class RssMinioUtil {
    @Autowired
    private MinioClient client;
    @Autowired
    private RssMinioProp rssMinioProp;
    /**
     * 获取某个buckect下所有对象
     * @param bucketName 桶名
     * @param prefix 对象的前缀名
     * @return
     */
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix, boolean b){
        //boolean true则使用递归，boolean false非递归
        return client.listObjects(bucketName,prefix,b);//minio 3.0.10版本
    }
    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        boolean flag = false;
        flag = client.bucketExists(bucketName);//minio 3.0.10版本
        if (flag) {
            return true;
        }
        return false;
    }

    /**
     * 获取对象的元数据
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @return
     */
    //minio 3.0.10版本
    @SneakyThrows
    public ObjectStat statObject(String bucketName, String objectName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            try {
                ObjectStat statObject = client.statObject(bucketName, objectName);
                return statObject;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     */
    @SneakyThrows
    public void createBucket(String bucketName) {
        if (!client.bucketExists(bucketName)) {//minio 3.0.10版本
            client.makeBucket(bucketName);//minio 3.0.10版本
        }
    }

    /**
     * 上传文件
     *
     * @param file       文件
     * @param bucketName 存储桶
     * @return
     */
    public JSONObject uploadFile(MultipartFile file, String bucketName) throws Exception {
        JSONObject res = new JSONObject();
        res.put("code", 0);
        // 判断上传文件是否为空
        if (null == file || 0 == file.getSize()) {
            res.put("msg", "上传文件不能为空");
            return res;
        }
        // 判断存储桶是否存在
        createBucket(bucketName);
        // 文件名
        String originalFilename = file.getOriginalFilename();
        // 新的文件名 = 存储桶名称_时间戳.后缀名
        String fileName = bucketName + "_" + System.currentTimeMillis() + originalFilename.substring(originalFilename.lastIndexOf("."));
        // 开始上传
        InputStream inputStream=file.getInputStream();
//        PutObjectOptions putObjectOptions=new PutObjectOptions(file.getSize(),PutObjectOptions.MIN_MULTIPART_SIZE);
//        putObjectOptions.setContentType(file.getContentType());
        client.putObject(bucketName, fileName, inputStream,file.getContentType());//minio 3.0.10版本
//        client.putObject(bucketName, fileName, inputStream,putObjectOptions);
//        client.putObject(args);
        res.put("code", 1);
        res.put("msg", rssMinioProp.getEndpoint() + "/" + bucketName + "/" + fileName);
        return res;
    }


    public JSONObject uploadFile(File file, String bucketName, String fileName) throws Exception{
        JSONObject res = new JSONObject();
        res.put("code", 0);
        if (null == file || 0 == file.length()) {
            res.put("msg", "上传文件不能为空");
            return res;
        }
        // 判断存储桶是否存在
        createBucket(bucketName);
        // 文件名
        String originalFilename = file.getName();

        // 开始上传
        FileInputStream input = new FileInputStream(file);
        client.putObject(bucketName,fileName,input,new MimetypesFileTypeMap().getContentType(file));//minio 3.0.10版本
//        PutObjectOptions putObjectOptions=new PutObjectOptions(file.length(),PutObjectOptions.MIN_MULTIPART_SIZE);
//        putObjectOptions.setContentType(new MimetypesFileTypeMap().getContentType(file));
//        client.putObject(bucketName,fileName,input,putObjectOptions);
        res.put("code", 1);
        res.put("msg", rssMinioProp.getEndpoint() + "/" + bucketName + "/" + fileName);
        return res;
    }
    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 二进制流
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName) {
        return client.getObject(bucketName, objectName);//minio 3.0.10版本
    }

}
