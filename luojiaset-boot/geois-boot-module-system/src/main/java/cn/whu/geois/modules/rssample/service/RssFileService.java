package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.analysis.RssParam;
import cn.whu.geois.modules.rssample.analysis.RssQuery;
import cn.whu.geois.modules.rssample.entity.*;

import cn.whu.geois.modules.rssample.exception.RssFileException;
import cn.whu.geois.modules.rssample.property.RssFileProperties;
import cn.whu.geois.modules.rssample.util.*;
import cn.whu.geois.modules.rssample.xml.*;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Iterables;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author czp
 * @version 1.0
 * @date 2021/1/28 17:20
 */
@Service
public class RssFileService {
    @Resource
    private RssFileUtil rssFileUtil;
    @Resource
    private RssPostgresqlService rssPostgresqlService;
    @Resource
    private IRssScClassService iRssScClassService;
    @Resource
    private IRssOdClassService iRssOdClassService;
    @Resource
    private IRssScSampleService iRssScSampleService;
    @Resource
    private IRssCdSampleService iRssCdSampleService;

    @Resource
    private IRssClassMapService iRssClassMapService;

    @Resource
    private IRssOdSampleClassService iRssOdSampleClassService;
    @Resource
    private IRssCdSampleClassService iRssCdSampleClassService;
    @Resource
    private IRssGqjcClassService iRssGqjcClassService;
    @Resource
    private RssMinioUtil rssMinioUtil;
    @Resource
    private RssQuery rssQuery;
    @Resource
    private RssGsUtil rssGsUtil;
    @Resource
    private RssImageUtil rssImageUtil;


