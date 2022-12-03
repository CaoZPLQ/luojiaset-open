package cn.whu.geois.modules.rssample.util;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.whu.geois.modules.rssample.xml.*;

import it.geosolutions.imageio.stream.output.ImageOutputStreamAdapter;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.Envelope2D;
import org.hibernate.service.spi.ServiceException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author czp
 * @version 1.0
 * @date 2021/1/28 21:32
 */
@Component
public class RssFileUtil {
    public BridgeDsAnn parseBDAXML(File filed){
        try {
            JAXBContext context=JAXBContext.newInstance(BridgeDsAnn.class);
            Unmarshaller unmarshaller=context.createUnmarshaller();
            if(!filed.exists() ) {
                System.out.println("not found file.");
                return null;
            }
            return (BridgeDsAnn)unmarshaller.unmarshal(filed);

        }catch (JAXBException e){
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public RssOdAnn parseDOTAAXML(File filed){
        try {
            JAXBContext context=JAXBContext.newInstance(RssOdAnn.class);
            Unmarshaller unmarshaller=context.createUnmarshaller();
            if(!filed.exists() ) {
                System.out.println("not found file.");
                return null;
            }
            return (RssOdAnn) unmarshaller.unmarshal(filed);

        }catch (JAXBException e){
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public RssOdAnn parseOdAXML(InputStream inputStream){
        try {
            JAXBContext context=JAXBContext.newInstance(RssOdAnn.class);
            Unmarshaller unmarshaller=context.createUnmarshaller();
            return (RssOdAnn) unmarshaller.unmarshal(inputStream);

        }catch (JAXBException e){
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public RssHrscAnn parseHRSCXML(File filed){
        try {
            JAXBContext context=JAXBContext.newInstance(RssHrscAnn.class);
            Unmarshaller unmarshaller=context.createUnmarshaller();
            if(!filed.exists() ) {
                System.out.println("not found file.");
                return null;
            }
            return (RssHrscAnn) unmarshaller.unmarshal(filed);

        }catch (JAXBException e){
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public RssOdObbAnn parseObbBoxXML(File filed) {
        try {
            JAXBContext context = JAXBContext.newInstance(RssOdObbAnn.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (!filed.exists()) {
                System.out.println("not found file.");
                return null;
            }
            return (RssOdObbAnn) unmarshaller.unmarshal(filed);

        } catch (JAXBException e) {
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public RssAirshipDsAnn parseADAXML(File filed){
        try {
            JAXBContext context=JAXBContext.newInstance(RssAirshipDsAnn.class);
            Unmarshaller unmarshaller=context.createUnmarshaller();
            if(!filed.exists() ) {
                System.out.println("not found file.");
                return null;
            }
            return (RssAirshipDsAnn) unmarshaller.unmarshal(filed);

        }catch (JAXBException e){
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    /**
     *
     * @param path
     * @return
     */

    public Timestamp getFileCreateTime(String path){
        File file = new File(path);
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long time = basicFileAttributes.creationTime().toMillis();
            Timestamp createTime = new Timestamp(time);
            return createTime;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 本地文件（图片、excel等）转换成Base64字符串
     *
     * @param imgPath
     */
    public  String convertFileToBase64(String imgPath) {
        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imgPath);
            System.out.println("文件大小（字节）="+in.available());
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组进行Base64编码，得到Base64编码的字符串
        BASE64Encoder encoder = new BASE64Encoder();
        String base64Str = encoder.encode(data);
        return "data:image/jpeg;base64,"+base64Str;
    }
    //将本地文件压缩为tar.gz文件
    public  void packet(List<File> resourceList, String outPath) throws Exception {
        //1. 参数验证, 初始化输出路径
        if(resourceList == null || resourceList.size() < 1 ){
            throw new ServiceException("文件压缩执行异常, 非法参数!");
        }
        long startTime = System.currentTimeMillis();
        // 2. 迭代源文件集合, 将文件打包为Tar
        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath+".tmp");
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);
             TarOutputStream tarOutputStream = new TarOutputStream(bufferedOutput);) {
            for (File resourceFile : resourceList) {
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
        }
    }
    public RssAirshipDsAnn parseAIRSHIPAXML(File filed){
        try {
            JAXBContext context=JAXBContext.newInstance(RssAirshipDsAnn.class);
            Unmarshaller unmarshaller=context.createUnmarshaller();
            if(!filed.exists() ) {
                System.out.println("not found file.");
                return null;
            }
            return (RssAirshipDsAnn) unmarshaller.unmarshal(filed);

        }catch (JAXBException e){
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public  boolean beanToXml(Object obj,Class<?> load,String xmlPath) throws JAXBException{
        try {
            JAXBContext context = JAXBContext.newInstance(load);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "GBK");
            StringWriter writer = new StringWriter();
            marshaller.marshal(obj,writer);

            BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(xmlPath)));
            bfw.write(writer.toString());
            bfw.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public RssOdPolAnn parsePolBoxXML(File filed) {
        try {
            JAXBContext context = JAXBContext.newInstance(RssOdPolAnn.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (!filed.exists()) {
                System.out.println("not found file.");
                return null;
            }
            return (RssOdPolAnn) unmarshaller.unmarshal(filed);

        } catch (JAXBException e) {
            System.err.println("解析有误!" + e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<RssOdAnn> cowcToRssOdAnn(String filePath) {
        List<RssOdAnn> rssOdAnns=new LinkedList<>();
        try {
            HashMap<String,String> selfIdNameMap=new HashMap<>();
            selfIdNameMap.put("0","Neg");
            selfIdNameMap.put("1","Other");
            selfIdNameMap.put("2","Pickup");
            selfIdNameMap.put("3","Sedan");
            selfIdNameMap.put("4","Unknown");
            //读取txt文件
            File imageDir=new File(filePath+File.separator+"images");
            String[] files=imageDir.list();
            HashMap<String,RssOdAnn> rssOdAnnHashMap=new HashMap<>();
            for (int i=0;i<files.length;i++){
                String fileName=files[i];
                if (fileName.endsWith("txt")){
                    RssOdAnn rssOdAnn=new RssOdAnn();
                    String imageName=fileName.replace("txt","jpg");
                    rssOdAnn.setFilename(imageName);
                    RssOdAnn.Size size=new RssOdAnn.Size();
                    size.setDepth("3");
//                    size.setWidth("32");
//                    size.setHeight("32");
                    size.setWidth("256");
                    size.setHeight("256");
                    rssOdAnn.setSize(size);
                    if (i%50==0){
                        System.out.println("读取"+i+"个");
                    }
                    File labelFile=new File(filePath+File.separator+"images"+File.separator+fileName);
                    BufferedReader bufferedReader=new BufferedReader(new FileReader(labelFile));
                    String line=null;
                    ArrayList<RssOdAnn.Object> objectArrayList=new ArrayList<>();
                    while ((line=bufferedReader.readLine())!=null){
                        String[] infos=line.split(" ");
                        String classId=infos[0];
                        String selfClassName=selfIdNameMap.get(classId);
                        int centerX=(int)(Double.parseDouble(infos[1])*256);
                        int centerY=(int)(Double.parseDouble(infos[2])*256);
                        Integer xmin=centerX-16;
                        Integer ymin=centerY-16;
                        Integer xmax=centerX+16;
                        Integer ymax=centerY+16;
//                        System.out.println("centerX:"+centerX);
//                        System.out.println("centerY:"+centerY);
                        RssOdAnn.Object object=new RssOdAnn.Object();
                        RssOdAnn.Object.Bndbox bndbox=new RssOdAnn.Object.Bndbox();
                        bndbox.setXmin(xmin.toString());
                        bndbox.setYmin(ymin.toString());
                        bndbox.setXmax(xmax.toString());
                        bndbox.setYmax(ymax.toString());
                        object.setBndbox(bndbox);
                        object.setName(selfClassName);
                        objectArrayList.add(object);

                    }
                    rssOdAnn.setObject(objectArrayList);
                    rssOdAnns.add(rssOdAnn);
                    rssOdAnnHashMap.put(imageName,rssOdAnn);
                    bufferedReader.close();
                }
            }
            int i=0;
            System.out.println("rssOdAnnHashMap.size()"+rssOdAnnHashMap.size());
            for (String imageName:rssOdAnnHashMap.keySet()){
                RssOdAnn rssOdAnn=rssOdAnnHashMap.get(imageName);
                i++;
                if (i%50==0){
                    System.out.println("完成"+i+"个");
                }

                String hbbxmlFlePath=filePath+File.separator+"hbbxml"+File.separator+rssOdAnn.getFilename().replace(".jpg",".xml");
                boolean success= beanToXml(rssOdAnn,RssOdAnn.class,hbbxmlFlePath);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return rssOdAnns;
    }


    //将xml文件中的自身类别修改为统一类别
    public RssOdAnn replaceClassName(RssOdAnn oldRssOdAnn,Map<String,String> classMap){
        List<RssOdAnn.Object> oldObjects=oldRssOdAnn.getObject();
        ArrayList<RssOdAnn.Object> newObjects=new ArrayList<>();
        if (oldObjects!=null&&oldObjects.size()!=0){
            for (RssOdAnn.Object object:oldObjects){
                String selfName=object.getName();
                if (selfName!=null&&!selfName.equals("null")){
                    String unitName=classMap.get(selfName);
//                    System.out.println("unitName:"+unitName);
                    object.setName(unitName);

                    //将旋转框的中心坐标加长宽角度标注方式转为四个角点坐标加角度
                    RssOdAnn.Object.Bndbox bndbox=object.getBndbox();
                    String boxAngle=bndbox.getBox_angle();
                    if (boxAngle!=null){

                        String boxCenterX=bndbox.getCenter_x();
                        String boxCenterY=bndbox.getCenter_y();
                        String boxWidth=bndbox.getBox_width();
                        String boxHeight=bndbox.getBox_height();
                        Double boxCenterXD=Double.parseDouble(boxCenterX);
                        Double boxCenterYD=Double.parseDouble(boxCenterY);
                        Double boxWidthD=Double.parseDouble(boxWidth);
                        Double boxHeightD=Double.parseDouble(boxHeight);
                        Double boxAngleD=Double.parseDouble(boxAngle);

                        int x0=(int) Math.round(boxCenterXD+(boxWidthD*Math.cos(boxAngleD)+boxHeightD*Math.sin(boxAngleD)));
                        int y0=(int) Math.round(boxCenterYD+(boxHeightD*Math.cos(boxAngleD)-boxWidthD*Math.sin(boxAngleD)));

                        int x1=(int) Math.round(boxCenterXD+(boxWidthD*Math.cos(boxAngleD)-boxHeightD*Math.sin(boxAngleD)));
                        int y1=(int) Math.round(boxCenterYD-(boxHeightD*Math.cos(boxAngleD)+boxWidthD*Math.sin(boxAngleD)));

                        int x2=(int) Math.round(boxCenterXD-(boxWidthD*Math.cos(boxAngleD)+boxHeightD*Math.sin(boxAngleD)));
                        int y2=(int) Math.round(boxCenterYD-(boxHeightD*Math.cos(boxAngleD)-boxWidthD*Math.sin(boxAngleD)));

                        int x3=(int) Math.round(boxCenterXD-(boxWidthD*Math.cos(boxAngleD)-boxHeightD*Math.sin(boxAngleD)));
                        int y3=(int) Math.round(boxCenterYD+(boxHeightD*Math.cos(boxAngleD)+boxWidthD*Math.sin(boxAngleD)));

                        bndbox.setX0(String.valueOf(x0));
                        bndbox.setY0(String.valueOf(y0));
                        bndbox.setX1(String.valueOf(x1));
                        bndbox.setY1(String.valueOf(y1));
                        bndbox.setX2(String.valueOf(x2));
                        bndbox.setY2(String.valueOf(y2));
                        bndbox.setX3(String.valueOf(x3));
                        bndbox.setY3(String.valueOf(y3));
                    }
                    object.setBndbox(bndbox);
                    newObjects.add(object);
                }
            }
            oldRssOdAnn.setObject(newObjects);
        }else {
            System.out.println(oldRssOdAnn.getFilename()+"----no objects");
        }

        return oldRssOdAnn;

    }

    /** 读 **/
//    public Map<Integer, SiteEntity> getSiteMap() {
    public List<RssOdAnn> xViewToRssOdAnn(String filePath) {
        List<RssOdAnn> rssOdAnns=new LinkedList<>();
        HashMap<String,String> selfIdNameMap=new HashMap<>();
        selfIdNameMap.put("11","Fixed-wing Aircraft");
//        selfIdNameMap.put("11","Fixed-Wing Aircraft");
        selfIdNameMap.put("12","Small Aircraft");
        selfIdNameMap.put("13","Passenger/Cargo Plane");
        selfIdNameMap.put("15","Helicopter");
        selfIdNameMap.put("17","Passenger Vehicle");
        selfIdNameMap.put("18","Small Car");
        selfIdNameMap.put("19","Bus");
        selfIdNameMap.put("20","Pickup Truck");
        selfIdNameMap.put("21","Utility Truck");
        selfIdNameMap.put("23","Truck");
        selfIdNameMap.put("24","Cargo Truck");
        selfIdNameMap.put("25","Truck Tractor w/ Box Trailer");
        selfIdNameMap.put("26","Truck Tractor");
        selfIdNameMap.put("27","Trailer");
        selfIdNameMap.put("28","Truck Tractor w/ Flatbed Trailer");
        selfIdNameMap.put("29","Truck Tractor w/ Liquid Tank");
        selfIdNameMap.put("32","Crane Truck");
        selfIdNameMap.put("33","Railway Vehicle");
        selfIdNameMap.put("34","Passenger Car");
        selfIdNameMap.put("35","Cargo/Container Car");
        selfIdNameMap.put("36","Flat Car");
        selfIdNameMap.put("37","Tank car");
        selfIdNameMap.put("38","Locomotive");
        selfIdNameMap.put("40","Maritime Vessel");
        selfIdNameMap.put("41","Motorboat");
        selfIdNameMap.put("42","Sailboat");
        selfIdNameMap.put("44","Tugboat");
        selfIdNameMap.put("45","Barge");
        selfIdNameMap.put("47","Fishing Vessel");
        selfIdNameMap.put("49","Ferry");
        selfIdNameMap.put("50","Yacht");
        selfIdNameMap.put("51","Container Ship");
        selfIdNameMap.put("52","Oil Tanker");
        selfIdNameMap.put("53","Engineering Vehicle");
        selfIdNameMap.put("54","Tower crane");
        selfIdNameMap.put("55","Container Crane");
        selfIdNameMap.put("56","Reach Stacker");
        selfIdNameMap.put("57","Straddle Carrier");
        selfIdNameMap.put("59","Mobile Crane");
        selfIdNameMap.put("60","Dump Truck");
        selfIdNameMap.put("61","Haul Truck");
        selfIdNameMap.put("62","Scraper/Tractor");
        selfIdNameMap.put("63","Front loader/Bulldozer");
        selfIdNameMap.put("64","Excavator");
        selfIdNameMap.put("65","Cement Mixer");
        selfIdNameMap.put("66","Ground Grader");
        selfIdNameMap.put("71","Hut/Tent");
        selfIdNameMap.put("72","Shed");
        selfIdNameMap.put("73","Building");
        selfIdNameMap.put("74","Aircraft Hangar");
        selfIdNameMap.put("76","Damaged Building");
        selfIdNameMap.put("77","Facility");
        selfIdNameMap.put("79","Construction Site");
        selfIdNameMap.put("83","Vehicle Lot");
        selfIdNameMap.put("84","Helipad");
        selfIdNameMap.put("86","Storage Tank");
        selfIdNameMap.put("89","Shipping container lot");
        selfIdNameMap.put("91","Shipping Container");
        selfIdNameMap.put("93","Pylon");
        selfIdNameMap.put("94","Tower");
        String trainImages=filePath+File.separator+"train_images"+File.separator+"train_images";
        File trainImagesFile=new File(trainImages);
        String[] imageNameArr=trainImagesFile.list();
        HashMap<String,String> imageNameSizeMap=new HashMap<>();
        HashMap<String,String> imageNameBboxMap=new HashMap<>();
        try {
            for (int i=0;i<imageNameArr.length;i++){
                String imageName=imageNameArr[i];
                String oldImageFile=trainImagesFile+File.separator+imageNameArr[i];
//                BufferedImage bi = ImageIO.read(new File(oldImageFile));
//                int height = bi.getHeight();
//                int width = bi.getWidth();
                GeoTiffReader reader=new GeoTiffReader(oldImageFile);
                GridCoverage2D coverage = reader.read(null);
                CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();

                Envelope2D coverageEnvelope = coverage.getEnvelope2D();
//                Double coverageMinX = coverageEnvelope.getBounds().getMinX();
                Double x= coverageEnvelope.x;
                Double y= coverageEnvelope.y;
                Double width= coverageEnvelope.width;
                Double height= coverageEnvelope.height;
//                Double coverageMinX = coverageEnvelope.getBounds().getMinX();
//                Double coverageMaxX = coverageEnvelope.getBounds().getMaxX();
//                Double coverageMinY = coverageEnvelope.getBounds().getMinY();
//                Double coverageMaxY = coverageEnvelope.getBounds().getMaxY();
                Double coverageMinX = x;
                Double coverageMaxX = x+width;
                Double coverageMinY = y;
                Double coverageMaxY = y+height;
//                System.out.println("x:"+x);
//                System.out.println("y:"+y);
//                System.out.println("width:"+width);
//                System.out.println("height:"+height);
//                System.out.println("coverageMinX:"+coverageMinX);
//                System.out.println("coverageMaxX:"+coverageMaxX);
//                System.out.println("coverageMinY:"+coverageMinY);
//                System.out.println("coverageMaxY:"+coverageMaxY);
                imageNameBboxMap.put(imageName,String.valueOf(coverageMinX)+"_"+String.valueOf(coverageMinY)
                        +"_"+String.valueOf(coverageMaxX)+"_"+String.valueOf(coverageMaxY));
                //获取影像长宽
                int imageWidth = coverage.getRenderedImage().getWidth();
                int imageHeight = coverage.getRenderedImage().getHeight();
                imageNameSizeMap.put(imageName,String.valueOf(imageHeight)+"_"+String.valueOf(imageWidth));
                System.out.println(imageName+"-imageHeight:"+imageHeight);
            }
        }catch (Exception e){
            e.printStackTrace();
        }



        try{
            BufferedReader br =new BufferedReader(new FileReader(new File(filePath+File.separator+"train_labels/xView_train.geojson")));
            String s = null;
            HashMap<String,RssOdAnn> rssOdAnnHashMap=new HashMap<>();
            while((s = br.readLine()) != null){  // s 为原生的json串
//                System.out.println("00=="+s);
                JSONObject jo = new JSONObject(s); // 创建一个包含原始json串的json对象
                JSONArray features = jo.getJSONArray("features"); //找到features的json数组

                for (int i = 0; i < features.size(); i++) {
//                    SiteEntity siteEntity = new SiteEntity();
                    JSONObject info = features.getJSONObject(i); // 获得features的第i个对象

                    JSONObject geometry = info.getJSONObject("geometry");
                    JSONObject properties = info.getJSONObject("properties");
                    String imageName=properties.get("image_id").toString();
                    String[] boundsImcoords=properties.get("bounds_imcoords").toString().split(",");
                    String typeId=properties.get("type_id").toString();
                    String ingestTime=properties.get("ingest_time").toString();
                    Object geoObject=geometry.getJSONArray("coordinates").get(0);
                    String coordinates=geoObject.toString();


                    String name=selfIdNameMap.get(typeId);
                    if (name==null||name.equals("null")){
                        System.out.println("typeId:"+typeId);
                    }

                    if (rssOdAnnHashMap.containsKey(imageName)){
                        RssOdAnn rssOdAnn=rssOdAnnHashMap.get(imageName);
                        ArrayList<RssOdAnn.Object> objectArrayList=rssOdAnn.getObject();
                        RssOdAnn.Object object=new RssOdAnn.Object();
                        RssOdAnn.Object.Bndbox bndbox=new RssOdAnn.Object.Bndbox();
                        bndbox.setXmin(boundsImcoords[0]);
                        bndbox.setYmin(boundsImcoords[1]);
                        bndbox.setXmax(boundsImcoords[2]);
                        bndbox.setYmax(boundsImcoords[3]);
                        bndbox.setCoordinates(coordinates);
                        bndbox.setIngestTime(ingestTime);

                        object.setBndbox(bndbox);
                        object.setName(name);
                        objectArrayList.add(object);
                        rssOdAnn.setObject(objectArrayList);
                        rssOdAnnHashMap.put(imageName,rssOdAnn);
                    }else {
                        RssOdAnn rssOdAnn=new RssOdAnn();
                        rssOdAnn.setFilename(imageName);
                        RssOdAnn.Size size=new RssOdAnn.Size();
                        if (imageNameSizeMap.containsKey(imageName)){
                            String width=imageNameSizeMap.get(imageName).split("_")[1];
                            String height=imageNameSizeMap.get(imageName).split("_")[0];
                            size.setDepth("3");
                            size.setWidth(width);
                            size.setHeight(height);
                            rssOdAnn.setSize(size);

                            String[] bbox=imageNameBboxMap.get(imageName).split("_");
                            rssOdAnn.setMinLongitude(Double.parseDouble(bbox[0]));
                            rssOdAnn.setMinLatitude(Double.parseDouble(bbox[1]));
                            rssOdAnn.setMaxLongitude(Double.parseDouble(bbox[2]));
                            rssOdAnn.setMaxLatitude(Double.parseDouble(bbox[3]));

                            ArrayList<RssOdAnn.Object> objectArrayList=new ArrayList<>();
                            RssOdAnn.Object object=new RssOdAnn.Object();
                            RssOdAnn.Object.Bndbox bndbox=new RssOdAnn.Object.Bndbox();
                            bndbox.setXmin(boundsImcoords[0]);
                            bndbox.setYmin(boundsImcoords[1]);
                            bndbox.setXmax(boundsImcoords[2]);
                            bndbox.setYmax(boundsImcoords[3]);
                            bndbox.setCoordinates(coordinates);
                            object.setBndbox(bndbox);
                            object.setName(name);
                            bndbox.setCoordinates(coordinates);
                            bndbox.setIngestTime(ingestTime);

                            objectArrayList.add(object);
                            rssOdAnn.setObject(objectArrayList);
                            rssOdAnnHashMap.put(imageName,rssOdAnn);
                        }else {
                            System.out.println("imageName:"+imageName+"不存在！！！");
                        }

                    }
//                    if (typeId.equals("33")){
//                        System.out.println("properties.get(\"image_id\"):"+properties.get("image_id"));
//                        System.out.println("properties.toString()"+properties.toString());
//                        System.out.println("coordinates:"+coordinates);
//                    }
//                    if (properties.get("image_id").equals("1036.tif")){
//                        System.out.println("properties.toString()"+properties.toString());
////                    siteEntity.setSite_stano(properties.getString("stano"));  // 获得站名
//                        Object object=geometry.getJSONArray("coordinates").get(0);
////                        System.out.println(geometry.getJSONArray("coordinates"));
//                        System.out.println(object.toString());
//
//                    }

//                    List list  = geometry.getJSONArray("coordinates").toList();  // 获得经纬度
//                    siteEntity.setSite_longitude(Double.parseDouble(list.get(0).toString()));
//                    siteEntity.setSite_latitude(Double.parseDouble(list.get(1).toString()));
//                    System.out.println(siteEntity.getSite_longitude()+"\n"+siteEntity.getSite_latitude());
//                    map.put(i,siteEntity);
                }
                System.out.println("features.size():"+features.size());
            }
            int i=0;
            System.out.println("rssOdAnnHashMap.size()"+rssOdAnnHashMap.size());
            for (String imageName:rssOdAnnHashMap.keySet()){
                RssOdAnn rssOdAnn=rssOdAnnHashMap.get(imageName);
                i++;
                if (i%50==0){
                    System.out.println("完成"+i+"个");
                }

                String hbbxmlFlePath=filePath+File.separator+"hbbxml"+File.separator+rssOdAnn.getFilename().replace(".tif",".xml");
//                boolean success= beanToXml(rssOdAnn,RssOdAnn.class,hbbxmlFlePath);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return rssOdAnns;
    }


    /**写**/
//    public void jsonOutPut(Map map) {
//        ObjectMapper mapper = new ObjectMapper();
//        try{
//            mapper.writeValue(new File("D:/river-site.json"), map);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }




    //将minio文件压缩为tar.gz文件
//    public  void packet(List<FileInputStream> resourceList, String outPath) throws Exception {
//
//        long startTime = System.currentTimeMillis();
//        // 2. 迭代源文件集合, 将文件打包为Tar
//        try (FileOutputStream fileOutputStream = new FileOutputStream(outPath+".tmp");
//             BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutputStream);
//             TarOutputStream tarOutputStream = new TarOutputStream(bufferedOutput);) {
//            for (FileInputStream fileInputStream : resourceList) {
//
//                try(
//                    BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);){
//                    TarEntry entry = new TarEntry(new File(fileInputStream.toString()));
//                    entry.setSize(resourceFile.length());
//                    tarOutputStream.putNextEntry(entry);
//                    IOUtils.copy(bufferedInput, tarOutputStream);
//                } catch (Exception e) {
//                    throw new ServiceException("文件["+resourceFile+"]压缩执行异常, 嵌套异常: \n" + e.toString());
//                }finally {
//                    tarOutputStream.closeEntry();
//                }
//            }
//        } catch (Exception e) {
//            Files.delete(Paths.get(outPath+".tmp"));
//            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
//        }
//        //3. 读取打包好的Tar临时文件文件, 使用GZIP方式压缩
//        try (FileInputStream fileInputStream = new FileInputStream(outPath+".tmp");
//             BufferedInputStream bufferedInput = new BufferedInputStream(fileInputStream);
//             FileOutputStream fileOutputStream = new FileOutputStream(outPath);
//             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
//             BufferedOutputStream bufferedOutput = new BufferedOutputStream(gzipOutputStream);
//        ) {
//            byte[] cache = new byte[1024];
//            for (int index = bufferedInput.read(cache); index != -1; index = bufferedInput.read(cache)) {
//                bufferedOutput.write(cache,0,index);
//            }
//            long endTime = System.currentTimeMillis();
//            System.out.println("文件["+outPath+"]压缩执行完毕, 耗时:" + (endTime - startTime) + "ms");
//        } catch (Exception e) {
//            throw new ServiceException("文件压缩至["+outPath+"]执行异常, 嵌套异常: \n" + e.toString());
//        }finally {
//            Files.delete(Paths.get(outPath+".tmp"));
//        }
//    }
    /**
     * 将base64字符串，生成文件
     */
    public  File convertBase64ToFile(String fileBase64String, String filePath, String fileName) {

        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {//判断文件目录是否存在
                dir.mkdirs();
            }

            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bfile = decoder.decodeBuffer(fileBase64String);

            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