    private final Path fileStorageLocation; // 文件在本地存储的地址
    @Autowired
    public RssFileService(RssFileProperties fileProperties) {
        this.fileStorageLocation = Paths.get(fileProperties.getSampleDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RssFileException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }


    public Boolean compress(String[] filePaths){

        List<File> files=new LinkedList<>();
        for (String filePath:filePaths){
            File file=new File(filePath);
            files.add(file);
        }
        try {
            rssFileUtil.packet(files,"output/test.tar.gz");

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    public Boolean compressMinioSimple(String bucketName,String[] filePaths,String outPath){
//        HashMap<String,List<String>> bucketObjectsHasMap=new HashMap<>();
//        bucketObjectsHasMap.put(bucketName,Arrays.asList(filePaths));
//        String targetBucketName="test";
//        String targetObjectName=outPath;
//        try {
//            rssMinioUtil.composeObjects(bucketObjectsHasMap,targetBucketName,targetObjectName);
//            return true;
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//    }
    public String compressSystem(String orderNum)throws IOException{
        long startTime = System.currentTimeMillis();

        //0. 判断是否已存在对应zip文件
        //0.1 minio上判断是否存在
        try {
            ObjectStat objectStat= rssMinioUtil.statObject("test",orderNum+".tar.gz");
            if (objectStat!=null){
                System.out.println("系统已存在"+orderNum+".tar.gz");
                //获得url
                String downloadUrl=rssQuery.queryOrderInfoDownloadUrl(orderNum);
                return downloadUrl;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        //0.2 本地判断时候存在
//        String outPath="output/"+orderNum+".tar.gz";
        String outPath=fileStorageLocation+File.separator+orderNum+".tar.gz";
        File tarGzFile=new File(outPath);
        if (tarGzFile.exists()){
            //如果存在直接跳转第四步
            //4. 上传文件
            String tarGzFileName=tarGzFile.getName();
            try {
                rssMinioUtil.uploadFile(tarGzFile,"test",tarGzFileName);
                long endTime = System.currentTimeMillis();
                System.out.println("文件["+outPath+"]上传执行完毕, 耗时:" + (endTime - startTime) + "ms");
            }catch (Exception e){
                e.printStackTrace();
            }
            //5. 根据压缩文件名和buckName生成加密下载url
            String downloadUrl=null;
            try {
                String inputStr = orderNum+".tar.gz&test";
                System.err.println("原文: " + inputStr);
                byte[] input = inputStr.getBytes();

                String pwd = "efg";
                System.err.println("密码: " + pwd);

                byte[] saltNew="xxx".getBytes();
                System.out.println("saltNew.length:"+saltNew.length);

                byte[] data = PBECoder.encrypt(input, pwd, saltNew);
                downloadUrl=PBECoder.encryptBASE64(data).replace("/","_").replaceAll("\r|\n", "");
                System.out.println("downloadUrl:"+downloadUrl);

            }catch (Exception e){
                e.printStackTrace();

            }
            //6. 更新订单状态，下载url
            if (downloadUrl!=null){
                Integer tradeStatus=1;
                boolean update=rssPostgresqlService.updateOrderInfo(tradeStatus,downloadUrl,orderNum);
                if (update){
                    System.out.println("orderNum:"+orderNum+"信息更新成功！！！");
                }else {
                    System.out.println("orderNum:"+orderNum+"信息更新失败！！！");
                    return null;
                }
            }else {
                System.out.println("数据url有误！！！");
                return null;
            }
            return downloadUrl;
        }
        //1. 先获取minio文件获取写入本地
        List<File> downloadFiles=new LinkedList<>();
        String bucketName=null;
        //根据订单编号查询任务类型
        String taskType=rssQuery.queryOrderInfoTaskType(orderNum);
        System.out.println("taskType:"+taskType);
        switch (taskType){
            case "od":
                bucketName="object-detection";
                break;
            case "sc":
                bucketName="scene-classification";
                break;
            case "lc":
                bucketName="land-cover";
                break;
            case "cd":
                bucketName="change-detection";
            default:
                break;
        }
        if (bucketName==null){
            return null;
        }
        //1.1 根据orderNum查询出对应的文件ids和buckName
        List<RssPair> pairs=rssQuery.queryOrderItemPath(orderNum,taskType);
//        System.out.println("filePaths.get(0)"+pairs.get(0).getImagePath());
        //1.2 将文件写入本地
        for (RssPair pair:pairs){
            if (taskType.equals("cd")){
                String preImagePath=pair.getPreImagePath();
                String postImagePath=pair.getPostImagePath();
                String labelPath=pair.getLabelPath();
                switch (taskType){
                    case "cd":
                        preImagePath=preImagePath.replace("change-detection/","");
                        postImagePath=postImagePath.replace("change-detection/","");
                        labelPath=labelPath.replace("change-detection/","");
                    default:
                        break;

                }

                InputStream preImageInputStream=rssMinioUtil.getObject(bucketName,preImagePath);
                InputStream postImageInputStream=rssMinioUtil.getObject(bucketName,postImagePath);
                InputStream labelInputStream=null;
                if (labelPath!=null){
                    labelInputStream=rssMinioUtil.getObject(bucketName,labelPath);

                }
//            String name=filePath.substring(filePath.lastIndexOf("/")+1);
//            System.out.println(name);
//            File imageFile = new File("output/"+imagePath);
                File preImageFile = new File(fileStorageLocation+File.separator+preImagePath);
                File postmageFile = new File(fileStorageLocation+File.separator+postImagePath);
//            File labelFile = new File("output/"+labelPath);
                File labelFile = new File(fileStorageLocation+File.separator+labelPath);
                downloadFiles.add(preImageFile);
                downloadFiles.add(postmageFile);

                FileUtils.copyToFile(preImageInputStream, preImageFile);
                FileUtils.copyToFile(postImageInputStream, postmageFile);
                if (labelInputStream!=null){
                    FileUtils.copyToFile(labelInputStream, labelFile);
                }
                if (labelFile.exists()){
                    System.out.println("labelFile:"+labelFile+" 存在！！！");
                    downloadFiles.add(labelFile);
                }else {
                    System.out.println("labelFile:"+labelFile+" 不存在！！！");
                }
            }else {
                String imagePath=pair.getImagePath();
                String labelPath=pair.getLabelPath();
                switch (taskType){
                    case "lc":
                        imagePath=imagePath.replace("land-cover/","");
                        System.out.println("imagePath:"+imagePath);
                        labelPath=labelPath.replace("land-cover/","").replace("image","label").replace(".tif",".png");
                        System.out.println("labelPath:"+labelPath);
                        break;
                    case "sc":
                        imagePath=imagePath.replace("scene-classification/","");
                        labelPath=null;
                        break;
                    case "od":
                        labelPath=labelPath.replace("label_hbb","label_unit_hbb");
                        break;
                    default:
                        break;

                }

                InputStream imageInputStream=rssMinioUtil.getObject(bucketName,imagePath);
                InputStream labelInputStream=null;
                if (labelPath!=null){
                    labelInputStream=rssMinioUtil.getObject(bucketName,labelPath);

                }
//            String name=filePath.substring(filePath.lastIndexOf("/")+1);
//            System.out.println(name);
//            File imageFile = new File("output/"+imagePath);
                File imageFile = new File(fileStorageLocation+File.separator+imagePath);
//            File labelFile = new File("output/"+labelPath);
                File labelFile = new File(fileStorageLocation+File.separator+labelPath);
                downloadFiles.add(imageFile);

                FileUtils.copyToFile(imageInputStream, imageFile);
                if (labelInputStream!=null){
                    FileUtils.copyToFile(labelInputStream, labelFile);
                }
                if (labelFile.exists()){
                    System.out.println("labelFile:"+labelFile+" 存在！！！");
                    downloadFiles.add(labelFile);
                }else {
                    System.out.println("labelFile:"+labelFile+" 不存在！！！");
                }
            }

        }
        //2. 迭代源文件集合, 将文件打包为Tar
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath+".tmp");
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);
             TarOutputStream tarOutputStream = new TarOutputStream(bufferedOutput);) {
            for (File resourceFile : downloadFiles) {
                if(!resourceFile.isFile()){
                    continue;
                }
                try(FileInputStream fileInputStream = new FileInputStream(resourceFile);
                    BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream)){
//                    TarEntry entry = new TarEntry(new File(resourceFile.getName()));
                    System.out.println("resourceFile.getPath()"+resourceFile.getPath());
//                    TarEntry entry = new TarEntry(new File(resourceFile.getPath().replace("output","")));

                    System.out.println("fileStorageLocation.toString():"+fileStorageLocation.toString());
                    TarEntry entry = new TarEntry(new File(resourceFile.getPath().replace(fileStorageLocation.toString(),"")));
                    entry.setSize(resourceFile.length());
                    tarOutputStream.putNextEntry(entry);
                    IOUtils.copy(bufferedInput, tarOutputStream);
                } catch (Exception e) {
                    throw new ServiceException("文件["+resourceFile+"]压缩执行异常, 嵌套异常: \n" + e.toString());
                }finally {
                    tarOutputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            Files.delete(Paths.get(outPath+".tmp"));
            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
        }
        //3. 读取打包好的Tar临时文件文件, 使用GZIP方式压缩
        try (FileInputStream fileInputStream = new FileInputStream(outPath+".tmp");
             BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(outPath);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(gzipOutputStream);
        ) {
            byte[] cache = new byte[1024];
            for (int index = bufferedInput.read(cache); index != -1; index = bufferedInput.read(cache)) {
                bufferedOutput.write(cache,0,index);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("文件["+outPath+"]压缩执行完毕, 耗时:" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
        }finally {
            Files.delete(Paths.get(outPath+".tmp"));
            for (RssPair pair:pairs){
                String imagePath=pair.getImagePath();
                String labelPath=pair.getLabelPath();
                System.out.println("labelPath:"+labelPath);
                switch (taskType){
                    case "lc":
                        imagePath=imagePath.replace("land-cover/","");
                        System.out.println("imagePath:"+imagePath);
                        labelPath=labelPath.replace("land-cover/","").replace("image","label").replace(".tif",".png");
                        System.out.println("labelPath:"+labelPath);
                        break;
                    case "sc":
                        imagePath=imagePath.replace("scene-classification/","");
                        System.out.println("imagePath:"+imagePath);
                        break;
                    case "od":
                        labelPath=labelPath.replace("label_hbb","label_unit_hbb");
                        break;
                    default:
                        break;
                }
//                File imageFile = new File("output/"+imagePath);
                File imageFile = new File(fileStorageLocation+File.separator+imagePath);
//                File labelFile = new File("output/"+labelPath);
                File labelFile = new File(fileStorageLocation+File.separator+labelPath);
                if (imageFile.exists()){
//                    Files.delete(Paths.get("output/"+imagePath));
                    Files.delete(Paths.get(fileStorageLocation+File.separator+imagePath));
                }else {
//                    System.out.println("不存在数据文件"+"output/"+imagePath);
                    System.out.println("不存在数据文件"+fileStorageLocation+File.separator+imagePath);
                }
                if (labelFile.exists()){
//                    Files.delete(Paths.get("output/"+labelPath));
                    Files.delete(Paths.get(fileStorageLocation+File.separator+labelPath));
                }else {
//                    System.out.println("不存在标签文件"+"output/"+labelPath);
                    System.out.println("不存在标签文件"+fileStorageLocation+File.separator+labelPath);
                }

            }
        }
        //4. 上传文件
        String tarGzFileName=tarGzFile.getName();
        try {
            rssMinioUtil.uploadFile(tarGzFile,"test",tarGzFileName);
            long endTime = System.currentTimeMillis();
            System.out.println("文件["+outPath+"]上传执行完毕, 耗时:" + (endTime - startTime) + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
        //5. 根据压缩文件名和buckName生成加密下载url
        String downloadUrl=null;
        try {
            String inputStr = orderNum+".tar.gz&test";
            System.err.println("原文: " + inputStr);
            byte[] input = inputStr.getBytes();

            String pwd = "efg";
            System.err.println("密码: " + pwd);

            byte[] saltNew="xxx".getBytes();
            System.out.println("saltNew.length:"+saltNew.length);

            byte[] data = PBECoder.encrypt(input, pwd, saltNew);
            downloadUrl=PBECoder.encryptBASE64(data).replace("/","_").replaceAll("\r|\n", "");
        }catch (Exception e){
            e.printStackTrace();
        }
        //6. 更新订单状态，下载url
        if (downloadUrl!=null){
            Integer tradeStatus=1;
            boolean update=rssPostgresqlService.updateOrderInfo(tradeStatus,downloadUrl,orderNum);
            if (update){
                System.out.println("orderNum:"+orderNum+"信息更新成功！！！");
            }else {
                System.out.println("orderNum:"+orderNum+"信息更新失败！！！");
                return null;
            }
        }else {
            System.out.println("数据url有误！！！");
            return null;
        }
        return downloadUrl;
    }


    public Boolean uploadSampleThumb(String filePath,String dataSetName){
        try {
            long startTime = System.currentTimeMillis();
            File renderPngDir=new File(filePath);
            if (!renderPngDir.isDirectory()){
                System.out.println(renderPngDir+"不是文件夹");
                return false;
            }
            String[] pngNames=renderPngDir.list();
            String newRenderPngPath=null;
            String newImagePath=null;
            String suffix=null;
            switch (dataSetName){
                case "DIUx_xView":
                    newRenderPngPath = "DIUx_xView-v1/render_png/";
                    newImagePath="DIUx_xView-v1/image/";
                    suffix="tif";
                    break;
                default:
                    break;
            }
            if (newRenderPngPath!=null&&newImagePath!=null){
                for (int i=0;i<pngNames.length;i++){
                    String minioImagePath=newImagePath+pngNames[i].replace("png",suffix);
                    File oldRenderPngFile=new File(filePath+File.separator+ pngNames[i]);
                    String minioRenderPngPath = newRenderPngPath + pngNames[i];
                    String[] minioRenderPngPaths={minioRenderPngPath,"0000"};
                    System.out.println("minioRenderPngPaths.length"+minioRenderPngPaths.length);
                    rssMinioUtil.uploadFile(oldRenderPngFile,"object-detection",minioRenderPngPath);
                    if (i%50==0){
                        long endTime=System.currentTimeMillis();
                        System.out.print("上传"+((float)i/pngNames.length)*100+"%完成");
                        System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                    }
                    //更新数据库
//                    boolean update=rssPostgresqlService.updateSampleThumb(minioRenderPngPaths,minioImagePath);
                }
                long endTime=System.currentTimeMillis();
                System.out.print("上传"+((float)pngNames.length/pngNames.length)*100+"%完成");
                System.out.println("上传"+pngNames.length+"耗时："+(endTime-startTime)/1000+"秒");
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean toPng(String filePath,String outputPath){
        try {
            long startTime = System.currentTimeMillis();
            File xviewImageFile=new File(filePath);
            String[] imagesName=xviewImageFile.list();
            for (int i=0;i<imagesName.length;i++){
                if (i % 50 == 0) {
                    long endTime = System.currentTimeMillis();
                    System.out.print("转换" + ((float) i / imagesName.length) * 100 + "%完成");
                    System.out.println("转换" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                }
                String imagePath=filePath+File.separator+imagesName[i];
                rssImageUtil.tifToPng(imagePath,outputPath);

            }
            long endTime = System.currentTimeMillis();
            System.out.println("最终转换" + imagesName.length + "耗时：" + (endTime - startTime) / 1000 + "秒");
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Boolean compressMinio(String bucketName,String[] filePaths,String outPath) throws IOException{
        //1. 先获取minio文件获取写入本地
        List<File> downloadFiles=new LinkedList<>();
        long startTime = System.currentTimeMillis();
        for (String filePath:filePaths){
            InputStream inputStream=rssMinioUtil.getObject(bucketName,filePath);
            String name=filePath.substring(filePath.lastIndexOf("/")+1);
            System.out.println(name);
            File file = new File("output/"+name);
            downloadFiles.add(file);
            FileUtils.copyToFile(inputStream, file);
        }
//        // 2. 迭代源文件集合, 将文件打包为Tar
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath+".tmp");
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);
             TarOutputStream tarOutputStream = new TarOutputStream(bufferedOutput);) {
            for (File resourceFile : downloadFiles) {
                if(!resourceFile.isFile()){
                    continue;
                }
                try(FileInputStream fileInputStream = new FileInputStream(resourceFile);
                    BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);){
                    TarEntry entry = new TarEntry(new File(resourceFile.getName()));
                    entry.setSize(resourceFile.length());
                    tarOutputStream.putNextEntry(entry);
                    IOUtils.copy(bufferedInput, tarOutputStream);
                } catch (Exception e) {
                    throw new ServiceException("文件["+resourceFile+"]压缩执行异常, 嵌套异常: \n" + e.toString());
                }finally {
                    tarOutputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            Files.delete(Paths.get(outPath+".tmp"));
            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
        }
        //3. 读取打包好的Tar临时文件文件, 使用GZIP方式压缩
        try (FileInputStream fileInputStream = new FileInputStream(outPath+".tmp");
             BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(outPath);
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(gzipOutputStream);
        ) {
            byte[] cache = new byte[1024];
            for (int index = bufferedInput.read(cache); index != -1; index = bufferedInput.read(cache)) {
                bufferedOutput.write(cache,0,index);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("文件["+outPath+"]压缩执行完毕, 耗时:" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
        }finally {
            Files.delete(Paths.get(outPath+".tmp"));
            for (String filePath:filePaths){
                String name=filePath.substring(filePath.lastIndexOf("/")+1);
                Files.delete(Paths.get("output/"+name));
            }
        }
        File tarGzFile=new File(outPath);
        String tarGzFileName=tarGzFile.getName();
        try {
            rssMinioUtil.uploadFile(tarGzFile,"test",tarGzFileName);
            long endTime = System.currentTimeMillis();
            System.out.println("文件["+outPath+"]上传执行完毕, 耗时:" + (endTime - startTime) + "ms");
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    /**
     *
     * @param filePath
     * @return
     */
    public boolean insertGQJCClass(String filePath){
        List<RssGqjcClass> rssGqjcClasses = new ArrayList<>();
        try{
            rssGqjcClasses = RssExcelUtil.readLcExcel(filePath, 2);
        }catch(Exception e){
            e.printStackTrace();
        }
        boolean insertBatch = iRssGqjcClassService.insertBatch(rssGqjcClasses);

        return insertBatch;
    }


    public boolean updateDatasetImage(String field,String imagePath,String datasetName,String version){
        String base64Str=rssFileUtil.convertFileToBase64(imagePath);
//        File out=rssFileUtil.convertBase64ToFile(base64Str,"output","test.jpg");
        boolean update=rssPostgresqlService.updateDatasetImage(field,base64Str,datasetName,version);
        return update;
    }
    /**
     * insert ScSamples into rss_sc_sample database
     * @param path
     * @param classMapPath
     * @return
     * @throws Exception
     */
    public boolean insertSCSample(String path,String classMapPath,int sheetNum) throws Exception {
        //获取类别映射map
        Map<String, String> classMap = RssExcelUtil.getClassMap(classMapPath, sheetNum);


        LinkedList<RssScSample> rssScSampleList = new LinkedList<>();

        //新建场景分类bucket
        String bucketName = "scene-classification";
        rssMinioUtil.createBucket(bucketName);

        long allStartTime = System.currentTimeMillis();

        //path以/Users/tanhaofeng/Desktop/3RSD46-WHU/val 为例
        File datasetDir = new File(path);

        //获取数据集id
        //getParentFile返回file，getparent返回string
        //grandParent = /Users/tanhaofeng/Desktop
        String grandParent = datasetDir.getParentFile().getParent();
        //parent = /Users/tanhaofeng/Desktop/3RSD46-WHU
        String parent = datasetDir.getParent();
        //去掉前缀 消除数字序号
        //datasetName = RSD46-WHU
        String datasetName = parent.replace(grandParent, "").replace("/","").substring(1);
        int datasetID = rssPostgresqlService.getDatasetID(datasetName);

        //获取当前sc_sample最大id
        Integer id= rssPostgresqlService.getMaxScSampleID();
        if(id==null){
            id = 0;
        }

        //其余数据集不用考虑train/val/test属性
        switch (datasetName){
            case "RSD46-WHU":
                //获取用途 val test train
                String aim = path.replace(parent, "").replace("/", "");
                Integer trainValueTest = null;
                switch (aim){
                    case "train":
                        trainValueTest = 0;
                        break;
                    case "val":
                        trainValueTest = 1;
                        break;
                }
                if(datasetDir.isDirectory()){
                    String[] classes = datasetDir.list();

                    for (int i = 0; i < classes.length; i++) {
                        String classMapValue = classMap.get(classes[i]);
                        //获取对应类的编码
                        String code = rssPostgresqlService.getScClassCode(classMapValue);

                        File imageDir = new File(path + File.separator + classes[i]);
                        if(imageDir.isDirectory()){
                            String[] images = imageDir.list();
                            String prefix = classMapValue.replace(" ","_");

                            //判断bucket是否存在
                            //注意转换大小写
                            //若以scene-classification作为buckName 取代以datasetName作为buckName
//                            Optional<Bucket> bucket = rssMinioUtil.getBucket(datasetName.toLowerCase());
//                            boolean present = bucket.isPresent();
//                            System.out.println(present);
                            //已有影像数量
                            int size = 0;
////                            if(present){
//                                Iterable<Result<Item>> results = rssMinioUtil.listObjects(datasetName.toLowerCase(), classMapValue,true);
//                                size = Iterables.size(results);
//                                System.out.println(size);
//                            }
                            //判断是否已经入库的rsd46 train/val
                            Iterable<Result<Item>> results = rssMinioUtil.listObjects(bucketName, "RSD46-WHU", false);
                            if (Iterables.size(results)==1){
                                Iterable<Result<Item>> results1 = rssMinioUtil.listObjects(bucketName, datasetName+File.separator+classMapValue, true);
                                size = Iterables.size(results1);
                            }




                            for (int j = 0; j < images.length; j++) {
//                                String trans = new String(images[j].getBytes(),"GB2312");
//                                if(trans.contains("副本")){
//                                    System.out.println(trans);
//
//                                }
                                id++;
                                RssScSample rssScSample = new RssScSample();
                                String suffix = RssExcelUtil.getSuffix(images[j]);


                                //影像新名 Artificial_dense_forest_land.jpg
                                int count = size+j+1;
                                String imageNewName = prefix+"_version1_"+count+"."+suffix;
                                System.out.println(imageNewName);
                                String imagePath = bucketName+File.separator+datasetName + File.separator + classMapValue + File.separator + imageNewName;
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String oldPath = path + File.separator + classes[i] + File.separator + images[j];

                                //获取annotation_time
//                                Timestamp annotationTime = rssFileUtil.getFileCreateTime(oldPath);

                                //本地测试方法
                                //拷贝到自定义目录
//                        File oldImageFile = new File(oldPath);
//                        File newImageFile = new File(fileStorageLocation + File.separator + imagePath);
//                        //判断dataset、class文件夹是否存在
//                        if(!newImageFile.getParentFile().getParentFile().exists()){
//                            newImageFile.getParentFile().getParentFile().mkdirs();
//                        }
//                        if(!newImageFile.getParentFile().exists()){
//                            newImageFile.getParentFile().mkdirs();
//                        }
//                        Files.copy(oldImageFile.toPath(),newImageFile.toPath());

                                //minio测试上传
                                File oldImageFile = new File(oldPath);
                                //转换file类型
//                                FileInputStream fis = new FileInputStream(oldImageFile);
//                                MultipartFile multipartFile = new MockMultipartFile(oldImageFile.getName(), oldImageFile.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fis);
//                                String objectName = datasetName+File.separator+classMapValue + File.separator + imageNewName;
//                                rssMinioUtil.uploadFile(multipartFile,bucketName,objectName);
                                String objectName = datasetName+File.separator+classMapValue + File.separator + imageNewName;
                                rssMinioUtil.uploadFile(oldImageFile,datasetName,objectName);

                                //第二种上传方式
//                                rssMinioUtil.upload(datasetName.toLowerCase(),objectName,fis,"image/png");



                                int height = 0;
                                int width = 0;
                                switch (suffix){
                                    case "jpg":
                                    case "png":
                                    case "TIF":
                                        BufferedImage bi = ImageIO.read(oldImageFile);
                                        height = bi.getHeight();
                                        width = bi.getWidth();
                                        break;
                                    default:
                                        break;
                                }

                                //插入数据库
                                rssScSample.setId(id);
                                //目前无单张影像多个场景 sampleid与id保持一致
                                rssScSample.setSampleId(String.valueOf(id));
                                rssScSample.setDatasetId(datasetID);
                                rssScSample.setClassId(code);
                                rssScSample.setSampleWidth(width);
                                rssScSample.setSampleHeight(height);
//                                rssScSample.setAnnotationDate(annotationTime);
                                rssScSample.setImagePath(imagePath);
                                rssScSample.setImageType("optical");
                                rssScSample.setImageChannels(3);
                                rssScSample.setInstrument("Google Earth、天地图");
                                rssScSample.setTrnValueTest(trainValueTest);
                                rssScSample.setCreateBy("admin");
                                rssScSample.setCreateTime(createTime);

                                rssScSampleList.add(rssScSample);
//

                            }

                        }
                    }

                }
                break;
            case "AID":
            case "RSSCN7":
            case "WHU-RS19":
                if(datasetDir.isDirectory()){
                    String[] classes = datasetDir.list();

//                    String[] tmpClasses = datasetDir.list();
////                    System.out.println(tmpClasses);
//                    //center和stadium存在问题 先剔除
//                    List<String> tmpList = ListUtil.transferArrayToList(tmpClasses);
//                    if(tmpList.contains("Center")){
//                        tmpList.remove("Center");
//                    }
//                    if(tmpList.contains("Stadium")){
//                        tmpList.remove("Stadium");
//                    }
//                    String[] classes = new String[tmpList.size()];
//                    tmpList.toArray(classes);

                    for (int i = 0; i < classes.length; i++) {
                        String classMapValue = classMap.get(classes[i]);
//                        System.out.println(classMapValue);
                        //获取对应类的编码
                        String code = rssPostgresqlService.getScClassCode(classMapValue);

                        File imageDir = new File(path + File.separator + classes[i]);
                        if(imageDir.isDirectory()){
                            String[] images = imageDir.list();
                            for (int j = 0; j < images.length; j++) {
                                id++;
                                RssScSample rssScSample = new RssScSample();
                                String suffix = RssExcelUtil.getSuffix(images[i]);
                                String imageNewName = classMapValue.replace(" ","_")+"_version1_"+(j+1)+"."+suffix;
                                System.out.println(imageNewName);
                                String imagePath = bucketName+File.separator+datasetName + File.separator + classMapValue + File.separator + imageNewName;
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String oldPath = path + File.separator + classes[i] + File.separator + images[j];

                                //获取annotation_time 不准确
//                                Timestamp annotationTime = rssFileUtil.getFileCreateTime(oldPath);

                                //minio测试上传
                                File oldImageFile = new File(oldPath);
                                //转换file类型
//                                FileInputStream fis = new FileInputStream(oldImageFile);
//                                MultipartFile multipartFile = new MockMultipartFile(oldImageFile.getName(), oldImageFile.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fis);
//                                String objectName = datasetName+File.separator+classMapValue + File.separator + imageNewName;
//                                rssMinioUtil.uploadFile(multipartFile,bucketName,objectName);

                                String objectName = datasetName+File.separator+classMapValue + File.separator + imageNewName;
                                rssMinioUtil.uploadFile(oldImageFile,datasetName,objectName);

                                int height = 0;
                                int width = 0;
                                switch (suffix){
                                    case "jpg":
                                    case "png":
                                    case "TIF":
                                        BufferedImage bi = ImageIO.read(oldImageFile);
                                        height = bi.getHeight();
                                        width = bi.getWidth();
                                        break;
                                    default:
                                        break;
                                }

                                //插入数据库
                                rssScSample.setId(id);
                                //目前无单张影像多个场景 sampleid与id保持一致
                                rssScSample.setSampleId(String.valueOf(id));
                                rssScSample.setDatasetId(datasetID);
                                rssScSample.setClassId(code);
                                rssScSample.setSampleWidth(width);
                                rssScSample.setSampleHeight(height);
//                                rssScSample.setAnnotationDate(annotationTime);
                                rssScSample.setImagePath(imagePath);
                                rssScSample.setImageType("optical");
                                rssScSample.setImageChannels(3);
                                rssScSample.setInstrument("Google Earth");
                                rssScSample.setCreateBy("admin");
                                rssScSample.setCreateTime(createTime);

                                rssScSampleList.add(rssScSample);


                            }

                        }
                    }

                }
                break;
            case "PatternNet":
                if(datasetDir.isDirectory()){
                    String[] classes = datasetDir.list();

                    for (int i = 0; i < classes.length; i++) {
                        String classMapValue = classMap.get(classes[i]);
                        //获取对应类的编码
                        String code = rssPostgresqlService.getScClassCode(classMapValue);

                        File imageDir = new File(path + File.separator + classes[i]);
                        if(imageDir.isDirectory()){
                            String[] images = imageDir.list();
                            for (int j = 0; j < images.length; j++) {
                                id++;
                                RssScSample rssScSample = new RssScSample();
                                String suffix = RssExcelUtil.getSuffix(images[i]);
                                String imageNewName = classMapValue.replace(" ","_")+"_version1_"+(j+1)+"."+suffix;
                                System.out.println(imageNewName);
                                String imagePath = bucketName+File.separator+datasetName + File.separator + classMapValue + File.separator + imageNewName;
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String oldPath = path + File.separator + classes[i] + File.separator + images[j];

                                //获取annotation_time
//                                Timestamp annotationTime = rssFileUtil.getFileCreateTime(oldPath);

                                //minio测试上传
                                File oldImageFile = new File(oldPath);
                                //转换file类型
//                                FileInputStream fis = new FileInputStream(oldImageFile);
//                                MultipartFile multipartFile = new MockMultipartFile(oldImageFile.getName(), oldImageFile.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fis);
//                                String objectName = datasetName+File.separator+classMapValue + File.separator + imageNewName;
//                                //buckname不能出现大写
//                                rssMinioUtil.uploadFile(multipartFile,bucketName,objectName);
                                String objectName = datasetName+File.separator+classMapValue + File.separator + imageNewName;
                                rssMinioUtil.uploadFile(oldImageFile,datasetName,objectName);

//                                rssMinioUtil.uploadFile(multipartFile,datasetName.toLowerCase(),objectName);


                                int height = 0;
                                int width = 0;
                                switch (suffix){
                                    case "jpg":
                                    case "png":
                                    case "TIF":
                                        BufferedImage bi = ImageIO.read(oldImageFile);
                                        height = bi.getHeight();
                                        width = bi.getWidth();
                                        break;
                                    default:
                                        break;
                                }

                                //插入数据库
                                rssScSample.setId(id);
                                //目前无单张影像多个场景 sampleid与id保持一致
                                rssScSample.setSampleId(String.valueOf(id));
                                rssScSample.setDatasetId(datasetID);
                                rssScSample.setClassId(code);
                                rssScSample.setSampleWidth(width);
                                rssScSample.setSampleHeight(height);
//                                rssScSample.setAnnotationDate(annotationTime);
                                rssScSample.setImagePath(imagePath);
                                rssScSample.setImageType("optical");
                                rssScSample.setImageChannels(3);
                                rssScSample.setInstrument("Google影像");
                                rssScSample.setCreateBy("admin");
                                rssScSample.setCreateTime(createTime);

                                rssScSampleList.add(rssScSample);


                            }

                        }
                    }

                }
                break;
            default:
                break;
        }



        boolean insertBatch = iRssScSampleService.insertBatch(rssScSampleList);

        long allEndTime = System.currentTimeMillis();
        long time = allEndTime-allStartTime;
        long min = time / (1000 * 60);
        long sec = time%(1000*60)/ 1000;
        System.out.println("============================");
        System.out.println("入库总耗时："+min+" min "+sec+" s");
        return insertBatch;

    }



    public boolean checkODClass(String path,String dataSetName){
        List<String> classNames=new LinkedList<>();
        try {
            switch (dataSetName){
                case "DOTA-1.5":
                    File dotadsDir=new File(path);
                    if (dotadsDir.isDirectory()){
                        String[] trnValueTests=dotadsDir.list();
                        for (int i=0;i<trnValueTests.length;i++){
                            String trnValueTestStr=trnValueTests[i];
                            File annDir=new File(path+File.separator+trnValueTestStr+File.separator+"hbbxml");
                            if (annDir.isDirectory()){
                                String s[] = annDir.list();
                                for (int j = 0; j < s.length; j++) {
                                    File labelFile = new File(path+File.separator+trnValueTestStr+File.separator+"hbbxml"+ File.separator + s[j]);
                                    System.out.println("labelFile.getName():"+labelFile.getName());
                                    RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                    //获取目标类别，判断是否重复
                                    List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
                                    if (objectList!=null){
                                        System.out.println("objectList.size():"+objectList.size());
                                        for (RssOdAnn.Object object:objectList){
                                            String objectName=object.getName();
                                            if (!classNames.contains(objectName)){
                                                classNames.add(objectName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        System.out.println("classNameNum；"+classNames.size());
        for (String className:classNames){
            System.out.println("className；"+className);
        }
        return true;
    }

    public boolean toUnitOdClass(String path,String dataSetName,String classMapPath){
        long startTime=System.currentTimeMillis();
        HashMap<String,String> selfIdNameMap=new HashMap<>();
        //获取类别映射map
        Map<String, String> classMap = new HashMap<>();
        Map<String, String> selfIdclassMap = new HashMap<>();
        try {
            switch (dataSetName) {
                case "AIR-SARShip":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 13);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    File airsarshipdsDir=new File(path);
                    if (airsarshipdsDir.isDirectory()){
                        File annDir=new File(path+File.separator+"hbbxml");
                        if (annDir.isDirectory()){
                            String s[]= annDir.list();
                            for (int i=0;i<s.length;i++){
                                File labelFile=new File(path+File.separator+"hbbxml"+File.separator+s[i]);
                                File newLabelFile=new File(path+File.separator+"unit_hbbxml"+File.separator+s[i]);

                                RssOdAnn oldRssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                RssOdAnn newRssOdAnn=rssFileUtil.replaceClassName(oldRssOdAnn,classMap);
                                rssFileUtil.beanToXml(newRssOdAnn,RssOdAnn.class,newLabelFile.getPath());
                                if (i%50==0){
                                    long endTime=System.currentTimeMillis();
                                    System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                    System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                }
                            }
                        }
                    }
                    break;
                case "DOTA-1.5":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 0);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    File dotadsDir=new File(path);
                    if (dotadsDir.isDirectory()){
                        String[] trnValueTests=dotadsDir.list();

                        for (int i=0;i<trnValueTests.length;i++) {
                            String trnValueTestStr = trnValueTests[i];
                            if (dotadsDir.isDirectory()) {
                                File annDir = new File(path + File.separator + trnValueTestStr + File.separator + "hbbxml");
                                if (annDir.isDirectory()) {
                                    String s[] = annDir.list();
                                    for (int j = 0; j < s.length; j++) {
                                        File labelFile = new File(path+File.separator+trnValueTestStr+File.separator+"hbbxml"+ File.separator + s[j]);
                                        File newLabelFile=new File(path+File.separator+trnValueTestStr+File.separator+"unit_hbbxml"+File.separator+s[j]);

                                        RssOdAnn oldRssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                        RssOdAnn newRssOdAnn=rssFileUtil.replaceClassName(oldRssOdAnn,classMap);
                                        rssFileUtil.beanToXml(newRssOdAnn,RssOdAnn.class,newLabelFile.getPath());
                                        if (j%50==0){
                                            long endTime=System.currentTimeMillis();
                                            System.out.print("上传"+((float)j/s.length)*100+"%完成");
                                            System.out.println("上传"+j+"耗时："+(endTime-startTime)/1000+"秒");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "DOTA-2.0":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 2);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    File dotadsTwoDir=new File(path);
                    if (dotadsTwoDir.isDirectory()){
                        String[] trnValueTests=dotadsTwoDir.list();
                        for (int i=0;i<trnValueTests.length;i++) {
                            String trnValueTestStr = trnValueTests[i];
                            if (dotadsTwoDir.isDirectory()) {
                                File annDir = new File(path + File.separator + trnValueTestStr + File.separator + "hbbxml");
                                if (annDir.isDirectory()) {
                                    String s[] = annDir.list();
                                    for (int j = 0; j < s.length; j++) {
                                        File labelFile = new File(path + File.separator + trnValueTestStr + File.separator + "hbbxml" + File.separator + s[j]);
                                        File newLabelFile = new File(path + File.separator + trnValueTestStr + File.separator + "unit_hbbxml" + File.separator + s[j]);

                                        RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(labelFile);
                                        RssOdAnn newRssOdAnn = rssFileUtil.replaceClassName(oldRssOdAnn, classMap);
                                        rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                        if (j % 50 == 0) {
                                            long endTime = System.currentTimeMillis();
                                            System.out.print("上传" + ((float) j / s.length) * 100 + "%完成");
                                            System.out.println("上传" + j + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "RSOD":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 1);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }

                    File rsoddsDir=new File(path);
                    if (rsoddsDir.isDirectory()){
                        String[] classNames=rsoddsDir.list();
                        for (String className:classNames){
                            File annDir=new File(path+File.separator+className+File.separator+"Annotation"+File.separator+"xml");
                            if (annDir.isDirectory()){
                                String s[] = annDir.list();
                                for (int i = 0; i < s.length; i++) {
                                    File labelFile = new File(path+File.separator+className+File.separator+"Annotation"+File.separator+"xml" + File.separator + s[i]);
                                    File newLabelFile = new File(path+File.separator+className+File.separator+"Annotation"+File.separator+ "unit_hbbxml" + File.separator + s[i]);

                                    RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(labelFile);
                                    RssOdAnn newRssOdAnn = rssFileUtil.replaceClassName(oldRssOdAnn, classMap);
                                    rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                    if (i % 50 == 0) {
                                        long endTime = System.currentTimeMillis();
                                        System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                        System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "bridges_dataset":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 4);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    File brdsDir=new File(path);
                    if (brdsDir.isDirectory()){
                        File annDir=new File(path+File.separator+"Annotations");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path+File.separator+"Annotations"+ File.separator + s[i]);
                                File newLabelFile = new File(path+File.separator+ "unit_hbbxml" + File.separator + s[i]);

                                RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdAnn newRssOdAnn = rssFileUtil.replaceClassName(oldRssOdAnn, classMap);
                                rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                            }
                        }
                    }
                    break;
                case "SSDD":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 3);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    File ssdddsDir=new File(path);
                    if (ssdddsDir.isDirectory()){
                        File annDir=new File(path+File.separator+"newAnnotations");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path+File.separator+"newAnnotations"+ File.separator + s[i]);
                                File newLabelFile = new File(path+File.separator+ "unit_hbbxml" + File.separator + s[i]);

                                RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdAnn newRssOdAnn = rssFileUtil.replaceClassName(oldRssOdAnn, classMap);
                                rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                            }
                        }
                    }
                case "DLR3k_DLR-MVDA":
                    selfIdNameMap.put("pkw","car");selfIdNameMap.put("pkw_trail","car with trailer");
                    selfIdNameMap.put("truck","truck");selfIdNameMap.put("truck_trail","truck with trailer");
//                    selfIdNameMap.put("van_trail","Van_With_Trail");selfIdNameMap.put("cam","long truck");
                    selfIdNameMap.put("van_trail","van with trail");selfIdNameMap.put("cam","long truck");
                    selfIdNameMap.put("bus","bus");
                    selfIdNameMap.put("van","van");
                    classMap=RssExcelUtil.getClassMap(classMapPath, 9);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    for (String selfId : selfIdNameMap.keySet()) {
                        String selfName=selfIdNameMap.get(selfId);
                        if (classMap.containsKey(selfName)){
                            selfIdclassMap.put(selfId,classMap.get(selfName));
                        }else {
                            System.out.println("selfName:"+selfName+"无统一类别");
                        }
                    }
                    File dlrDir=new File(path);
                    if (dlrDir.isDirectory()){
                        File annDir=new File(path+File.separator+"Train/hbbxml");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path+File.separator+"Train/hbbxml"+ File.separator + s[i]);
                                File newLabelFile = new File(path+File.separator+"Train/unit_obbxml"+ File.separator + s[i]);

                                RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdAnn newRssOdAnn = rssFileUtil. replaceClassName(oldRssOdAnn, selfIdclassMap);
                                rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                            }
                        }
                    }
                    break;
                case "VEDAI":
                    selfIdNameMap.put("1", "Car");
                    selfIdNameMap.put("2", "Truck");
                    selfIdNameMap.put("23", "Ship");
                    selfIdNameMap.put("4", "Tractor");
                    selfIdNameMap.put("5", "Camping Car");
                    selfIdNameMap.put("9", "van");
                    selfIdNameMap.put("10", "vehicle");
                    selfIdNameMap.put("11", "pick-up");
                    selfIdNameMap.put("31", "plane");
                    selfIdNameMap.put("7", "Small Land Vehicle");
                    selfIdNameMap.put("8", "Large Land Vehicle");
                    classMap = RssExcelUtil.getClassMap(classMapPath, 15);
                    for (String className : classMap.keySet()) {
                        System.out.println(className + ":" + classMap.get(className));
                    }
                    for (String selfId : selfIdNameMap.keySet()) {
                        String selfName=selfIdNameMap.get(selfId);
                        if (classMap.containsKey(selfName)){
                            selfIdclassMap.put(selfId,classMap.get(selfName));
                        }else {
                            System.out.println("selfName:"+selfName+"无统一类别");
                        }
                    }
                    File VEDAIDir = new File(path);
                    if (VEDAIDir.isDirectory()) {
                        File annDir512 = new File(path + File.separator + "obbxml512new");
                        File annDir1024 = new File(path + File.separator + "obbxml1024new");
                        if (annDir512.isDirectory()) {
                            String s[] = annDir512.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path + File.separator + "obbxml512new" + File.separator + s[i]);
                                File newLabelFile = new File(path + File.separator + "unit512_obbxml" + File.separator + s[i]);

                                RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdAnn newRssOdAnn = rssFileUtil.replaceClassName(oldRssOdAnn, selfIdclassMap);
                                rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                            }
                        }
                        if (annDir1024.isDirectory()) {
                            String s[] = annDir1024.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path + File.separator + "obbxml1024new" + File.separator + s[i]);
                                File newLabelFile = new File(path + File.separator + "unit1024_obbxml" + File.separator + s[i]);

                                RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdAnn newRssOdAnn = rssFileUtil.replaceClassName(oldRssOdAnn, selfIdclassMap);
                                rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                            }
                        }
                    }
                    //vedai数据自身编号缺失
                    break;
                case "DIUx_xView":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 19);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    File xviewDir=new File(path);
                    if (xviewDir.isDirectory()){
                        File annDir=new File(path+File.separator+"hbbxml");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path+File.separator+"hbbxml"+ File.separator + s[i]);
                                File newLabelFile = new File(path+File.separator+ "unit_hbbxml" + File.separator + s[i]);

                                RssOdAnn oldRssOdAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdAnn newRssOdAnn = rssFileUtil.replaceClassName(oldRssOdAnn, classMap);
                                rssFileUtil.beanToXml(newRssOdAnn, RssOdAnn.class, newLabelFile.getPath());
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                            }
                        }
                    }
                default:
                    break;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        long endTime=System.currentTimeMillis();
        System.out.println("总耗时："+(endTime-startTime)/1000+"秒");
        return true;
    }


    public boolean toUnitOdAnn(String path,String dataSetName,String classMapPath){
        long startTime=System.currentTimeMillis();
        HashMap<String,String> selfIdNameMap=new HashMap<>();
        //获取类别映射map
        Map<String, String> classMap = new HashMap<>();
        Map<String,String> objectCodeMap=new HashMap<>();
        HashMap<String,Integer> totalObjectNumMap=new HashMap<>();
        try {
            switch (dataSetName){
                case "AIR-SARShip":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 13);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File airsarshipdsDir=new File(path);
                    if (airsarshipdsDir.isDirectory()){
                        File annDir=new File(path+File.separator+"AIR-SARShip-2.0-xml"+File.separator+"AIR-SARShip-2.0-xml");
                        if (annDir.isDirectory()){
                            String s[]= annDir.list();
                            for (int i=0;i<s.length;i++){
                                RssOdSample rssOdSample=new RssOdSample();
                                File labelFile=new File(path+File.separator+"AIR-SARShip-2.0-xml"+File.separator+"AIR-SARShip-2.0-xml"+File.separator+s[i]);
                                RssAirshipDsAnn airshipDsAnn=rssFileUtil.parseAIRSHIPAXML(labelFile);

                                RssOdAnn rssOdAnn=new RssOdAnn();
                                rssOdAnn.setFilename(airshipDsAnn.getSource().getFilename());
                                rssOdAnn.setFolder("AIRShip");

                                RssOdAnn.Size size=new RssOdAnn.Size();
                                size.setDepth("16");
                                size.setWidth("1000");
                                size.setHeight("1000");
                                rssOdAnn.setSize(size);

                                ArrayList<RssOdAnn.Object> objects=new ArrayList<RssOdAnn.Object>();
                                for(int num=0;num<airshipDsAnn.getObjects().getObject().size();num++) {
                                    RssOdAnn.Object object = new RssOdAnn.Object();
//                                    String unitName=objectCodeMap.get("ship");
                                    object.setName("ship");
//                                    object.setName(unitName);

                                    RssOdAnn.Object.Bndbox bndbox = new RssOdAnn.Object.Bndbox();
                                    String Xmin = airshipDsAnn.getObjects().getObject().get(num).getPoints().getPoint().get(0).substring(0, 3);
                                    String Ymin = airshipDsAnn.getObjects().getObject().get(num).getPoints().getPoint().get(0).substring(4);
                                    String Xmax = airshipDsAnn.getObjects().getObject().get(num).getPoints().getPoint().get(2).substring(0, 3);
                                    String Ymax = airshipDsAnn.getObjects().getObject().get(num).getPoints().getPoint().get(2).substring(4);
                                    bndbox.setXmin(Xmin);
                                    bndbox.setXmax(Xmax);
                                    bndbox.setYmin(Ymin);
                                    bndbox.setYmax(Ymax);

                                    object.setBndbox(bndbox);
                                    objects.add(object);
                                }
                                //System.out.println(objects);
                                rssOdAnn.setObject(objects);
                                String xmlPath = path+"/hbbxml/"+s[i];
                                //写入到xml文件
                                boolean success= rssFileUtil.beanToXml(rssOdAnn,RssOdAnn.class,xmlPath);
                                if (!success){
                                    System.out.println(s[i]+"转换失败！！！");
                                }
                            }
                        }
                    }
                    break;
                case "DIUx_xView":
                    List<RssOdAnn> rssOdAnns=rssFileUtil.xViewToRssOdAnn(path);

                    break;

                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    /**
     *
     * @param path
     * @param classMapPath
     * @param sheetNum
     * @param labelSuffix
     * @return
     */
    @SneakyThrows
    public boolean insertCDSample(String path, String classMapPath, int sheetNum,String labelSuffix){

        long startTime= System.currentTimeMillis();

        String bucketName = "change-detection";
        rssMinioUtil.createBucket(bucketName);

        LinkedList<RssCdSample> rssCdSampleList = new LinkedList<>();
        LinkedList<RssCdSampleClass> rssCdSampleClassList = new LinkedList<>();
        LinkedList<RssClassMap> rssClassMapList = new LinkedList<>();

        Map<String, String> classMap = RssExcelUtil.getClassMap(classMapPath, sheetNum);

        File baseDir = new File(path);
//        String grandParent = baseDir.getParentFile().getParent();
//        String parent = baseDir.getParent();
//        String datasetName = parent.replace(grandParent, "").replace("/","");
        String datasetName = "SRSBCDD";
        System.out.println("datasetName:"+datasetName);
//        Integer datasetID = rssPostgresqlService.getDatasetID(datasetName);
        Integer datasetID = 81;

        Integer maxCdSampleID = rssPostgresqlService.getMaxCdSampleId();
        if(maxCdSampleID==null){
            maxCdSampleID=0;
        }

        Integer maxCdSampleClassId = rssPostgresqlService.getMaxCdSampleClassId();
        if(maxCdSampleClassId==null){
            maxCdSampleClassId=0;
        }

        Integer maxClassMapId = rssPostgresqlService.getMaxClassMapId();
        if(maxCdSampleClassId==null){
            maxClassMapId=0;
        }

//        String ifTrnValTest = path.replace(parent, "").replace("/", "");
        String ifTrnValTest = "";
        Integer trnValTest = null;
        switch (ifTrnValTest){
            case "train":
                trnValTest=0;
                break;
            case "val":
                trnValTest=1;
                break;
            case "test":
                trnValTest=2;
                break;
            default:
                break;
        }

        //依据数据集赋值
        String preImageType=null,postImageType=null,preInstrument=null,postInstrument=null;
        Integer preImageChannel=null,postImageChannel=null;
        switch (datasetName){
            case "HRSCD":
                preImageType=postImageType="optical";
                preInstrument=postInstrument="aerial image";
                preImageChannel=postImageChannel=3;
                break;
            case "SRSBCDD":
                preImageType=postImageType="optical";
                preImageChannel=postImageChannel=3;
                break;
            default:
                break;
        }


        try{
            File T1Dir = new File(path + File.separator + "T1");
            if(T1Dir.isDirectory()){
                String[] T1Images = T1Dir.list();

                for (int i = 0; i < T1Images.length; i++) {
                    String T1ImagePath = path+File.separator+"T1"+File.separator+T1Images[i];
                    String T2ImagePath = path+File.separator+"T2"+File.separator+T1Images[i];

                    String suffix = T1Images[i].substring(T1Images[i].lastIndexOf(".")+1);

                    String labelPath = path+File.separator+"Label"+File.separator+T1Images[i].replace(suffix,labelSuffix);


                    String newT1ImageName,newT2ImageName,newLabelName;
                    if(trnValTest==null){
                        newT1ImageName = datasetName+"_"+(i+1)+"."+suffix;
                        newT2ImageName = newT1ImageName;
                        newLabelName = datasetName+"_"+(i+1)+"."+labelSuffix;
                    }else{
                        newT1ImageName = datasetName+trnValTest+"_"+(i+1)+"."+suffix;
                        newT2ImageName = newT1ImageName;
                        newLabelName = datasetName+trnValTest+"_"+(i+1)+"."+labelSuffix;
                    }

                    String minioT1ImageObject = datasetName+File.separator+"T1"+File.separator+newT1ImageName;
                    String minioT1ImagePath = bucketName+File.separator+minioT1ImageObject;

                    String minioT2ImageObject = datasetName+File.separator+"T2"+File.separator+newT2ImageName;
                    String minioT2ImagePath = bucketName+File.separator+minioT2ImageObject;

                    String minioLabelObject = datasetName+File.separator+"Label"+File.separator+newLabelName;
                    String minioLabelPath = bucketName+File.separator+minioLabelObject;

                    int width=0;
                    int height=0;
                    try {
                        BufferedImage bufferedImage = ImageIO.read(new File(T1ImagePath));
                        width = bufferedImage.getWidth();
                        height = bufferedImage.getHeight();
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    RssCdSample rssCdSample = new RssCdSample();

                    maxCdSampleID++;
                    rssCdSample.setId(maxCdSampleID);
                    rssCdSample.setDatasetId(datasetID);
                    rssCdSample.setSampleWidth(width);
                    rssCdSample.setSampleHeight(height);
                    rssCdSample.setPreImagePath(minioT1ImagePath);
                    rssCdSample.setPostImagePath(minioT2ImagePath);
                    rssCdSample.setLabelPath(minioLabelPath);
                    rssCdSample.setPreImageType(preImageType);
                    rssCdSample.setPostImageType(postImageType);
                    rssCdSample.setPreImageChannels(preImageChannel);
                    rssCdSample.setPostImageChannels(postImageChannel);
                    rssCdSample.setTrnValueTest(trnValTest);
                    rssCdSample.setPreInstrument(preInstrument);
                    rssCdSample.setPostInstrument(postInstrument);
                    rssCdSample.setCreateBy("admin");
                    rssCdSample.setCreateTime(new Timestamp(System.currentTimeMillis()));

                    rssCdSampleList.add(rssCdSample);

                    Set<Map.Entry<String, String>> entrySet = classMap.entrySet();
                    for (Map.Entry<String, String> entry : entrySet) {
                        RssCdSampleClass rssCdSampleClass = new RssCdSampleClass();

                        maxCdSampleClassId++;
                        rssCdSampleClass.setId(maxCdSampleClassId);
                        rssCdSampleClass.setSampleId(maxCdSampleID);
                        rssCdSampleClass.setSelfClassName(entry.getKey());
                        if(entry.getKey().equals("undefined")){
                            rssCdSampleClass.setUniClassId(rssPostgresqlService.getLcClassCode(null));
                        }else{
                            rssCdSampleClass.setUniClassId(rssPostgresqlService.getLcClassCode(entry.getValue()));
                        }
                        rssCdSampleClass.setCreateBy("admin");
                        rssCdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));

                        rssCdSampleClassList.add(rssCdSampleClass);
                    }

//                    rssMinioUtil.uploadFile(new File(T1ImagePath),bucketName,minioT1ImageObject);
                    System.out.println(T1ImagePath);
//                    rssMinioUtil.uploadFile(new File(T2ImagePath),bucketName,minioT2ImageObject);
//                    rssMinioUtil.uploadFile(new File(labelPath),bucketName,minioLabelObject);
                    System.out.println(labelPath);

                    double process = new BigDecimal( (i + 1) /(float)T1Images.length).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    System.out.println("上传成功："+T1Images[i]+"------进度："+process*100+"%");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        //插入映射表
        Set<Map.Entry<String, String>> classMapEntries = classMap.entrySet();
        for (Map.Entry<String, String> entry : classMapEntries) {
            RssClassMap rssClassMap = new RssClassMap();

            maxClassMapId++;
            rssClassMap.setId(maxClassMapId);
            rssClassMap.setDatasetId(datasetID);
            rssClassMap.setSelfClassName(entry.getKey());
            if(entry.getKey().equals("undefined")){
                rssClassMap.setUniClassName(null);
            }else{
                rssClassMap.setUniClassName(entry.getValue());
            }
            rssClassMap.setCreateBy("admin");
            rssClassMap.setCreateTime(new Timestamp(System.currentTimeMillis()));

            rssClassMapList.add(rssClassMap);
        }

        boolean insertBatchCdSample = iRssCdSampleService.insertBatch(rssCdSampleList);
        boolean insertBatchCdSampleClass = iRssCdSampleClassService.insertBatch(rssCdSampleClassList);
        boolean insertBatchClassMap = iRssClassMapService.insertBatch(rssClassMapList);

        long endTime = System.currentTimeMillis();

        long time = endTime-startTime;
        long min = time / (1000 * 60);
        long sec = time%(1000*60)/ 1000;
        System.out.println("============================");
        System.out.println("入库总耗时："+min+" min "+sec+" s");
//        return true;
        return insertBatchClassMap;
    }
    /**
     * Insert object detection samples into database.
     * @param path Path of object detection sample dataset.
     * @return Whether the data was inserted successfully.
     */
    public boolean insertODSample(String path,String dataSetName,String classMapPath)throws IOException,Exception {

        long startTime=System.currentTimeMillis();
        Integer datasetId=rssPostgresqlService.getDatasetID(dataSetName);
        HashMap<String,String> selfIdNameMap=new HashMap<>();
        //获取类别映射map
        Map<String, String> classMap = new HashMap<>();
        Map<String,String> objectCodeMap=new HashMap<>();
        List<String> wrongName=new LinkedList<>();
        List<String> rightName=new LinkedList<>();
        HashMap<String,Integer> totalObjectNumMap=new HashMap<>();
        //1.遍历xml文件获得目标类别，导入数据库
        //2.将标签和数据复制到新的目录

        //获得od_sample最大id
        Integer maxOdSampleId=rssPostgresqlService.getMaxOdSampleId();
        if (maxOdSampleId==null){
            maxOdSampleId=0;
        }
        //获得od_sample_class最大id
        Integer maxOdSampleClassId=rssPostgresqlService.getMaxOdSampleClassId();
        if (maxOdSampleClassId==null){
            maxOdSampleClassId=0;
        }
        List<RssOdSample> rssOdSampleList=new LinkedList<>();
        List<RssOdSampleClass> rssOdSampleClassList=new LinkedList<>();
        try {
            switch (dataSetName){
                case "bridges_dataset":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 4);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File brdsDir=new File(path);
                    if (brdsDir.isDirectory()){
//                        File annDir=new File(path+File.separator+"Annotations");
                        File annDir=new File(path+File.separator+"unit_hbbxml");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
//                                File f = new File(path+File.separator+"Annotations"+ File.separator + s[i]);
                                File f = new File(path+File.separator+"unit_hbbxml"+ File.separator + s[i]);
                                BridgeDsAnn bridgeDsAnn=rssFileUtil.parseBDAXML(f);
                                RssOdSample rssOdSample=new RssOdSample();

//                                datasetId=0;
                                Integer sampleWidth=Integer.parseInt(bridgeDsAnn.getSize().getWidth());
                                Integer sampleHeight=Integer.parseInt(bridgeDsAnn.getSize().getHeight());
                                String wkt="POINT(115 30)";
                                String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality="A";
                                String sampleLabeler=null;
                                Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox="HBB";
                                String imagePath=path+File.separator+"JPEGImages"+ File.separator + s[i].replace("xml","jpg");
                                String newImagePath="bridges_dataset-v1/image/";
                                String minioImagePath=newImagePath+s[i].replace("xml","jpg");
//                                String newLabelPath="bridges_dataset-v1/label_hbb/";
                                String newLabelPath="bridges_dataset-v1/label_unit_hbb/";
                                String minioLablePath=newLabelPath+ s[i];

                                String imageType="Optical";
                                Integer imageChannels=3;
                                String imageResolution="0.2~30";
                                String instrument=null;
                                Integer trnValueTest=0;
                                String createBy="admin";
                                Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                String updateBy=null;
                                Timestamp updateTime=null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);
                                //获取目标类别，判断是否重复
                                List<BridgeDsAnn.Object> objectList=bridgeDsAnn.getObject();
                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                if (objectList!=null){
                                    for (BridgeDsAnn.Object object:objectList){
                                        String objectName=object.getName();
                                        if (objectNumHashMap.containsKey(objectName)){
                                            Integer num=objectNumHashMap.get(objectName);
                                            num++;
                                            objectNumHashMap.put(objectName,num);
                                        }else {
                                            objectNumHashMap.put(objectName,1);
                                        }
                                    }
                                    for (String objectName:objectNumHashMap.keySet()){
//                                        System.out.println("self class:"+objectName);
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer num=objectNumHashMap.get(objectName);
                                        //获得目标类别编码


                                        String code=objectCodeMap.get(objectName);

                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                        rssOdSampleClassList.add(rssOdSampleClass);

                                    }
                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(f,"object-detection",minioLablePath);
                                if (i%50==0){
                                    long endTime=System.currentTimeMillis();
                                    System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                    System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                }
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile=new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                            }
                        }
                    }
                    break;
                case "DOTA-1.5":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 0);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File dotadsDir=new File(path);
                    if (dotadsDir.isDirectory()){
                        String[] trnValueTests=dotadsDir.list();

                        for (int i=0;i<trnValueTests.length;i++){
                            String trnValueTestStr=trnValueTests[i];
                            Integer trnValueTest=0;
                            switch (trnValueTestStr){
                                case "train":
                                    trnValueTest=0;
                                    break;
                                case "val":
                                    trnValueTest=1;
                                    break;
                                case "test":
                                    trnValueTest=2;
                                    break;
                                default:
                                    break;
                            }
//                            File annDir=new File(path+File.separator+trnValueTestStr+File.separator+"hbbxml");
                            File annDir=new File(path+File.separator+trnValueTestStr+File.separator+"unit_hbbxml");
                            if (annDir.isDirectory()){
                                String s[] = annDir.list();
                                for (int j = 0; j < s.length; j++) {
                                    RssOdSample rssOdSample=new RssOdSample();
//                                    File labelFile = new File(path+File.separator+trnValueTestStr+File.separator+"hbbxml"+ File.separator + s[j]);
                                    File labelFile = new File(path+File.separator+trnValueTestStr+File.separator+"unit_hbbxml"+ File.separator + s[j]);
                                    RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                    datasetId=2;
                                    Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                                    Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                                    String wkt="POINT(115 30)";
                                    String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                    Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                    String sampleQuality="A";
                                    String sampleLabeler=null;
                                    Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                    String lableBbox="HBB";
                                    String imagePath=path+File.separator+trnValueTestStr+File.separator+"images"+ File.separator + s[j].replace("xml","png");
                                    String newImagePath="dota-v1.5/image/";
                                    String minioImagePath=newImagePath+s[j].replace("xml","png");
//                                    String newLabelPath="dota-v1.5/label_hbb/";
                                    String newLabelPath="dota-v1.5/label_unit_hbb/";
                                    String minioLablePath=newLabelPath+ s[j];
                                    String imageType="Optical";
                                    Integer imageChannels=3;
                                    String imageResolution=null;
                                    String instrument=null;

                                    String createBy="admin";
                                    Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                    String updateBy=null;
                                    Timestamp updateTime=null;


                                    maxOdSampleId++;
                                    rssOdSample.setId(maxOdSampleId);
                                    rssOdSample.setDatasetId(datasetId);
                                    rssOdSample.setSampleWidth(sampleWidth);
                                    rssOdSample.setSampleHeight(sampleHeight);
                                    rssOdSample.setSampleArea(sampleArea);
                                    rssOdSample.setSampleDate(sampleDate);
                                    rssOdSample.setSampleQuality(sampleQuality);
                                    rssOdSample.setSampleLabeler(sampleLabeler);
                                    rssOdSample.setAnnotationDate(annotationDate);
                                    rssOdSample.setLabelBbox(lableBbox);
                                    rssOdSample.setImagePath(minioImagePath);
                                    rssOdSample.setLabelPath(minioLablePath);
                                    rssOdSample.setImageType(imageType);
                                    rssOdSample.setImageChannels(imageChannels);
                                    rssOdSample.setImageResolution(imageResolution);
                                    rssOdSample.setInstrument(instrument);
                                    rssOdSample.setTrnValueTest(trnValueTest);
                                    rssOdSample.setCreateBy(createBy);
                                    rssOdSample.setCreateTime(createTime);
                                    rssOdSample.setUpdateBy(updateBy);
                                    rssOdSample.setUpdateTime(updateTime);
                                    rssOdSampleList.add(rssOdSample);

                                    //获取目标类别，判断是否重复
                                    List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
                                    HashMap<String,Integer> objectNumHashMap=new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                    if (objectList!=null){
                                        for (RssOdAnn.Object object:objectList){
                                            String objectName=object.getName();
                                            if (objectNumHashMap.containsKey(objectName)){
                                                Integer num=objectNumHashMap.get(objectName);
                                                num++;
                                                objectNumHashMap.put(objectName,num);
                                            }else {
                                                objectNumHashMap.put(objectName,1);
                                            }
                                        }
                                        for (String objectName:objectNumHashMap.keySet()){
//                                        System.out.println("self class:"+objectName);
                                            RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                            Integer num=objectNumHashMap.get(objectName);
                                            //获得目标类别编码


                                            String code=objectCodeMap.get(objectName);
                                            if (code==null){
                                                System.out.println(minioLablePath+"objectName:"+objectName+"没有code");
                                            }
                                            maxOdSampleClassId++;
                                            rssOdSampleClass.setId(maxOdSampleClassId);
                                            rssOdSampleClass.setClassId(code);
                                            rssOdSampleClass.setSampleId(maxOdSampleId);
                                            rssOdSampleClass.setObjectNum(num);
                                            rssOdSampleClass.setCreateBy("admin");
                                            rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                            rssOdSampleClass.setUpdateBy(null);
                                            rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                            rssOdSampleClassList.add(rssOdSampleClass);

                                        }
                                    }else {
                                        System.out.println("该样本没有目标！！！");
                                    }



                                    //2.1标签文件复制到对应的目录下
                                    rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);


                                    if (j%50==0){
                                        long endTime=System.currentTimeMillis();
                                        System.out.print("上传"+((float)j/s.length)*100+"%完成");
                                        System.out.println("上传"+j+"耗时："+(endTime-startTime)/1000+"秒");
                                    }

                                    //2.2数据文件复制到对应的目录下
                                    File oldImageFile=new File(imagePath);
//                                    rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                                }
                            }
                        }
                    }
                    break;
                case "UCAS-AOD":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 7);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File ucasaoddsdir=new File(path);
                    if (ucasaoddsdir.isDirectory()) {
                        String[] classNames = ucasaoddsdir.list();
                        for (String className : classNames) {
                            if (className.equals("CAR")||className.equals("PLANE")) {
                                File annDir = new File(path + File.separator + className + File.separator + "rbbxml");
                                String s[] = annDir.list();
                                for (int i = 0; i < s.length; i++) {
                                    RssOdSample rssOdSample = new RssOdSample();
                                    File labelFile = new File(path + File.separator + className + File.separator + "rbbxml" + File.separator + s[i]);
                                    RssOdAnn rssOdAnn = rssFileUtil.parseDOTAAXML(labelFile);

                                    Integer sampleWidth = Integer.parseInt(rssOdAnn.getSize().getWidth());
                                    Integer sampleHeight = Integer.parseInt(rssOdAnn.getSize().getHeight());
                                    String wkt = "POINT(115 30)";
                                    String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                    Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                    String sampleQuality = "A";
                                    String sampleLabeler = null;
                                    Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                    String lableBbox = "OBB";
                                    String imagePath = path + File.separator + className + File.separator + "images" + File.separator + s[i].replace("xml", "png");
                                    String newImagePath = "ucas-aod-v1/image/"+className+"-";
//                                    System.out.println("newImagePath:"+newImagePath);
                                    String minioImagePath = newImagePath + s[i].replace("xml", "png");
                                    String newLabelPath = "ucas-aod-v1/label_obb/"+className+"-";
                                    String minioLablePath = newLabelPath + s[i];
                                    String imageType = "Optical";
                                    Integer imageChannels = 3;
                                    String imageResolution = null;
                                    String instrument = null;
                                    Integer trnValueTest = 0;
                                    String createBy = "admin";
                                    Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                    String updateBy = null;
                                    Timestamp updateTime = null;

                                    maxOdSampleId++;
                                    rssOdSample.setId(maxOdSampleId);
                                    rssOdSample.setDatasetId(datasetId);
                                    rssOdSample.setSampleWidth(sampleWidth);
                                    rssOdSample.setSampleHeight(sampleHeight);
                                    rssOdSample.setSampleArea(sampleArea);
                                    rssOdSample.setSampleDate(sampleDate);
                                    rssOdSample.setSampleQuality(sampleQuality);
                                    rssOdSample.setSampleLabeler(sampleLabeler);
                                    rssOdSample.setAnnotationDate(annotationDate);
                                    rssOdSample.setLabelBbox(lableBbox);
                                    rssOdSample.setImagePath(minioImagePath);
                                    rssOdSample.setLabelPath(minioLablePath);
                                    rssOdSample.setImageType(imageType);
                                    rssOdSample.setImageChannels(imageChannels);
                                    rssOdSample.setImageResolution(imageResolution);
                                    rssOdSample.setInstrument(instrument);
                                    rssOdSample.setTrnValueTest(trnValueTest);
                                    rssOdSample.setCreateBy(createBy);
                                    rssOdSample.setCreateTime(createTime);
                                    rssOdSample.setUpdateBy(updateBy);
                                    rssOdSample.setUpdateTime(updateTime);
                                    rssOdSampleList.add(rssOdSample);

                                    List<RssOdAnn.Object> objectList = rssOdAnn.getObject();
//                                    for (int j = 0; j < objectList.size(); j++) {
//                                        System.out.println(objectList.get(j).getName());
//                                    }
                                    Integer num = objectList.size();


                                    //获得目标类别编码
                                    String code = objectCodeMap.get(className);
                                    if (code==null){
                                        if (!wrongName.contains(className)){
                                            wrongName.add(className);
                                        }
                                    }else {
                                        if (!rightName.contains(className)){
                                            rightName.add(className);
                                        }
                                    }
                                    RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                    maxOdSampleClassId++;
                                    rssOdSampleClass.setId(maxOdSampleClassId);
                                    rssOdSampleClass.setClassId(code);
                                    rssOdSampleClass.setSampleId(maxOdSampleId);
                                    rssOdSampleClass.setObjectNum(num);
//                                    System.out.println("Num:" + num);
                                    rssOdSampleClass.setCreateBy("admin");
                                    rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                    rssOdSampleClass.setUpdateBy(null);
                                    rssOdSampleClass.setUpdateTime(null);
                                    rssOdSampleClassList.add(rssOdSampleClass);

                                    //2.1标签文件复制到对应的目录下
                                    rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);
                                    if (i%50==0){
                                        long endTime=System.currentTimeMillis();
                                        System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                        System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                    }
                                    //2.2数据文件复制到对应的目录下
                                    File oldImageFile=new File(imagePath);
                                    rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);

                                }
                            } else if(className.equals("Neg")){
                                File NegDir = new File(path + File.separator + className);
                                String[] n = NegDir.list();
                                for (int m = 0; m < n.length; m++) {
                                    RssOdSample rssOdSample = new RssOdSample();
                                    if (n[m].endsWith("png")){
                                        File f = new File(path + File.separator + className + File.separator + n[m]);
                                        try {
                                            BufferedImage image1 = ImageIO.read(f);
                                            int width = image1.getWidth();
                                            int height = image1.getHeight();
                                            Integer sampleWidth =width;
                                            Integer sampleHeight = height;
//                                            System.out.println("sampleWidth:"+sampleWidth);
                                            String wkt = "POINT(115 30)";
                                            String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                            Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                            String sampleQuality = "A";
                                            String sampleLabeler = null;
                                            Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                            String lableBbox = "OBB";
                                            String imagePath = path + File.separator + className + File.separator + n[m];
                                            String newImagePath = "ucas-aod-v1/image/"+className+"-";
                                            String minioImagePath = newImagePath + n[m];
                                            String imageType = "Optical";
                                            Integer imageChannels = 3;
                                            String imageResolution = null;
                                            String instrument = null;
                                            Integer trnValueTest = 0;
                                            String createBy = "admin";
                                            Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                            String updateBy = null;
                                            Timestamp updateTime = null;

                                            maxOdSampleId++;
                                            rssOdSample.setId(maxOdSampleId);
                                            rssOdSample.setDatasetId(datasetId);
                                            rssOdSample.setSampleWidth(sampleWidth);
                                            rssOdSample.setSampleHeight(sampleHeight);
                                            rssOdSample.setSampleArea(sampleArea);
                                            rssOdSample.setSampleDate(sampleDate);
                                            rssOdSample.setSampleQuality(sampleQuality);
                                            rssOdSample.setSampleLabeler(sampleLabeler);
                                            rssOdSample.setAnnotationDate(annotationDate);
                                            rssOdSample.setLabelBbox(lableBbox);
                                            rssOdSample.setImagePath(minioImagePath);
                                            //rssOdSample.setLabelPath(minioLablePath);
                                            rssOdSample.setImageType(imageType);
                                            rssOdSample.setImageChannels(imageChannels);
                                            rssOdSample.setImageResolution(imageResolution);
                                            rssOdSample.setInstrument(instrument);
                                            rssOdSample.setTrnValueTest(trnValueTest);
                                            rssOdSample.setCreateBy(createBy);
                                            rssOdSample.setCreateTime(createTime);
                                            rssOdSample.setUpdateBy(updateBy);
                                            rssOdSample.setUpdateTime(updateTime);
                                            rssOdSampleList.add(rssOdSample);

                                            if (m%50==0){
                                                long endTime=System.currentTimeMillis();
                                                System.out.print("上传"+((float)m/n.length)*100+"%完成");
                                                System.out.println("上传"+m+"耗时："+(endTime-startTime)/1000+"秒");
                                            }
                                            //2.2数据文件复制到对应的目录下
                                            File oldImageFile=new File(imagePath);
                                            rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                                        }catch (Exception e){
                                            System.out.println(f.getName()+"读取影像有误");
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            }
                        }
                    }
                    for (String name:wrongName){
                        System.out.println("wrongName"+name);
                    }
                    break;
                case "DOTA-2.0":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 2);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File dotadsTwoDir=new File(path);
                    if (dotadsTwoDir.isDirectory()){
                        String[] trnValueTests=dotadsTwoDir.list();

                        for (int i=0;i<trnValueTests.length;i++){
                            String trnValueTestStr=trnValueTests[i];
                            Integer trnValueTest=0;
                            switch (trnValueTestStr){
                                case "train":
                                    trnValueTest=0;
                                    break;
                                case "val":
                                    trnValueTest=1;
                                    break;
                                case "test-dev":
                                    trnValueTest=2;
                                    break;
                                default:
                                    break;
                            }
//                            File annDir=new File(path+File.separator+trnValueTestStr+File.separator+"hbbxml");
                            File annDir=new File(path+File.separator+trnValueTestStr+File.separator+"unit_hbbxml");
                            if (annDir.isDirectory()){
                                String s[] = annDir.list();
                                for (int j = 0; j < s.length; j++) {
                                    RssOdSample rssOdSample=new RssOdSample();
//                                    File labelFile = new File(path+File.separator+trnValueTestStr+File.separator+"hbbxml"+ File.separator + s[j]);
                                    File labelFile = new File(path+File.separator+trnValueTestStr+File.separator+"unit_hbbxml"+ File.separator + s[j]);
                                    RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                    datasetId=9;
                                    Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                                    Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                                    String wkt="POINT(115 30)";
                                    String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                    Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                    String sampleQuality="A";
                                    String sampleLabeler=null;
                                    Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                    String lableBbox="HBB";
                                    String imagePath=path+File.separator+trnValueTestStr+File.separator+"images"+ File.separator + s[j].replace("xml","png");
                                    String newImagePath="dota-v2.0/image/";
                                    String minioImagePath=newImagePath+s[j].replace("xml","png");
//                                    String newLabelPath="dota-v2.0/label_hbb/";
                                    String newLabelPath="dota-v2.0/label_unit_hbb/";
                                    String minioLablePath=newLabelPath+ s[j];
                                    String imageType="Optical";
                                    Integer imageChannels=3;
                                    String imageResolution=null;
                                    String instrument=null;

                                    String createBy="admin";
                                    Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                    String updateBy=null;
                                    Timestamp updateTime=null;

                                    maxOdSampleId++;
                                    rssOdSample.setId(maxOdSampleId);
                                    rssOdSample.setDatasetId(datasetId);
                                    rssOdSample.setSampleWidth(sampleWidth);
                                    rssOdSample.setSampleHeight(sampleHeight);
                                    rssOdSample.setSampleArea(sampleArea);
                                    rssOdSample.setSampleDate(sampleDate);
                                    rssOdSample.setSampleQuality(sampleQuality);
                                    rssOdSample.setSampleLabeler(sampleLabeler);
                                    rssOdSample.setAnnotationDate(annotationDate);
                                    rssOdSample.setLabelBbox(lableBbox);
                                    rssOdSample.setImagePath(minioImagePath);
                                    rssOdSample.setLabelPath(minioLablePath);
                                    rssOdSample.setImageType(imageType);
                                    rssOdSample.setImageChannels(imageChannels);
                                    rssOdSample.setImageResolution(imageResolution);
                                    rssOdSample.setInstrument(instrument);
                                    rssOdSample.setTrnValueTest(trnValueTest);
                                    rssOdSample.setCreateBy(createBy);
                                    rssOdSample.setCreateTime(createTime);
                                    rssOdSample.setUpdateBy(updateBy);
                                    rssOdSample.setUpdateTime(updateTime);
                                    rssOdSampleList.add(rssOdSample);

                                    //获取目标类别，判断是否重复
                                    List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
                                    HashMap<String,Integer> objectNumHashMap=new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                    if (objectList!=null){
                                        for (RssOdAnn.Object object:objectList){
                                            String objectName=object.getName();
                                            if (objectNumHashMap.containsKey(objectName)){
                                                Integer num=objectNumHashMap.get(objectName);
                                                num++;
                                                objectNumHashMap.put(objectName,num);
                                            }else {
                                                objectNumHashMap.put(objectName,1);
                                            }
                                        }
                                        for (String objectName:objectNumHashMap.keySet()){
//                                        System.out.println("self class:"+objectName);
                                            RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                            Integer num=objectNumHashMap.get(objectName);
                                            //获得目标类别编码

                                            String code=objectCodeMap.get(objectName);

                                            if (code==null){
                                                System.out.println(minioLablePath+"objectName:"+objectName+"没有code");
                                            }
                                            maxOdSampleClassId++;
                                            rssOdSampleClass.setId(maxOdSampleClassId);
                                            rssOdSampleClass.setClassId(code);
                                            rssOdSampleClass.setSampleId(maxOdSampleId);
                                            rssOdSampleClass.setObjectNum(num);
                                            rssOdSampleClass.setCreateBy("admin");
                                            rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                            rssOdSampleClass.setUpdateBy(null);
                                            rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                            rssOdSampleClassList.add(rssOdSampleClass);

                                        }
                                    }else {
                                        System.out.println("该样本没有目标！！！");
                                    }



                                    //2.1标签文件复制到对应的目录下
                                    rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);


                                    if (j%50==0){
                                        long endTime=System.currentTimeMillis();
                                        System.out.print("上传"+((float)j/s.length)*100+"%完成");
                                        System.out.println("上传"+j+"耗时："+(endTime-startTime)/1000+"秒");
                                    }

                                    //2.2数据文件复制到对应的目录下
                                    File oldImageFile=new File(imagePath);
//                                    rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                                }
                            }
                        }
                    }
                    break;
                case "RSOD":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 1);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File rsoddsDir=new File(path);
                    if (rsoddsDir.isDirectory()){
                        String[] classNames=rsoddsDir.list();
                        for (String className:classNames){
//                            File annDir=new File(path+File.separator+className+File.separator+"Annotation"+File.separator+"xml");
                            File annDir=new File(path+File.separator+className+File.separator+"Annotation"+File.separator+"unit_hbbxml");
                            if (annDir.isDirectory()){
                                String s[] = annDir.list();
                                for (int i = 0; i < s.length; i++) {
                                    RssOdSample rssOdSample=new RssOdSample();
//                                    File labelFile = new File(path+File.separator+className+File.separator+"Annotation"+File.separator+"xml"+ File.separator + s[i]);
                                    File labelFile = new File(path+File.separator+className+File.separator+"Annotation"+File.separator+"unit_hbbxml"+ File.separator + s[i]);
                                    String labelFileName=labelFile.getName();
                                    String lastNameString=labelFileName.substring(labelFileName.lastIndexOf(".")-1,labelFileName.lastIndexOf("."));
                                    Integer trnValueTest=0;
                                    if (lastNameString.equals("1")||lastNameString.equals("9")){
                                        trnValueTest=2;
                                    }else {
                                        trnValueTest=0;
                                    }
                                    RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                    Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                                    Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                                    String wkt="POINT(115 30)";
                                    String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                    Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                    String sampleQuality="A";
                                    String sampleLabeler=null;
                                    Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                    String lableBbox="HBB";
                                    String imagePath=path+File.separator+className+File.separator+"JPEGImages"+ File.separator + s[i].replace("xml","jpg");
                                    String newImagePath="rsod-v1/image/";
                                    String minioImagePath=newImagePath+s[i].replace("xml","jpg");
//                                    String newLabelPath="rsod-v1/label_hbb/";
                                    String newLabelPath="rsod-v1/label_unit_hbb/";
                                    String minioLablePath=newLabelPath+ s[i];
                                    String imageType="Optical";
                                    Integer imageChannels=3;
                                    String imageResolution=null;
                                    String instrument=null;

                                    String createBy="admin";
                                    Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                    String updateBy=null;
                                    Timestamp updateTime=null;


                                    maxOdSampleId++;
                                    rssOdSample.setId(maxOdSampleId);
                                    rssOdSample.setDatasetId(datasetId);
                                    rssOdSample.setSampleWidth(sampleWidth);
                                    rssOdSample.setSampleHeight(sampleHeight);
                                    rssOdSample.setSampleArea(sampleArea);
                                    rssOdSample.setSampleDate(sampleDate);
                                    rssOdSample.setSampleQuality(sampleQuality);
                                    rssOdSample.setSampleLabeler(sampleLabeler);
                                    rssOdSample.setAnnotationDate(annotationDate);
                                    rssOdSample.setLabelBbox(lableBbox);
                                    rssOdSample.setImagePath(minioImagePath);
                                    rssOdSample.setLabelPath(minioLablePath);
                                    rssOdSample.setImageType(imageType);
                                    rssOdSample.setImageChannels(imageChannels);
                                    rssOdSample.setImageResolution(imageResolution);
                                    rssOdSample.setInstrument(instrument);
                                    rssOdSample.setTrnValueTest(trnValueTest);
                                    rssOdSample.setCreateBy(createBy);
                                    rssOdSample.setCreateTime(createTime);
                                    rssOdSample.setUpdateBy(updateBy);
                                    rssOdSample.setUpdateTime(updateTime);
                                    rssOdSampleList.add(rssOdSample);

                                    //获取目标类别，判断是否重复
                                    List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
                                    Integer num=objectList.size();



                                    //获得目标类别编码
                                    String code=objectCodeMap.get(className);
                                    RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                    maxOdSampleClassId++;
                                    rssOdSampleClass.setId(maxOdSampleClassId);
                                    rssOdSampleClass.setClassId(code);
                                    rssOdSampleClass.setSampleId(maxOdSampleId);
                                    rssOdSampleClass.setObjectNum(num);
                                    rssOdSampleClass.setCreateBy("admin");
                                    rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                    rssOdSampleClass.setUpdateBy(null);
                                    rssOdSampleClass.setUpdateTime(null);
                                    rssOdSampleClassList.add(rssOdSampleClass);

                                    //2.1标签文件复制到对应的目录下
                                    rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);

                                    if (i%50==0){
                                        long endTime=System.currentTimeMillis();
                                        System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                        System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                    }
                                    //2.2数据文件复制到对应的目录下
                                    File oldImageFile=new File(imagePath);
//                                    rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);

                                }
                            }
                        }

                    }
                    break;
                case "SSDD":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 3);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File ssdddsDir=new File(path);
                    if (ssdddsDir.isDirectory()){
//                        File annDir=new File(path+File.separator+"newAnnotations");
                        File annDir=new File(path+File.separator+"unit_hbbxml");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                RssOdSample rssOdSample=new RssOdSample();
//                                File labelFile = new File(path+File.separator+"newAnnotations"+ File.separator + s[i]);
                                File labelFile = new File(path+File.separator+"unit_hbbxml"+ File.separator + s[i]);
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                                String wkt="POINT(115 30)";
                                String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality="A";
                                String sampleLabeler=null;
                                Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox="HBB";
                                String imagePath=path+File.separator+"JPEGImages"+ File.separator + s[i].replace("xml","jpg");
                                String newImagePath="ssdd-v1/image/";
                                String minioImagePath=newImagePath+s[i].replace("xml","jpg");
//                                String newLabelPath="ssdd-v1/label_hbb/";
                                String newLabelPath="ssdd-v1/label_unit_hbb/";
                                String minioLablePath=newLabelPath+ s[i];
                                String imageType="Sar";
                                Integer imageChannels=3;
                                String imageResolution=null;
                                String instrument=null;
                                Integer trnValueTest=0;
                                String createBy="admin";
                                Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                String updateBy=null;
                                Timestamp updateTime=null;


                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);

                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
//                                System.out.println("objectList.size():"+objectList.size());
                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                for (RssOdAnn.Object object:objectList){
                                    String objectName=object.getName();
                                    if (objectNumHashMap.containsKey(objectName)){
                                        Integer num=objectNumHashMap.get(objectName);
                                        num++;
                                        objectNumHashMap.put(objectName,num);
                                    }else {
                                        objectNumHashMap.put(objectName,1);
                                    }
                                }
                                if (objectList!=null){
                                    for (String objectName:objectNumHashMap.keySet()){
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer num=objectNumHashMap.get(objectName);
                                        String code=objectCodeMap.get(objectName);

                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
                                        rssOdSampleClassList.add(rssOdSampleClass);
                                    }

                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }
//                                System.out.println("rssOdSampleClassList.size():"+rssOdSampleClassList.size());
                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);


                                if (i%50==0){
                                    long endTime=System.currentTimeMillis();
                                    System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                    System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                }

                                //2.2数据文件复制到对应的目录下
                                File oldImageFile=new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);

                            }
                        }
                    }
                    break;
                case "NWPU VHR-10 dataset":
                    //1-airplane, 2-ship, 3-storage tank, 4-baseball diamond, 5-tennis court, 6-basketball court, 7-ground track field, 8-harbor, 9-bridge, 10-vehicle
                    selfIdNameMap.put("1","airplane");selfIdNameMap.put("2","ship");
                    selfIdNameMap.put("3","storage tank");selfIdNameMap.put("4","baseball diamond");
                    selfIdNameMap.put("5","tennis court");selfIdNameMap.put("6","basketball court");
                    selfIdNameMap.put("7","ground track field");selfIdNameMap.put("8","harbor");
                    selfIdNameMap.put("9","bridge");selfIdNameMap.put("10","vehicle");
                    classMap=RssExcelUtil.getClassMap(classMapPath, 5);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    System.out.println("objectCodeMap.size():"+objectCodeMap.size());
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File nwpuvhrDir=new File(path);
                    if (nwpuvhrDir.isDirectory()){
                        File annDir=new File(path+File.separator+"hbbxml");
                        String[] s=annDir.list();
                        for (int i = 0; i < s.length; i++) {
                            RssOdSample rssOdSample=new RssOdSample();
                            File labelFile = new File(path+File.separator+"hbbxml"+ File.separator + s[i]);
                            RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                            Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                            Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                            String wkt="POINT(115 30)";
                            String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                            Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                            String sampleQuality="A";
                            String sampleLabeler=null;
                            Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                            String lableBbox="HBB";
                            String imagePath=path+File.separator+"positive image set"+ File.separator + s[i].replace("xml","jpg");
                            String newImagePath="NWPU-VHR-10-dataset-v1/image/";
                            String minioImagePath=newImagePath+s[i].replace("xml","jpg");
                            String newLabelPath="NWPU-VHR-10-dataset-v1/label_hbb/";
                            String minioLablePath=newLabelPath+ s[i];
                            String imageType="Optical";
                            Integer imageChannels=3;
                            String imageResolution=null;
                            String instrument=null;
                            Integer trnValueTest=0;
                            String createBy="admin";
                            Timestamp createTime=new Timestamp(System.currentTimeMillis());
                            String updateBy=null;
                            Timestamp updateTime=null;

                            maxOdSampleId++;
                            rssOdSample.setId(maxOdSampleId);
                            rssOdSample.setDatasetId(datasetId);
                            rssOdSample.setSampleWidth(sampleWidth);
                            rssOdSample.setSampleHeight(sampleHeight);
                            rssOdSample.setSampleArea(sampleArea);
                            rssOdSample.setSampleDate(sampleDate);
                            rssOdSample.setSampleQuality(sampleQuality);
                            rssOdSample.setSampleLabeler(sampleLabeler);
                            rssOdSample.setAnnotationDate(annotationDate);
                            rssOdSample.setLabelBbox(lableBbox);
                            rssOdSample.setImagePath(minioImagePath);
                            rssOdSample.setLabelPath(minioLablePath);
                            rssOdSample.setImageType(imageType);
                            rssOdSample.setImageChannels(imageChannels);
                            rssOdSample.setImageResolution(imageResolution);
                            rssOdSample.setInstrument(instrument);
                            rssOdSample.setTrnValueTest(trnValueTest);
                            rssOdSample.setCreateBy(createBy);
                            rssOdSample.setCreateTime(createTime);
                            rssOdSample.setUpdateBy(updateBy);
                            rssOdSample.setUpdateTime(updateTime);
                            rssOdSampleList.add(rssOdSample);


                            //获取目标类别，判断是否重复
                            List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
//                                System.out.println("objectList.size():"+objectList.size());
                            HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                            for (RssOdAnn.Object object:objectList){
                                String objectNameId=object.getName();
                                String objectName=selfIdNameMap.get(objectNameId);
                                if (objectName==null||objectName.equals("null")){
                                    System.out.println("objectNameId:"+objectNameId);
                                }
                                if (objectNumHashMap.containsKey(objectName)){
                                    Integer num=objectNumHashMap.get(objectName);
                                    num++;
                                    objectNumHashMap.put(objectName,num);
                                }else {
                                    objectNumHashMap.put(objectName,1);
                                }
                            }


                            if (objectList!=null){
                                for (String objectName:objectNumHashMap.keySet()){
                                    RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                    Integer num=objectNumHashMap.get(objectName);
                                    String code=objectCodeMap.get(objectName);
//                                    System.out.println("code"+code);
                                    if (code==null){
                                        if (!wrongName.contains(objectName)){
                                            wrongName.add(objectName);
                                        }
                                        System.out.println("imagePath:"+imagePath);
                                    }else {
                                        if (!rightName.contains(objectName)){
                                            rightName.add(objectName);
                                        }
                                    }
                                    maxOdSampleClassId++;
                                    rssOdSampleClass.setId(maxOdSampleClassId);
                                    rssOdSampleClass.setClassId(code);
                                    rssOdSampleClass.setSampleId(maxOdSampleId);
                                    rssOdSampleClass.setObjectNum(num);
                                    rssOdSampleClass.setCreateBy("admin");
                                    rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                    rssOdSampleClass.setUpdateBy(null);
                                    rssOdSampleClass.setUpdateTime(null);
                                    rssOdSampleClassList.add(rssOdSampleClass);
                                }

                            }else {
                                System.out.println("该样本没有目标！！！");
                            }
//                                System.out.println("rssOdSampleClassList.size():"+rssOdSampleClassList.size());
                            //2.1标签文件复制到对应的目录下
//                            rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);


                            if (i%50==0){
                                long endTime=System.currentTimeMillis();
                                System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                            }

                            //2.2数据文件复制到对应的目录下
                            File oldImageFile=new File(imagePath);
//                            rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                        }
                    }
                    for (String name:wrongName){
                        System.out.println("wrongName"+name);
                    }
                    break;
                case "TGRS-HRRSD":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 6);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    System.out.println("objectCodeMap.size():"+objectCodeMap.size());
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File tgrsHrrsdDir=new File(path);
                    if (tgrsHrrsdDir.isDirectory()){
                        File annDir=new File(path+File.separator+"Annotations");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                RssOdSample rssOdSample=new RssOdSample();
                                File labelFile = new File(path+File.separator+"Annotations"+ File.separator + s[i]);
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                                String wkt="POINT(115 30)";
                                String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality="A";
                                String sampleLabeler=rssOdAnn.getOwner().getName();
                                Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox="HBB";
                                String imagePath=path+File.separator+"JPEGImages"+ File.separator +"JPEGImages-1"+ File.separator+ s[i].replace("xml","jpg");
                                String newImagePath="TGRS-HRRSD-v1/image/";
                                String minioImagePath=newImagePath+s[i].replace("xml","jpg");
                                String newLabelPath="TGRS-HRRSD-v1/label_hbb/";
                                String minioLablePath=newLabelPath+ s[i];
                                String imageType="Optical";
                                Integer imageChannels=3;
                                String imageResolution=null;
                                String instrument=null;
                                Integer trnValueTest=0;
                                String createBy="admin";
                                Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                String updateBy=null;
                                Timestamp updateTime=null;


                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);



                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
//                                System.out.println("objectList.size():"+objectList.size());
                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                for (RssOdAnn.Object object:objectList){
                                    String objectName=object.getName();
                                    if (objectNumHashMap.containsKey(objectName)){
                                        Integer num=objectNumHashMap.get(objectName);
                                        num++;
                                        objectNumHashMap.put(objectName,num);
                                    }else {
                                        objectNumHashMap.put(objectName,1);
                                    }
                                }
                                for (String objectName:objectNumHashMap.keySet()){
                                    if (totalObjectNumMap.containsKey(objectName)){
                                        Integer num=totalObjectNumMap.get(objectName);
                                        num++;
                                        totalObjectNumMap.put(objectName,num);
                                    }else {
                                        totalObjectNumMap.put(objectName,1);
                                    }
                                }

                                if (objectList!=null){
                                    for (String objectName:objectNumHashMap.keySet()){
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer num=objectNumHashMap.get(objectName);
                                        String code=objectCodeMap.get(objectName);
//                                    System.out.println("code"+code);

                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
                                        rssOdSampleClassList.add(rssOdSampleClass);
                                    }

                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);


                                if (i%50==0){
                                    long endTime=System.currentTimeMillis();
                                    System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                    System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                }

                                //2.2数据文件复制到对应的目录下
                                File oldImageFile=new File(imagePath);
                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);

                            }
                            for (String objectName:totalObjectNumMap.keySet()){
                                System.out.println(objectName+":"+totalObjectNumMap.get(objectName));
                            }
                        }
                    }
                    break;
                case "DIOR":

                    //读取/ImageSets/Main下text文件，获得dior数据的train test val类型
                    String mainDirPath=path+"/ImageSets/Main";
                    HashMap<String,Integer> imageNameAppTypeMap=getImageNameAppTypeMap(mainDirPath);
                    classMap=RssExcelUtil.getClassMap(classMapPath, 8);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    System.out.println("objectCodeMap.size():"+objectCodeMap.size());
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File diorDir=new File(path);
                    if (diorDir.isDirectory()){
                        File annDir=new File(path+File.separator+"hbbxml");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                RssOdSample rssOdSample=new RssOdSample();
                                File labelFile = new File(path+File.separator+"hbbxml"+ File.separator + s[i]);
                                String fileId=s[i].substring(0,s[i].lastIndexOf("."));
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                                String wkt="POINT(115 30)";
                                String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality="A";
//                                String sampleLabeler=rssOdAnn.getOwner().getName();
                                String sampleLabeler=null;
                                Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox="HBB";
                                Integer trnValueTest=imageNameAppTypeMap.get(fileId);//修改
//                                System.out.println("fileId"+fileId);
//                                System.out.println("imageNameAppTypeMap.size()"+imageNameAppTypeMap.size());
                                String imagePath=null;
                               if (trnValueTest==2){
                                   imagePath=path+File.separator+"JPEGImages-test"+ File.separator+ s[i].replace("xml","jpg");
                                }else {
                                   imagePath=path+File.separator+"JPEGImages-trainval"+ File.separator+ s[i].replace("xml","jpg");
                               }
                                String newImagePath="DIOR-v1/image/";
                                String minioImagePath=newImagePath+s[i].replace("xml","jpg");
                                String newLabelPath="DIOR-v1/label_hbb/";
                                String minioLablePath=newLabelPath+ s[i];
                                String imageType="Optical";
                                Integer imageChannels=3;
                                String imageResolution=null;
                                String instrument=null;

                                String createBy="admin";
                                Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                String updateBy=null;
                                Timestamp updateTime=null;


                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);


                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
//                                System.out.println("objectList.size():"+objectList.size());
                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                for (RssOdAnn.Object object:objectList){
                                    String objectName=object.getName();
                                    if (objectNumHashMap.containsKey(objectName)){
                                        Integer num=objectNumHashMap.get(objectName);
                                        num++;
                                        objectNumHashMap.put(objectName,num);
                                    }else {
                                        objectNumHashMap.put(objectName,1);
                                    }
                                }
                                for (String objectName:objectNumHashMap.keySet()){
                                    if (totalObjectNumMap.containsKey(objectName)){
                                        Integer num=totalObjectNumMap.get(objectName);
                                        num++;
                                        totalObjectNumMap.put(objectName,num);
                                    }else {
                                        totalObjectNumMap.put(objectName,1);
                                    }
                                }

                                if (objectList!=null){
                                    for (String objectName:objectNumHashMap.keySet()){
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer num=objectNumHashMap.get(objectName);
                                        String code=objectCodeMap.get(objectName);
//                                        System.out.println("code"+code);
                                        if (code==null){
                                            if (!wrongName.contains(objectName)){
                                                wrongName.add(objectName);
                                            }
                                        }else {
                                            if (!rightName.contains(objectName)){
                                                rightName.add(objectName);
                                            }
                                        }
//                                        if (wrongName.size()+rightName.size()==20){
//                                            for (String name:wrongName){
//                                                System.out.println("wrongName"+name);
//                                            }
////                                            return true;
//                                        }
                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
                                        rssOdSampleClassList.add(rssOdSampleClass);
                                    }

                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
//                                rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);


                                if (i%50==0){
                                    long endTime=System.currentTimeMillis();
                                    System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                    System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                }

                                //2.2数据文件复制到对应的目录下
                                File oldImageFile=new File(imagePath);
                                if (oldImageFile.exists()){
                                    rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                                }else{
                                    System.out.println(imagePath+"文件不存在");
                                }

                            }
                            for (String objectName:totalObjectNumMap.keySet()){
                                System.out.println(objectName+":"+totalObjectNumMap.get(objectName));
                            }

                        }
                    }
                    for (String name:wrongName){
                        System.out.println("wrongName"+name);
                    }
                    break;
                case "DLR3k_DLR-MVDA":

                    selfIdNameMap.put("pkw","car");selfIdNameMap.put("pkw_trail","car with trailer");
                    selfIdNameMap.put("truck","truck");selfIdNameMap.put("truck_trail","truck with trailer");
//                    selfIdNameMap.put("van_trail","Van_With_Trail");selfIdNameMap.put("cam","long truck");
                    selfIdNameMap.put("van_trail","van with trail");selfIdNameMap.put("cam","long truck");
                    selfIdNameMap.put("bus","bus");
                    selfIdNameMap.put("van","van");
                    classMap=RssExcelUtil.getClassMap(classMapPath, 9);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    System.out.println("objectCodeMap.size():"+objectCodeMap.size());
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File dlrDir=new File(path);
                    if (dlrDir.isDirectory()){
//                        File annDir=new File(path+File.separator+"Train/hbbxml");
                        File annDir=new File(path+File.separator+"Train/unit_obbxml");
                        if (annDir.isDirectory()){
                            String s[] = annDir.list();
                            System.out.println("s.length:"+s.length);
                            for (int i = 0; i < s.length; i++) {
                                RssOdSample rssOdSample=new RssOdSample();
//                                File labelFile = new File(path+File.separator+"Train/hbbxml"+ File.separator + s[i]);
                                File labelFile = new File(path+File.separator+"Train/unit_obbxml"+ File.separator + s[i]);
                                String fileId=s[i].substring(0,s[i].lastIndexOf("."));
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                Integer sampleWidth=Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight=Integer.parseInt(rssOdAnn.getSize().getHeight());
                                String wkt="POINT(115 30)";
                                String sampleArea="ST_GeomFromText('"+wkt+"', 4326)";
                                Timestamp sampleDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality="A";
//                                String sampleLabeler=rssOdAnn.getOwner().getName();
                                String sampleLabeler=null;
                                Timestamp annotationDate=Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox="HBB";
                                Integer trnValueTest=0;

                                String imagePath=path+File.separator+"Train/images"+ File.separator+ s[i].replace("xml","JPG");

                                String newImagePath="DLR3k_DLR-MVDA-v1/image/";
                                String minioImagePath=newImagePath+s[i].replace("xml","JPG");
//                                String newLabelPath="DLR3k_DLR-MVDA-v1/label_hbb/";
                                String newLabelPath="DLR3k_DLR-MVDA-v1/label_unit_hbb/";
                                String minioLablePath=newLabelPath+ s[i];
                                String imageType="Optical";
                                Integer imageChannels=3;
                                String imageResolution=null;
                                String instrument=null;

                                String createBy="admin";
                                Timestamp createTime=new Timestamp(System.currentTimeMillis());
                                String updateBy=null;
                                Timestamp updateTime=null;


                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);

                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
                                System.out.println("objectList.size():"+objectList.size());
                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                for (RssOdAnn.Object object:objectList){
                                    String objectNameId=object.getName();
                                    String objectName=selfIdNameMap.get(objectNameId);
                                    if (objectName==null){
                                        System.out.println("objectNameId不存在:"+objectNameId);
                                    }
//                                    System.out.println("objectName:"+objectName);
                                    if (objectNumHashMap.containsKey(objectName)){
                                        Integer num=objectNumHashMap.get(objectName);
                                        num++;
                                        objectNumHashMap.put(objectName,num);
                                    }else {
                                        objectNumHashMap.put(objectName,1);
                                    }
                                }
//                                for (String objectName:objectNumHashMap.keySet()){
//                                    if (totalObjectNumMap.containsKey(objectName)){
//                                        Integer num=totalObjectNumMap.get(objectName);
//                                        num=num+objectNumHashMap.get(objectName);
//                                        totalObjectNumMap.put(objectName,num);
//                                    }else {
//                                        Integer num=totalObjectNumMap.get(objectName);
//                                        totalObjectNumMap.put(objectName,num);
//                                    }
//                                }

                                if (objectList!=null){
                                    for (String objectName:objectNumHashMap.keySet()){
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer num=objectNumHashMap.get(objectName);
                                        String code=objectCodeMap.get(objectName);
//                                    System.out.println("code"+code);
                                        if (code==null){
                                            if (!wrongName.contains(objectName)){
                                                wrongName.add(objectName);
                                            }
                                        }else {
                                            if (!rightName.contains(objectName)){
                                                rightName.add(objectName);
                                            }
                                        }
                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
                                        rssOdSampleClassList.add(rssOdSampleClass);
                                    }

                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }
//                                System.out.println("rssOdSampleClassList.size():"+rssOdSampleClassList.size());
                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);



                                long endTime=System.currentTimeMillis();
                                System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile=new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                            }
                            for (String objectName:totalObjectNumMap.keySet()){
                                System.out.println(objectName+":"+totalObjectNumMap.get(objectName));
                            }
                        }
                    }
                    for (String name:wrongName){
                        System.out.println("wrongName"+name);
                    }
                    break;
                case "LEVIR":
                    selfIdNameMap.put("1", "airplane");
                    selfIdNameMap.put("2", "ship");
                    selfIdNameMap.put("3", "oilpot");
                    classMap = RssExcelUtil.getClassMap(classMapPath, 11);
                    for (String className : classMap.keySet()) {
                        System.out.println(className + ":" + classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap = getObjectCodeMap(classMap);
                    File LEVIRDir = new File(path);
                    if (LEVIRDir.isDirectory()) {
                        File annDir = new File(path + File.separator + "hbbxml");
                        if (annDir.isDirectory()) {
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path + File.separator + "hbbxml" + File.separator + s[i]);
                                RssOdAnn LEVIRAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdSample rssOdSample = new RssOdSample();

//                                datasetId=0;
                                Integer sampleWidth = Integer.parseInt(LEVIRAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(LEVIRAnn.getSize().getHeight());
                                String wkt = "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path + File.separator + "imageWithLabel" + File.separator + s[i].replace("xml", "jpg");
                                String newImagePath = "LEVIR-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "jpg");
                                String newLabelPath = "LEVIR-v1/label_hbb/";
                                String minioLablePath = newLabelPath + s[i];

                                String imageType = "Optical";
                                Integer imageChannels = 3;
                                String imageResolution = "0.2~1";
                                String instrument = null;
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);
                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList = LEVIRAnn.getObject();
                                HashMap<String, Integer> objectNumHashMap = new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                if (objectList != null&&objectList.size()!=0) {
                                    for (RssOdAnn.Object object : objectList) {
//                                        String objectName = object.getName();
                                        String objectNameId=object.getName();
                                        String objectName=selfIdNameMap.get(objectNameId);
                                        if (objectNumHashMap.containsKey(objectName)) {
                                            Integer num = objectNumHashMap.get(objectName);
                                            num++;
                                            objectNumHashMap.put(objectName, num);
                                        } else {
                                            objectNumHashMap.put(objectName, 1);
                                        }
                                    }
                                    for (String objectName : objectNumHashMap.keySet()) {
//                                        System.out.println("self class:"+objectName);
                                        RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                        Integer num = objectNumHashMap.get(objectName);
                                        //获得目标类别编码


                                        String code = objectCodeMap.get(objectName);

                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                        rssOdSampleClassList.add(rssOdSampleClass);

                                    }
                                } else {
//                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(f, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
                                rssMinioUtil.uploadFile(oldImageFile, "object-detection", minioImagePath);
                            }
                        }
                    }
                    break;
                case "HRSID":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 10);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File hrsiddsDir=new File(path);
                    if (hrsiddsDir.isDirectory()){
                        File annDir=new File(path+File.separator+"SAR-Ship"+File.separator+"ship_detection_online"+File.separator+"Annotations_new");
                        if (annDir.isDirectory()){
                            String s[]= annDir.list();
                            for (int i=0;i<s.length;i++){
                                RssOdSample rssOdSample=new RssOdSample();
                                File labelFile=new File(path+File.separator+"SAR-Ship"+File.separator+"ship_detection_online"+File.separator+"Annotations_new"+File.separator+s[i]);
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                Integer sampleWidth = Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(rssOdAnn.getSize().getHeight());
                                String wkt = "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path+File.separator+"SAR-Ship"+File.separator+"ship_detection_online"+File.separator+"JPEGImages"+File.separator+s[i].replace("xml", "jpg");
                                String newImagePath = "hrsid-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "jpg");
                                String newLabelPath = "hrsid-v1/label_hbb/";
                                String minioLablePath = newLabelPath + s[i];
                                String imageType = "Sar";
                                Integer imageChannels = 3;
                                String imageResolution = "0.5~3";
                                String instrument = null;
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);

                                List<RssOdAnn.Object> objectList = rssOdAnn.getObject();
//                                for (int j = 0; j < objectList.size(); j++) {
//                                    System.out.println(objectList.get(j).getName());
//                                }


                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                for (RssOdAnn.Object object:objectList){
                                    String objectName=object.getName();
                                    if (objectNumHashMap.containsKey(objectName)){
                                        Integer number=objectNumHashMap.get(objectName);
                                        number++;
                                        objectNumHashMap.put(objectName,number);
                                    }else {
                                        objectNumHashMap.put(objectName,1);
                                    }
                                }
                                if (objectList!=null){
                                    for (String objectName:objectNumHashMap.keySet()){
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer number=objectNumHashMap.get(objectName);
                                        String code=objectCodeMap.get(objectName);

                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
//                                        rssOdSampleClass.setObjectNum(num);
//                                        System.out.println(objectName+":"+number);
                                        rssOdSampleClass.setObjectNum(number);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
                                        rssOdSampleClassList.add(rssOdSampleClass);
                                    }

                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }
                                //2.1标签文件复制到对应的目录下
//                                rssMinioUtil.uploadFile(labelFile, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }

                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                            }
                        }
                    }
                    break;
                case "ITCVD":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 12);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File itcvdDir=new File(path);
                    if (itcvdDir.isDirectory()){
                        String[] trnTests=itcvdDir.list();
                        for (String trnTest:trnTests){
                            Integer trnValueTest=0;
                            if (trnTest.equals("Testing")){
                                trnValueTest=2;
                            }else if (trnTest.equals("Training")){
                                trnValueTest=0;
                            }
                            File annDir=new File(path+File.separator+trnTest+File.separator+"hbbxml");
                            if (annDir.isDirectory()){
                                String s[]= annDir.list();
                                for (int i=0;i<s.length;i++){
                                    RssOdSample rssOdSample=new RssOdSample();
                                    File labelFile=new File(path+File.separator+trnTest+File.separator+"hbbxml"+File.separator+s[i]);
                                    RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                    Integer sampleWidth = Integer.parseInt(rssOdAnn.getSize().getWidth());
                                    Integer sampleHeight = Integer.parseInt(rssOdAnn.getSize().getHeight());
                                    String wkt = "POINT(115 30)";
                                    String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                    Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                    String sampleQuality = "A";
                                    String sampleLabeler = null;
                                    Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                    String lableBbox = "HBB";
                                    String imagePath = path+File.separator+trnTest+File.separator+"Image"+File.separator+s[i].replace("xml", "jpg");
                                    String newImagePath = "ITCVD-v1/image/";
                                    String minioImagePath = newImagePath + s[i].replace("xml", "jpg");
                                    String newLabelPath = "ITCVD-v1/label_hbb/";
                                    String minioLablePath = newLabelPath + s[i];
                                    String imageType = "Optical";
                                    Integer imageChannels = 3;
                                    String imageResolution = "0.1";
                                    String instrument = null;
                                    String createBy = "admin";
                                    Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                    String updateBy = null;
                                    Timestamp updateTime = null;

                                    maxOdSampleId++;
                                    rssOdSample.setId(maxOdSampleId);
                                    rssOdSample.setDatasetId(datasetId);
                                    rssOdSample.setSampleWidth(sampleWidth);
                                    rssOdSample.setSampleHeight(sampleHeight);
                                    rssOdSample.setSampleArea(sampleArea);
                                    rssOdSample.setSampleDate(sampleDate);
                                    rssOdSample.setSampleQuality(sampleQuality);
                                    rssOdSample.setSampleLabeler(sampleLabeler);
                                    rssOdSample.setAnnotationDate(annotationDate);
                                    rssOdSample.setLabelBbox(lableBbox);
                                    rssOdSample.setImagePath(minioImagePath);
                                    rssOdSample.setLabelPath(minioLablePath);
                                    rssOdSample.setImageType(imageType);
                                    rssOdSample.setImageChannels(imageChannels);
                                    rssOdSample.setImageResolution(imageResolution);
                                    rssOdSample.setInstrument(instrument);
                                    rssOdSample.setTrnValueTest(trnValueTest);
                                    rssOdSample.setCreateBy(createBy);
                                    rssOdSample.setCreateTime(createTime);
                                    rssOdSample.setUpdateBy(updateBy);
                                    rssOdSample.setUpdateTime(updateTime);
                                    rssOdSampleList.add(rssOdSample);

                                    List<RssOdAnn.Object> objectList = rssOdAnn.getObject();
//                                for (int j = 0; j < objectList.size(); j++) {
//                                    System.out.println(objectList.get(j).getName());
//                                }
//                                    Integer num = objectList.size();

                                    HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                    for (RssOdAnn.Object object:objectList){
                                        String objectName=object.getName();
                                        if (objectNumHashMap.containsKey(objectName)){
                                            Integer number=objectNumHashMap.get(objectName);
                                            number++;
                                            objectNumHashMap.put(objectName,number);
                                        }else {
                                            objectNumHashMap.put(objectName,1);
                                        }
                                    }
                                    if (objectList!=null){
                                        for (String objectName:objectNumHashMap.keySet()){
                                            RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                            Integer number=objectNumHashMap.get(objectName);
                                            String code=objectCodeMap.get(objectName);

                                            maxOdSampleClassId++;
                                            rssOdSampleClass.setId(maxOdSampleClassId);
                                            rssOdSampleClass.setClassId(code);
                                            rssOdSampleClass.setSampleId(maxOdSampleId);
//                                            System.out.println(objectName+":"+number);
                                            rssOdSampleClass.setObjectNum(number);
                                            rssOdSampleClass.setCreateBy("admin");
                                            rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                            rssOdSampleClass.setUpdateBy(null);
                                            rssOdSampleClass.setUpdateTime(null);
                                            rssOdSampleClassList.add(rssOdSampleClass);
                                        }

                                    }else {
                                        System.out.println("该样本没有目标！！！");
                                    }
                                    //2.1标签文件复制到对应的目录下
                                    rssMinioUtil.uploadFile(labelFile, "object-detection", minioLablePath);
                                    if (i % 50 == 0) {
                                        long endTime = System.currentTimeMillis();
                                        System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                        System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                    }

                                    //2.2数据文件复制到对应的目录下
                                    File oldImageFile = new File(imagePath);
                                    rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                                }
                            }
                        }
                    }
                    break;
                case "AIR-SARShip":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 13);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File airsarshipdsDir=new File(path);
                    if (airsarshipdsDir.isDirectory()){
//                        File annDir=new File(path+File.separator+"hbbxml");
                        File annDir=new File(path+File.separator+"unit_hbbxml");
                        if (annDir.isDirectory()){
                            String s[]= annDir.list();
                            for (int i=0;i<s.length;i++){
                                RssOdSample rssOdSample=new RssOdSample();
//                                File labelFile=new File(path+File.separator+"hbbxml"+File.separator+s[i]);
                                File labelFile=new File(path+File.separator+"unit_hbbxml"+File.separator+s[i]);
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                Integer sampleWidth = Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(rssOdAnn.getSize().getHeight());
                                String wkt = "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path + File.separator + "AIR-SARShip-2.0-data" + File.separator + "AIR-SARShip-2.0-data" + File.separator + s[i].replace("xml", "tiff");
                                String newImagePath = "AIR-SARShip-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "tiff");
//                                String newLabelPath = "AIR-SARShip-v1/label_hbb/";
                                String newLabelPath = "AIR-SARShip-v1/label_unit_hbb/";
                                String minioLablePath = newLabelPath + s[i];
                                String imageType = "Sar";
                                Integer imageChannels = 3;
                                String imageResolution = null;
                                String instrument = null;
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);

                                List<RssOdAnn.Object> objectList = rssOdAnn.getObject();
//                                for (int j = 0; j < objectList.size(); j++) {
//                                    System.out.println(objectList.get(j).getName());
//                                }
                                Integer num = objectList.size();
                                String objectName=objectList.get(0).getName();


                                //获得目标类别编码
                                String code = objectCodeMap.get(objectName);
                                RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                maxOdSampleClassId++;
                                rssOdSampleClass.setId(maxOdSampleClassId);
                                rssOdSampleClass.setClassId(code);
                                rssOdSampleClass.setSampleId(maxOdSampleId);
                                rssOdSampleClass.setObjectNum(num);
//                                System.out.println("Num:" + num);
                                rssOdSampleClass.setCreateBy("admin");
                                rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                rssOdSampleClass.setUpdateBy(null);
                                rssOdSampleClass.setUpdateTime(null);
                                rssOdSampleClassList.add(rssOdSampleClass);

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(labelFile, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }

                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                            }
                        }
                    }
                    break;
                case "TAS":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 14);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    File tasdsDir=new File(path);
                    if (tasdsDir.isDirectory()){
                        File annDir=new File(path+File.separator+"hbbxml");
                        if (annDir.isDirectory()){
                            String s[]= annDir.list();
                            for (int i=0;i<s.length;i++){
                                RssOdSample rssOdSample=new RssOdSample();
                                File labelFile=new File(path+File.separator+"hbbxml"+File.separator+s[i]);
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);

                                Integer sampleWidth = Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(rssOdAnn.getSize().getHeight());
                                String wkt = "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path + File.separator + "example" + File.separator + "Data" + File.separator +"Images"+File.separator +s[i].replace("xml", "jpg");
                                String newImagePath = "TAS-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "jpg");
                                String newLabelPath = "TAS-v1/label_hbb/";
                                String minioLablePath = newLabelPath + s[i];
                                String imageType = "Sar";
                                Integer imageChannels = 3;
                                String imageResolution = null;
                                String instrument = null;
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);

                                List<RssOdAnn.Object> objectList = rssOdAnn.getObject();
//                                for (int j = 0; j < objectList.size(); j++) {
//                                    System.out.println(objectList.get(j).getName());
//                                }
                                Integer num = objectList.size();
                                String objectName=objectList.get(0).getName();


                                //获得目标类别编码
                                String code = objectCodeMap.get(objectName);
                                RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                maxOdSampleClassId++;
                                rssOdSampleClass.setId(maxOdSampleClassId);
                                rssOdSampleClass.setClassId(code);
                                rssOdSampleClass.setSampleId(maxOdSampleId);
                                rssOdSampleClass.setObjectNum(num);
//                                System.out.println("Num:" + num);
                                rssOdSampleClass.setCreateBy("admin");
                                rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                rssOdSampleClass.setUpdateBy(null);
                                rssOdSampleClass.setUpdateTime(null);
                                rssOdSampleClassList.add(rssOdSampleClass);

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(labelFile, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }

                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);
                            }
                        }
                    }
                    break;
                case "VEDAI":
                    selfIdNameMap.put("1", "Car");
                    selfIdNameMap.put("2", "Truck");
                    selfIdNameMap.put("23", "Ship");
                    selfIdNameMap.put("4", "Tractor");
                    selfIdNameMap.put("5", "Camping Car");
                    selfIdNameMap.put("9", "van");
                    selfIdNameMap.put("10", "vehicle");
                    selfIdNameMap.put("11", "pick-up");
                    selfIdNameMap.put("31", "plane");
                    selfIdNameMap.put("7", "Small Land Vehicle");
                    selfIdNameMap.put("8", "Large Land Vehicle");
                    classMap = RssExcelUtil.getClassMap(classMapPath, 15);
                    for (String className : classMap.keySet()) {
                        System.out.println(className + ":" + classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap = getObjectCodeMap(classMap);
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File VEDAIDir = new File(path);
                    if (VEDAIDir.isDirectory()) {
//                        File annDir512 = new File(path + File.separator + "obbxml512new");
//                        File annDir1024 = new File(path + File.separator + "obbxml1024new");
                        File annDir512 = new File(path + File.separator + "unit512_obbxml");
                        File annDir1024 = new File(path + File.separator + "unit1024_obbxml");
                        if (annDir512.isDirectory()) {
                            String s[] = annDir512.list();
                            for (int i = 0; i < s.length; i++) {
//                                File f = new File(path + File.separator + "obbxml512new" + File.separator + s[i]);
                                File f = new File(path + File.separator + "unit512_obbxml" + File.separator + s[i]);
                                RssOdObbAnn VEDAI512Ann = rssFileUtil.parseObbBoxXML(f);
                                RssOdSample rssOdSample = new RssOdSample();

//                                datasetId=0;
                                Integer sampleWidth = Integer.parseInt(VEDAI512Ann.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(VEDAI512Ann.getSize().getHeight());
                                String wkt = "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "OBB";
                                String imagePath = path + File.separator + "Vehicules512" + File.separator + s[i].replace("xml", "png");
                                String newImagePath = "VEDAI-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "png");
//                                String newLabelPath = "VEDAI-v1/label_obb/";
                                String newLabelPath = "VEDAI-v1/label_unit_obb/";
                                String minioLablePath = newLabelPath + s[i];

                                String imageType = "Optical";
                                Integer imageChannels = 4;
                                String imageResolution = "0.125";
                                String instrument = "Utah AGRC";
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);
                                //获取目标类别，判断是否重复
                                List<RssOdObbAnn.Object> objectList = VEDAI512Ann.getObject();
                                HashMap<String, Integer> objectNumHashMap = new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                if (objectList != null) {
                                    for (RssOdObbAnn.Object object : objectList) {
//                                        String objectName = object.getName();
                                        String objectNameId=object.getName();
                                        String objectName=selfIdNameMap.get(objectNameId);
                                        if (objectName==null||objectName.equals("null")){
                                            System.out.println("objectNameId:"+objectNameId);
                                        }
                                        if (objectNumHashMap.containsKey(objectName)) {
                                            Integer num = objectNumHashMap.get(objectName);
                                            num++;
                                            objectNumHashMap.put(objectName, num);
                                        } else {
                                            objectNumHashMap.put(objectName, 1);
                                        }
                                    }
                                    for (String objectName : objectNumHashMap.keySet()) {
//                                        System.out.println("self class:"+objectName);
                                        RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                        Integer num = objectNumHashMap.get(objectName);
                                        //获得目标类别编码


                                        String code = objectCodeMap.get(objectName);
//                                        System.out.println("code:" + code);
                                        if (code==null){
                                            if (!wrongName.contains(objectName)){
                                                wrongName.add(objectName);
                                            }
                                            System.out.println("imagePath:"+imagePath);
                                        }else {
                                            if (!rightName.contains(objectName)){
                                                rightName.add(objectName);
                                            }
                                        }
                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);

//                                        System.out.println("Num:" + num);

                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                        rssOdSampleClassList.add(rssOdSampleClass);

                                    }
                                } else {
                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(f, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile, "object-detection", minioImagePath);
                            }
                        }
                        if (annDir1024.isDirectory()) {
                            String s[] = annDir1024.list();
                            for (int i = 0; i < s.length; i++) {
//                                File f = new File(path + File.separator + "obbxml1024new" + File.separator + s[i]);
                                File f = new File(path + File.separator + "unit1024_obbxml" + File.separator + s[i]);
                                RssOdObbAnn VEDAI1024Ann = rssFileUtil.parseObbBoxXML(f);
                                RssOdSample rssOdSample = new RssOdSample();

//                                datasetId=0;
                                Integer sampleWidth = Integer.parseInt(VEDAI1024Ann.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(VEDAI1024Ann.getSize().getHeight());
                                String wkt = "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "OBB";
                                String imagePath = path + File.separator + "Vehicules1024" + File.separator + s[i].replace("xml", "png");
                                String newImagePath = "VEDAI-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "png");
//                                String newLabelPath = "VEDAI-v1/label_hbb/";
                                String newLabelPath = "VEDAI-v1/label_unit_obb/";
                                String minioLablePath = newLabelPath + s[i];

                                String imageType = "Optical";
                                Integer imageChannels = 4;
                                String imageResolution = "0.125";
                                String instrument = "Utah AGRC";
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);
                                //获取目标类别，判断是否重复
                                List<RssOdObbAnn.Object> objectList = VEDAI1024Ann.getObject();
                                HashMap<String, Integer> objectNumHashMap = new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                if (objectList != null) {
                                    for (RssOdObbAnn.Object object : objectList) {
//                                        String objectName = object.getName();
                                        String objectNameId=object.getName();
                                        String objectName=selfIdNameMap.get(objectNameId);
                                        if (objectName==null||objectName.equals("null")){
                                            System.out.println("objectNameId:"+objectNameId);
                                        }
                                        if (objectNumHashMap.containsKey(objectName)) {
                                            Integer num = objectNumHashMap.get(objectName);
                                            num++;
                                            objectNumHashMap.put(objectName, num);
                                        } else {
                                            objectNumHashMap.put(objectName, 1);
                                        }
                                    }
                                    for (String objectName : objectNumHashMap.keySet()) {
//                                        System.out.println("self class:"+objectName);
                                        RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                        Integer num = objectNumHashMap.get(objectName);
                                        //获得目标类别编码


                                        String code = objectCodeMap.get(objectName);
                                        if (code==null){
                                            if (!wrongName.contains(objectName)){
                                                wrongName.add(objectName);
                                            }
                                            System.out.println("imagePath:"+imagePath);
                                        }else {
                                            if (!rightName.contains(objectName)){
                                                rightName.add(objectName);
                                            }
                                        }
//                                        System.out.println("code:" + code);
                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);

//                                        System.out.println("Num:" + num);

                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                        rssOdSampleClassList.add(rssOdSampleClass);

                                    }
                                } else {
                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(f, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile, "object-detection", minioImagePath);
                            }
                        }
                    }
                    for (String name:wrongName){
                        System.out.println("wrongName"+name);
                    }
                    break;
                case "DIUx_xView":
//                    classMap=RssExcelUtil.getClassMap(classMapPath, 16);
                    classMap=RssExcelUtil.getClassMap(classMapPath, 19);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    for (String className:objectCodeMap.keySet()){
                        System.out.println(className+":"+objectCodeMap.get(className));
                    }
                    File xViewDir=new File(path);
                    if (xViewDir.isDirectory()){
                        File annDir=new File(path+File.separator+"hbbxml");
                        if (annDir.isDirectory()){
                            String s[]= annDir.list();
                            for (int i=0;i<s.length;i++){
                                RssOdSample rssOdSample=new RssOdSample();
                                File labelFile=new File(path+File.separator+"hbbxml"+File.separator+s[i]);
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                Integer sampleWidth = Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(rssOdAnn.getSize().getHeight());

                                Double minLongitude=rssOdAnn.getMinLongitude();
                                Double minLatitude=rssOdAnn.getMinLatitude();
                                Double maxLongitude=rssOdAnn.getMaxLongitude();
                                Double maxLatitude=rssOdAnn.getMaxLatitude();

                                String wkt = rssGsUtil.DoubleToWKT(minLongitude,minLatitude,maxLongitude,maxLatitude);
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path+File.separator+"train_images"+File.separator+"train_images"+File.separator+s[i].replace("xml", "tif");
                                String newImagePath = "DIUx_xView-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "tif");
                                String newLabelPath = "DIUx_xView-v1/label_hbb/";
                                String minioLablePath = newLabelPath + s[i];
                                String imageType = "Optical";
                                Integer imageChannels = 3;
                                String imageResolution = "0.3";
                                String instrument = "WorldView 3";
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;
                                Integer trnValueTest=0;
                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);


                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
//                                System.out.println("objectList.size():"+objectList.size());
                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                for (RssOdAnn.Object object:objectList){
                                    String objectName=object.getName();
                                    if (objectName==null||objectName.equals("null")){
                                        System.out.println("objectName:"+objectName);
                                    }
                                    if (objectNumHashMap.containsKey(objectName)){
                                        Integer num=objectNumHashMap.get(objectName);
                                        num++;
                                        objectNumHashMap.put(objectName,num);
                                    }else {
                                        objectNumHashMap.put(objectName,1);
                                    }
                                }
                                if (objectList!=null){
                                    for (String objectName:objectNumHashMap.keySet()){
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer num=objectNumHashMap.get(objectName);
                                        String code=objectCodeMap.get(objectName);

                                        if (code==null){
                                            if (!wrongName.contains(objectName)){
                                                wrongName.add(objectName);
                                            }
                                            System.out.println("imagePath:"+imagePath);
                                        }else {
                                            if (!rightName.contains(objectName)){
                                                rightName.add(objectName);
                                            }
                                        }
                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
                                        rssOdSampleClassList.add(rssOdSampleClass);
                                    }

                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }
//                                System.out.println("rssOdSampleClassList.size():"+rssOdSampleClassList.size());
                                //2.1标签文件复制到对应的目录下
//                                rssMinioUtil.uploadFile(labelFile,"object-detection",minioLablePath);


                                if (i%50==0){
                                    long endTime=System.currentTimeMillis();
                                    System.out.print("上传"+((float)i/s.length)*100+"%完成");
                                    System.out.println("上传"+i+"耗时："+(endTime-startTime)/1000+"秒");
                                }

                                //2.2数据文件复制到对应的目录下
                                File oldImageFile=new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile,"object-detection",minioImagePath);

                            }
                        }
                    }
                    for (String name:wrongName){
                        System.out.println("wrongName"+name);
                    }
                    break;
                case "OIRDS":
                    classMap = RssExcelUtil.getClassMap(classMapPath, 17);
                    for (String className : classMap.keySet()) {
                        System.out.println(className + ":" + classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap = getObjectCodeMap(classMap);
                    File OIRDSDir = new File(path);
                    if (OIRDSDir.isDirectory()) {
                        for (int i = 1; i <= 20; i++) {
                            File annDir = new File(path + File.separator + "DataSet_" + i + File.separator + "polxml");
                            if (annDir.isDirectory()) {
                                String s[] = annDir.list();
                                for (int j = 0; j < s.length; j++) {
                                    File f = new File(path + File.separator + "DataSet_" + i + File.separator + "polxml" + File.separator + s[j]);
                                    RssOdPolAnn OIRDSAnn = rssFileUtil.parsePolBoxXML(f);
                                    RssOdSample rssOdSample = new RssOdSample();

//                                datasetId=0;
                                    Integer sampleWidth = Integer.parseInt(OIRDSAnn.getSize().getWidth());
                                    Integer sampleHeight = Integer.parseInt(OIRDSAnn.getSize().getHeight());
                                    String wkt = "POINT(115 30)";
                                    String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                    Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                    String sampleQuality = "A";
                                    String sampleLabeler = null;
                                    Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                    String lableBbox = "POL";
                                    String imagePath = path + File.separator + "DataSet_" + i + File.separator + s[j].replace("xml", "tif");
                                    String newImagePath = "OIRDS-v1/image/";
                                    String minioImagePath = newImagePath + s[j].replace("xml", "tif");
                                    String newLabelPath = "OIRDS-v1/label_pol/";
                                    String minioLablePath = newLabelPath + s[j];

                                    String imageType = "Optical";
                                    Integer imageChannels = 3;
                                    String imageResolution = "0.2~30";
                                    String instrument = null;
                                    Integer trnValueTest = 0;
                                    String createBy = "admin";
                                    Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                    String updateBy = null;
                                    Timestamp updateTime = null;

                                    maxOdSampleId++;
                                    rssOdSample.setId(maxOdSampleId);
                                    rssOdSample.setDatasetId(datasetId);
                                    rssOdSample.setSampleWidth(sampleWidth);
                                    rssOdSample.setSampleHeight(sampleHeight);
                                    rssOdSample.setSampleArea(sampleArea);
                                    rssOdSample.setSampleDate(sampleDate);
                                    rssOdSample.setSampleQuality(sampleQuality);
                                    rssOdSample.setSampleLabeler(sampleLabeler);
                                    rssOdSample.setAnnotationDate(annotationDate);
                                    rssOdSample.setLabelBbox(lableBbox);
                                    rssOdSample.setImagePath(minioImagePath);
                                    rssOdSample.setLabelPath(minioLablePath);
                                    rssOdSample.setImageType(imageType);
                                    rssOdSample.setImageChannels(imageChannels);
                                    rssOdSample.setImageResolution(imageResolution);
                                    rssOdSample.setInstrument(instrument);
                                    rssOdSample.setTrnValueTest(trnValueTest);
                                    rssOdSample.setCreateBy(createBy);
                                    rssOdSample.setCreateTime(createTime);
                                    rssOdSample.setUpdateBy(updateBy);
                                    rssOdSample.setUpdateTime(updateTime);
                                    rssOdSampleList.add(rssOdSample);
                                    //获取目标类别，判断是否重复
                                    List<RssOdPolAnn.Object> objectList = OIRDSAnn.getObject();
                                    HashMap<String, Integer> objectNumHashMap = new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                    if (objectList != null) {
                                        for (RssOdPolAnn.Object object : objectList) {
                                            String objectName = object.getName();
                                            if (objectNumHashMap.containsKey(objectName)) {
                                                Integer num = objectNumHashMap.get(objectName);
                                                num++;
                                                objectNumHashMap.put(objectName, num);
                                            } else {
                                                objectNumHashMap.put(objectName, 1);
                                            }
                                        }
                                        for (String objectName : objectNumHashMap.keySet()) {
//                                        System.out.println("self class:"+objectName);
                                            RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                            Integer num = objectNumHashMap.get(objectName);
                                            //获得目标类别编码


                                            String code = objectCodeMap.get(objectName);
//                                            System.out.println("code:"+code);
                                            maxOdSampleClassId++;
                                            rssOdSampleClass.setId(maxOdSampleClassId);
                                            rssOdSampleClass.setClassId(code);
                                            rssOdSampleClass.setSampleId(maxOdSampleId);
                                            rssOdSampleClass.setObjectNum(num);
                                            rssOdSampleClass.setCreateBy("admin");
                                            rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                            rssOdSampleClass.setUpdateBy(null);
                                            rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                            rssOdSampleClassList.add(rssOdSampleClass);

                                        }
                                    } else {
                                        System.out.println("该样本没有目标！！！");
                                    }

                                    //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(f, "object-detection", minioLablePath);
                                    if (i % 50 == 0) {
                                        long endTime = System.currentTimeMillis();
                                        System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                        System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                    }
                                    //2.2数据文件复制到对应的目录下
                                    File oldImageFile = new File(imagePath);
                                rssMinioUtil.uploadFile(oldImageFile, "object-detection", minioImagePath);
                                }
                            }

                        }
                    }
                    break;
                case "COWC":
                    classMap=RssExcelUtil.getClassMap(classMapPath, 18);
                    for (String className:classMap.keySet()){
                        System.out.println(className+":"+classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap=getObjectCodeMap(classMap);
                    for (String className:objectCodeMap.keySet()){
                        System.out.println(className+":"+objectCodeMap.get(className));
                    }
                    File COWCDir=new File(path);
                    if (COWCDir.isDirectory()){
                        File annDir=new File(path+File.separator+"hbbxml");
                        if (annDir.isDirectory()){
                            String s[]= annDir.list();
                            for (int i=0;i<s.length;i++){
                                RssOdSample rssOdSample=new RssOdSample();
                                File labelFile=new File(path+File.separator+"hbbxml"+File.separator+s[i]);
                                RssOdAnn rssOdAnn=rssFileUtil.parseDOTAAXML(labelFile);
                                Integer sampleWidth = Integer.parseInt(rssOdAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(rssOdAnn.getSize().getHeight());

                                String wkt =  "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path+File.separator+"images"+File.separator+s[i].replace("xml", "jpg");
                                String newImagePath = "COWC-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "jpg");
                                String newLabelPath = "COWC-v1/label_hbb/";
                                String minioLablePath = newLabelPath + s[i];
                                String imageType = "Optical";
                                Integer imageChannels = 3;
                                String imageResolution = "0.15";
                                String instrument = "Utah";
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;
                                Integer trnValueTest=0;


                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);
                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList=rssOdAnn.getObject();
//                                System.out.println("objectList.size():"+objectList.size());
                                HashMap<String,Integer> objectNumHashMap=new HashMap<>();
                                for (RssOdAnn.Object object:objectList){
                                    String objectName=object.getName();
                                    if (objectNumHashMap.containsKey(objectName)){
                                        Integer num=objectNumHashMap.get(objectName);
                                        num++;
                                        objectNumHashMap.put(objectName,num);
                                    }else {
                                        objectNumHashMap.put(objectName,1);
                                    }
                                }
                                if (objectList!=null){
                                    for (String objectName:objectNumHashMap.keySet()){
                                        RssOdSampleClass rssOdSampleClass=new RssOdSampleClass();
                                        Integer num=objectNumHashMap.get(objectName);
                                        String code=objectCodeMap.get(objectName);

//                                        System.out.println("code:"+code);

                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
                                        rssOdSampleClassList.add(rssOdSampleClass);
                                    }

                                }else {
                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(labelFile, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
//                                rssMinioUtil.uploadFile(oldImageFile, "object-detection", minioImagePath);
                            }
                        }
                    }
                    break;
                case "SAR-Ship-Dataset":
                    classMap = RssExcelUtil.getClassMap(classMapPath, 21);
                    for (String className : classMap.keySet()) {
                        System.out.println(className + ":" + classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap = getObjectCodeMap(classMap);
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File SARShipDir = new File(path);
                    if (SARShipDir.isDirectory()) {
                        File annDir = new File(path + File.separator + "hbbxml");
                        if (annDir.isDirectory()) {
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path + File.separator + "hbbxml" + File.separator + s[i]);
                                RssOdAnn SARShipAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdSample rssOdSample = new RssOdSample();

//                                datasetId=0;
                                Integer sampleWidth = Integer.parseInt(SARShipAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(SARShipAnn.getSize().getHeight());
                                String wkt = "POINT(115 30)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path + File.separator + "images" + File.separator + s[i].replace("xml", "jpg");
                                String newImagePath = "SAR-Ship-Dataset-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "jpg");
                                String newLabelPath = "SAR-Ship-Dataset-v1/label_hbb/";
                                String minioLablePath = newLabelPath + s[i];

                                String imageType = "Optical";
                                Integer imageChannels = 3;
                                String imageResolution = "1.7~25";
                                String instrument = null;
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;

                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);
                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList = SARShipAnn.getObject();
                                HashMap<String, Integer> objectNumHashMap = new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                if (objectList != null) {
                                    for (RssOdAnn.Object object : objectList) {
                                        String objectName = object.getName();
//                                        String objectNameId = object.getName();
//                                        String objectName = selfIdNameMap.get(objectNameId);
                                        if (objectNumHashMap.containsKey(objectName)) {
                                            Integer num = objectNumHashMap.get(objectName);
                                            num++;
                                            objectNumHashMap.put(objectName, num);
                                        } else {
                                            objectNumHashMap.put(objectName, 1);
                                        }
                                    }
                                    for (String objectName:objectNumHashMap.keySet()){
                                        if (totalObjectNumMap.containsKey(objectName)){
                                            Integer num=totalObjectNumMap.get(objectName);
                                            num++;
                                            totalObjectNumMap.put(objectName,num);
                                        }else {
                                            totalObjectNumMap.put(objectName,1);
                                        }
                                    }
                                    for (String objectName : objectNumHashMap.keySet()) {
//                                        System.out.println("self class:"+objectName);
                                        RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                        Integer num = objectNumHashMap.get(objectName);
                                        //获得目标类别编码


                                        String code = objectCodeMap.get(objectName);
                                        if (code==null){
                                            if (!wrongName.contains(objectName)){
                                                wrongName.add(objectName);
                                            }
                                        }else {
                                            if (!rightName.contains(objectName)){
                                                rightName.add(objectName);
                                            }
                                        }
                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                        rssOdSampleClassList.add(rssOdSampleClass);

                                    }
                                } else {
//                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(f, "object-detection", minioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
                                rssMinioUtil.uploadFile(oldImageFile, "object-detection", minioImagePath);
                            }
                            for (String objectName:totalObjectNumMap.keySet()){
                                System.out.println(objectName+":"+totalObjectNumMap.get(objectName));
                            }
                        }
                    }
                    for (String name:wrongName){
                        System.out.println("wrongName"+name);
                    }
                    break;
                case "SOUTHGIS Remote Sensing Building Detection DataSet":
                    classMap = RssExcelUtil.getClassMap(classMapPath, 24);
                    for (String className : classMap.keySet()) {
                        System.out.println(className + ":" + classMap.get(className));
                    }
                    //根据统一的类别名获得对应的编码
                    objectCodeMap = getObjectCodeMap(classMap);
                    for (String className : objectCodeMap.keySet()) {
                        System.out.println(className + ":" + objectCodeMap.get(className));
                    }
                    File southgisDir = new File(path);
                    if (southgisDir.isDirectory()) {
                        File annDir = new File(path + File.separator + "annotations");
                        if (annDir.isDirectory()) {
                            String s[] = annDir.list();
                            for (int i = 0; i < s.length; i++) {
                                File f = new File(path + File.separator + "annotations" + File.separator + s[i]);
                                RssOdAnn southgisAnn = rssFileUtil.parseDOTAAXML(f);
                                RssOdSample rssOdSample = new RssOdSample();

                                Integer sampleWidth = Integer.parseInt(southgisAnn.getSize().getWidth());
                                Integer sampleHeight = Integer.parseInt(southgisAnn.getSize().getHeight());
                                String wkt = "POINT(114.86 40.66)";
                                String sampleArea = "ST_GeomFromText('" + wkt + "', 4326)";
                                Timestamp sampleDate = Timestamp.valueOf("2019-01-01 00:00:00");
                                String sampleQuality = "A";
                                String sampleLabeler = null;
                                Timestamp annotationDate = Timestamp.valueOf("2021-09-01 00:00:00");
                                String lableBbox = "HBB";
                                String imagePath = path + File.separator + "img" + File.separator + s[i].replace("xml", "png");
                                String newImagePath = "SRSBDD-v1/image/";
                                String minioImagePath = newImagePath + s[i].replace("xml", "png");
                                String newLabelPath = "SRSBDD-v1/label_hbb/";
                                String unitnewLabelPath = "SRSBDD-v1/label_unit_hbb/";
                                String minioLablePath = newLabelPath + s[i];

                                String unitminioLablePath = unitnewLabelPath + s[i];

                                String imageType = "Optical";
                                Integer imageChannels = 3;
                                String imageResolution = "1";
                                String instrument = null;
                                Integer trnValueTest = 0;
                                String createBy = "admin";
                                Timestamp createTime = new Timestamp(System.currentTimeMillis());
                                String updateBy = null;
                                Timestamp updateTime = null;


                                maxOdSampleId++;
                                rssOdSample.setId(maxOdSampleId);
                                rssOdSample.setDatasetId(datasetId);
                                rssOdSample.setSampleWidth(sampleWidth);
                                rssOdSample.setSampleHeight(sampleHeight);
                                rssOdSample.setSampleArea(sampleArea);
                                rssOdSample.setSampleDate(sampleDate);
                                rssOdSample.setSampleQuality(sampleQuality);
                                rssOdSample.setSampleLabeler(sampleLabeler);
                                rssOdSample.setAnnotationDate(annotationDate);
                                rssOdSample.setLabelBbox(lableBbox);
                                rssOdSample.setImagePath(minioImagePath);
                                rssOdSample.setLabelPath(minioLablePath);
                                rssOdSample.setImageType(imageType);
                                rssOdSample.setImageChannels(imageChannels);
                                rssOdSample.setImageResolution(imageResolution);
                                rssOdSample.setInstrument(instrument);
                                rssOdSample.setTrnValueTest(trnValueTest);
                                rssOdSample.setCreateBy(createBy);
                                rssOdSample.setCreateTime(createTime);
                                rssOdSample.setUpdateBy(updateBy);
                                rssOdSample.setUpdateTime(updateTime);
                                rssOdSampleList.add(rssOdSample);


                                //获取目标类别，判断是否重复
                                List<RssOdAnn.Object> objectList = southgisAnn.getObject();
                                HashMap<String, Integer> objectNumHashMap = new HashMap<>();
//                                System.out.println("objectList.size():"+objectList.size());
                                if (objectList != null) {
                                    for (RssOdAnn.Object object : objectList) {
                                        String objectName = object.getName();
//                                        String objectNameId = object.getName();
//                                        String objectName = selfIdNameMap.get(objectNameId);
                                        if (objectNumHashMap.containsKey(objectName)) {
                                            Integer num = objectNumHashMap.get(objectName);
                                            num++;
                                            objectNumHashMap.put(objectName, num);
                                        } else {
                                            objectNumHashMap.put(objectName, 1);
                                        }
                                    }
                                    for (String objectName:objectNumHashMap.keySet()){
                                        if (totalObjectNumMap.containsKey(objectName)){
                                            Integer num=totalObjectNumMap.get(objectName);
                                            num++;
                                            totalObjectNumMap.put(objectName,num);
                                        }else {
                                            totalObjectNumMap.put(objectName,1);
                                        }
                                    }
                                    for (String objectName : objectNumHashMap.keySet()) {
//                                        System.out.println("self class:"+objectName);
                                        RssOdSampleClass rssOdSampleClass = new RssOdSampleClass();
                                        Integer num = objectNumHashMap.get(objectName);
                                        //获得目标类别编码


                                        String code = objectCodeMap.get(objectName);
                                        if (code==null){
                                            if (!wrongName.contains(objectName)){
                                                wrongName.add(objectName);
                                            }
                                        }else {
                                            if (!rightName.contains(objectName)){
                                                rightName.add(objectName);
                                            }
                                        }
                                        maxOdSampleClassId++;
                                        rssOdSampleClass.setId(maxOdSampleClassId);
                                        rssOdSampleClass.setClassId(code);
                                        rssOdSampleClass.setSampleId(maxOdSampleId);
                                        rssOdSampleClass.setObjectNum(num);
                                        rssOdSampleClass.setCreateBy("admin");
                                        rssOdSampleClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                        rssOdSampleClass.setUpdateBy(null);
                                        rssOdSampleClass.setUpdateTime(null);
//                                    System.out.println("objectName:"+objectName+"num:"+objectNumHashMap.get(objectName));
                                        rssOdSampleClassList.add(rssOdSampleClass);

                                    }
                                } else {
//                                    System.out.println("该样本没有目标！！！");
                                }

                                //2.1标签文件复制到对应的目录下
                                rssMinioUtil.uploadFile(f, "object-detection", minioLablePath);
                                rssMinioUtil.uploadFile(f, "object-detection", unitminioLablePath);
                                if (i % 50 == 0) {
                                    long endTime = System.currentTimeMillis();
                                    System.out.print("上传" + ((float) i / s.length) * 100 + "%完成");
                                    System.out.println("上传" + i + "耗时：" + (endTime - startTime) / 1000 + "秒");
                                }
                                //2.2数据文件复制到对应的目录下
                                File oldImageFile = new File(imagePath);
                                rssMinioUtil.uploadFile(oldImageFile, "object-detection", minioImagePath);

                            }
                            for (String objectName:totalObjectNumMap.keySet()){
                                System.out.println(objectName+":"+totalObjectNumMap.get(objectName));
                            }
                        }
                    }
                default:
                    break;


            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //3.1样本信息插入数据库
//        boolean insertRssODSampleBatch=rssPostgresqlService.insertRssOdSampleBatch(rssOdSampleList);
        //3.2样本类别信息插入数据库
//        boolean insertODsampleClassBatch=iRssOdSampleClassService.insertBatch(rssOdSampleClassList);

        long endTime=System.currentTimeMillis();
        System.out.println("总耗时："+(endTime-startTime)/1000+"秒");
//        return insertRssODSampleBatch;
        return true;
    }


    /**
     *
     * @param path
     * @param classMapPath
     * @param sheetNum
     * @param propertyFilePath
     * @return
     * @throws Exception
     */
    public boolean insertSCSampleWithProperty(String path, String classMapPath, int sheetNum,String propertyFilePath) throws Exception {
        //获取映射
        Map<String, String> classMap = RssExcelUtil.getClassMap(classMapPath, sheetNum);
        System.out.println("获取映射");

        //检查映射表是否有错
//        List<String> checkClassMapList = checkClassMap(classMapPath, sheetNum);
//        if(!checkClassMapList.isEmpty()){
//            System.out.println(checkClassMapList);
//            return false;
//        }
        System.out.println("jiancha yingshebiao shifou youcuo");

        LinkedList<RssScSample> rssScSampleList = new LinkedList<>();
        LinkedList<RssClassMap> rssClassMapList = new LinkedList<>();

        String bucketName = "scene-classification";
        rssMinioUtil.createBucket(bucketName);

        long allStartTime = System.currentTimeMillis();
        File datasetDir = new File(path);
        String grandParent = datasetDir.getParentFile().getParent();
        String parent = datasetDir.getParent();
        //获取数据集名
        String datasetName = parent.replace(grandParent,"").replace("/","");
        Integer datasetID = rssPostgresqlService.getDatasetID(datasetName);
        System.out.println(datasetID);
        //判断用途
        String ifTrnValTest = path.replace(parent,"").replace("/","");
        Integer trnValTest = null;
        switch (ifTrnValTest){
            case "train":
                trnValTest=0;
                break;
            case "val":
                trnValTest=1;
                break;
            case "test":
                trnValTest=2;
                break;
            default:
                break;
        }
        //判断是否已入库train/val/test
        Iterable<Result<Item>> datasetExistSampleNum = rssMinioUtil.listObjects(bucketName, datasetName, false);
        boolean ifExist = false;
        if(Iterables.size(datasetExistSampleNum)>0){
            ifExist = true;
        }
        System.out.println("判断是否已入库train/val/test");


        String imageType=null,instrument=null;
        Integer imageChannel=null;
        //解析property文件获取相关属性 待完成
        File propertyFile = new File(propertyFilePath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(propertyFile));
        String line = null;
        while((line = bufferedReader.readLine())!=null){
            String[] split = line.split(":");
            switch(split[0]){
                case "imageType":
                    imageType = split[1];
                    break;
                case "instrument":
                    instrument = split[1];
                    break;
                case "imageChannel":
                    imageChannel = Integer.parseInt(split[1]);
                    break;
                default:
                    break;
            }
        }

        Integer maxScSampleID = rssPostgresqlService.getMaxScSampleID();
        if(maxScSampleID==null){
            maxScSampleID = 0;
        }
//        Integer maxClassMapId = rssPostgresqlService.getMaxClassMapId();
//        if(maxClassMapId==null){
//            maxClassMapId = 0;
//        }
        System.out.println("jiexi wancheng");

        try{
            if(datasetDir.isDirectory()){
                String[] classes = datasetDir.list();

                for (int i = 0; i < classes.length; i++) {
                    String classMapValue = classMap.get(classes[i]);
                    //对应类别插入映射表内
                    //未入库train/val/test则插入
//                    if(!ifExist){
//                        RssClassMap rssClassMap = new RssClassMap();
//                        maxClassMapId++;
//                        rssClassMap.setId(maxClassMapId);
//                        rssClassMap.setDatasetId(datasetID);
//                        rssClassMap.setSelfClassName(classes[i]);
//                        rssClassMap.setUniClassName(classMapValue);
//                        rssClassMap.setCreateBy("admin");
//                        rssClassMap.setCreateTime(new Timestamp(System.currentTimeMillis()));
//
//                        rssClassMapList.add(rssClassMap);
//                    }
//                    System.out.println("yingshebiao");

                    //获取对应类的编码
                    String code = rssPostgresqlService.getScClassCode(classMapValue);

                    File imageDir = new File(path + File.separator + classes[i]);
                    if(imageDir.isDirectory()){
                        String[] images = imageDir.list();
                        //已入库数量
                        int exsitSampleNum = 0;
                        if(ifExist){
                            Iterable<Result<Item>> sampleNumIter = rssMinioUtil.listObjects(bucketName, datasetName + File.separator + classMapValue, true);
                            exsitSampleNum = Iterables.size(sampleNumIter);
                        }
                        System.out.println(classes[i]);

                        for (int j = 0; j < images.length; j++) {
                            System.out.println(images.length);
                            maxScSampleID++;
                            RssScSample rssScSample = new RssScSample();
                            String suffix = RssExcelUtil.getSuffix(images[j]);
                            String imageNewName = classMapValue.replace(" ","_")+"_version1_"+images[j];
                            System.out.println(imageNewName);
                            String imagePath = bucketName+File.separator+datasetName + File.separator + classMapValue + File.separator + imageNewName;
                            Timestamp createTime = new Timestamp(System.currentTimeMillis());
                            String oldPath = path + File.separator + classes[i] + File.separator + images[j];

                            File oldImageFile = new File(oldPath);

                            String objectName = datasetName+File.separator+classMapValue + File.separator + imageNewName;
                            rssMinioUtil.uploadFile(oldImageFile,bucketName,objectName);

                            int height = 0;
                            int width = 0;
                            switch (suffix){
                                case "jpg":
                                case "png":
                                case "TIF":
                                case "tif":
                                case "tiff":
                                    BufferedImage bi = ImageIO.read(oldImageFile);
                                    height = bi.getHeight();
                                    width = bi.getWidth();
                                    break;
                                default:
                                    break;
                            }

                            //插入数据库
                            rssScSample.setId(maxScSampleID);
                            //目前无单张影像多个场景 sampleid与id保持一致
                            rssScSample.setSampleId(String.valueOf(maxScSampleID));
                            rssScSample.setDatasetId(datasetID);
                            rssScSample.setClassId(code);
                            rssScSample.setSampleWidth(width);
                            rssScSample.setSampleHeight(height);
                            rssScSample.setImagePath(imagePath);
                            rssScSample.setImageType(imageType);
                            rssScSample.setImageChannels(imageChannel);
                            rssScSample.setInstrument(instrument);
                            rssScSample.setTrnValueTest(trnValTest);
                            rssScSample.setCreateBy("admin");
                            rssScSample.setCreateTime(createTime);

                            rssScSampleList.add(rssScSample);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean insertBatch = iRssScSampleService.insertBatch(rssScSampleList);
        if(!rssClassMapList.isEmpty()){
//            boolean insert = iRssClassMapService.insertBatch(rssClassMapList);
        }

        long allEndTime = System.currentTimeMillis();
        long time = allEndTime-allStartTime;
        long min = time / (1000 * 60);
        long sec = time%(1000*60)/ 1000;
        System.out.println("============================");
        System.out.println("入库总耗时："+min+" min "+sec+" s");
        return insertBatch;

    }

    public HashMap<String,Integer> getImageNameAppTypeMap(String filePath) throws Exception{
        HashMap<String,Integer> imageNameAppTypeMap=new HashMap<>();
        File mainDir=new File(filePath);
        String[] s=mainDir.list();
        for (int i=0;i<s.length;i++){
            String fileName=s[i];
            switch (fileName){
                case "test.txt":
                    FileReader testfr = new FileReader(filePath+"/test.txt");
                    BufferedReader testbf = new BufferedReader(testfr);
                    String teststr;
                    while ((teststr = testbf.readLine()) != null) {
                        imageNameAppTypeMap.put(teststr,2);
                    }
                    testbf.close();
                    testfr.close();
                    break;
                case "train.txt":
                    FileReader trainfr = new FileReader(filePath+"/train.txt");
                    BufferedReader trainbf = new BufferedReader(trainfr);
                    String trainstr;
                    while ((trainstr = trainbf.readLine()) != null) {
                        imageNameAppTypeMap.put(trainstr,0);
                    }
                    trainbf.close();
                    trainfr.close();
                    break;
                case "val.txt":
                    FileReader valfr = new FileReader(filePath+"/val.txt");
                    BufferedReader valbf = new BufferedReader(valfr);
                    String valstr;
                    while ((valstr = valbf.readLine()) != null) {
                        imageNameAppTypeMap.put(valstr,1);
                    }
                    valbf.close();
                    valfr.close();
                    break;
                 default:
                     break;
            }
        }
        return imageNameAppTypeMap;
    }
    public HashMap<String,String > getObjectCodeMap(Map<String,String> classMap){
        HashMap<String,String> objectCodeMap=new HashMap<>();
        for (String className:classMap.keySet()){
            System.out.println(className+":"+classMap.get(className));
            //获得目标类别编码
            String uniObjectName=classMap.get(className);
            RssParam rssParam=new RssParam();
            rssParam.setObjectName(uniObjectName);
            List<RssOdClass> rssOdClassList=rssQuery.getOdClass(rssParam);
            String code=null;
            if (rssOdClassList.size()==0){
                System.out.println("od_class不存在"+className);
            }else {
                code =rssOdClassList.get(0).getCode();
                objectCodeMap.put(className,code);
            }
        }
        return objectCodeMap;
    }

    /**
     * Insert class information of sence classification into database.
     * @param filePath Path of class information of sence classification.
     * @return
     */
    public boolean insertSCClass(String filePath){
        String path ="F:\\studyofPostgraduate\\项目\\样本库\\本子\\20201230/分类体系最新20210310加英文.xlsx";
        List<RssScClass> rssScClasses = null;
        try {
            rssScClasses = RssExcelUtil.readScExcel(path, 0);
            RssScClass rssScClass = rssScClasses.get(1);
            System.out.println(rssScClass);

        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean insert=iRssScClassService.insertBatch(rssScClasses);
//        boolean insert=iRssScClassService.save(rssScClasses.get(0));
        return insert;
    }


    /**
     * Insert class information of object detection into database.
     * @param filePath Path of class information of object detection
     * @return
     */
    public boolean insertODClass(String filePath){
        String path = "F:\\studyofPostgraduate\\项目\\样本库\\本子\\20201230/分类体系最新20210310加英文.xlsx";
        List<RssOdClass> rssOdClasses = null;
        try{
            rssOdClasses = RssExcelUtil.readOdExcel(path, 1);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean insertBatch = iRssOdClassService.insertBatch(rssOdClasses);

        return insertBatch;
    }



}
