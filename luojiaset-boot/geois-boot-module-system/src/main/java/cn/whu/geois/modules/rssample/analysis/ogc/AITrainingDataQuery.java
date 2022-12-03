package cn.whu.geois.modules.rssample.analysis.ogc;

import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.whu.geois.modules.rssample.analysis.RssQuery;
import cn.whu.geois.modules.rssample.entity.*;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingData;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingDataClassMap;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingDataCollection;
import cn.whu.geois.modules.rssample.service.RssPostgresqlService;
import cn.whu.geois.modules.rssample.util.RssFileUtil;
import cn.whu.geois.modules.rssample.util.RssGsUtil;
import cn.whu.geois.modules.rssample.util.RssMinioUtil;
import cn.whu.geois.modules.rssample.xml.RssOdAnn;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.ObjectStat;
import org.apache.commons.compress.utils.IOUtils;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.util.factory.GeoTools;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author czp
 * @version 1.0
 * @date 2021/8/6 10:27
 */
@Component
public class AITrainingDataQuery {

    private static final WKTReader READER = new WKTReader();
    @Autowired
    private RssPostgresqlService rssPostgresqlService;
    @Autowired
    private RssQuery rssQuery;
    @Autowired
    private RssMinioUtil rssMinioUtil;
    @Autowired
    private RssFileUtil rssFileUtil;
    @Autowired
    private RssGsUtil rssGsUtil;

    public List<AITrainingDataCollection> getCollections(){
        List<AITrainingDataCollection> trainingDataCollections=new LinkedList<>();
        //获得数据集类别
        List<AITrainingDataClassMap> trainingDataClassMaps=new LinkedList<>();
        HashMap<Integer,String[]> datasetIdClassesArrMap=new HashMap<>();
        trainingDataClassMaps=rssPostgresqlService.getDatasetIdClasses();
        for (int i=0;i<trainingDataClassMaps.size();i++){
            AITrainingDataClassMap datasetIdClassesMap=trainingDataClassMaps.get(i);
            String[] classPairs=datasetIdClassesMap.getClasses().split(",");
            String[] classes=new String[classPairs.length];
            for (int j=0;j<classPairs.length;j++){
                String[] classPair=classPairs[j].split("~");
                if (classPair.length==2){
                    classes[j]=classPair[1];
                }
            }
            datasetIdClassesArrMap.put(datasetIdClassesMap.getDatasetId(),classes);
        }

//        for (String datasetId:datasetIdClassesArrMap.keySet()){
//            System.out.println("datasetId:"+datasetId);
//            System.out.println("datasetIdClassesArrMap.get(datasetId):"+datasetIdClassesArrMap.get(datasetId).toString());
//        }
        QueryWrapper<RssDataset> queryWrapper = new QueryWrapper<>();
        List<RssDataset> rssDatasets=rssPostgresqlService.getCollections(queryWrapper);
        if (rssDatasets!=null&&rssDatasets.size()!=0){
            for (RssDataset rssDataset:rssDatasets){
                AITrainingDataCollection trainingDataCollection=transDatasetToCollection(rssDataset,datasetIdClassesArrMap);
                trainingDataCollections.add(trainingDataCollection);
            }
        }
        return trainingDataCollections;
    }
    public  List<AITrainingData> getTrainingData(String collectionId){
        List<AITrainingData> trainingDataList=new LinkedList<>();
        Integer collectionIdInt=Integer.parseInt(collectionId);
        //获得数据集任务类型
        List<RssDataset> rssDatasets =rssQuery.queryDatasetDetail(collectionId);
        String taskType="";
        if (rssDatasets.size()!=0){
            taskType=rssDatasets.get(0).getTaskType();

        }
        switch (taskType){
            case "od":
                QueryWrapper<RssOdSampleSimple> queryWrapperOd =new QueryWrapper<>();
                queryWrapperOd.eq("dataset_id",collectionIdInt);
                String combinationParam="";
                List<RssOdSampleSimple> rssOdSampleSimples=rssPostgresqlService.getOdSamples(queryWrapperOd,combinationParam);
                if (rssOdSampleSimples!=null&&rssOdSampleSimples.size()!=0){
                    for (RssOdSampleSimple rssOdSampleSimple:rssOdSampleSimples){
                        AITrainingData trainingData=transOdSampleToTrainingData(rssOdSampleSimple);
                        trainingDataList.add(trainingData);
                    }
                }
                break;
            case "sc":
                QueryWrapper<RssScSampleSimple> queryWrapperSc = new QueryWrapper<>();
                queryWrapperSc.eq("dataset_id", collectionIdInt);
                String combinationParamSc = "";
                List<RssScSampleSimple> rssScSampleSimples = rssPostgresqlService.getScSamples(queryWrapperSc, combinationParamSc);
                if (rssScSampleSimples != null && rssScSampleSimples.size() != 0) {
                    for (RssScSampleSimple rssScSampleSimple : rssScSampleSimples) {
//                        String imagepath=rssScSampleSimple.getImagePath().toString();
//                        System.out.println("imagepath"+imagepath);
                        AITrainingData trainingData = transScSampleToTrainingData(rssScSampleSimple);
                        trainingDataList.add(trainingData);
                    }
                }
                break;
            case "lc":
                QueryWrapper<RssLcSampleSimple> queryWrapperLc = new QueryWrapper<>();
                queryWrapperLc.eq("dataset_id", collectionIdInt);
                String combinationParamLc = "";
                List<RssLcSampleSimple> rssLcSampleSimples = rssPostgresqlService.getLcSamples(queryWrapperLc, combinationParamLc);
                if (rssLcSampleSimples != null && rssLcSampleSimples.size() != 0) {
                    for (RssLcSampleSimple rssLcSampleSimple : rssLcSampleSimples) {
                        AITrainingData trainingData = transLcSampleToTrainingData(rssLcSampleSimple);
                        trainingDataList.add(trainingData);
                    }
                }
                break;
            case "cd":
                QueryWrapper<RssCdSampleSimple> queryWrapperCd = new QueryWrapper<>();
                queryWrapperCd.eq("dataset_id", collectionIdInt);
                String combinationParamCd = "";
                List<RssCdSampleSimple> rssCdSampleSimples = rssPostgresqlService.getCdSamples(queryWrapperCd, combinationParamCd);
                if (rssCdSampleSimples != null && rssCdSampleSimples.size() != 0) {
                    for (RssCdSampleSimple rssCdSampleSimple : rssCdSampleSimples) {
//                        String imagepath=rssCdSampleSimple.getPreImagePath().toString();
//                        System.out.println("imagepath"+imagepath);
                        AITrainingData trainingData = transCdSampleToTrainingData(rssCdSampleSimple);
                        trainingDataList.add(trainingData);
                    }
                }
                break;
            default:
                break;
        }

        return trainingDataList;
    }

    public AITrainingData getTrainingDataById(String collectionId,String trainingDataId){
        AITrainingData trainingData=new AITrainingData();
        Integer collectionIdInt=Integer.parseInt(collectionId);
        Integer trainingDataIdInt=Integer.parseInt(trainingDataId);
        //获得数据集任务类型
        List<RssDataset> rssDatasets =rssQuery.queryDatasetDetail(collectionId);
        String taskType="";
        if (rssDatasets.size()!=0){
            taskType=rssDatasets.get(0).getTaskType();

        }
        switch (taskType){
            case "od":
                QueryWrapper<RssOdSampleSimple> queryWrapperOd =new QueryWrapper<>();
                queryWrapperOd.eq("dataset_id",collectionIdInt);
                queryWrapperOd.eq("ros.id",trainingDataIdInt);
                String combinationParam="";
                List<RssOdSampleSimple> rssOdSampleSimples=rssPostgresqlService.getOdSamples(queryWrapperOd,combinationParam);
                if (rssOdSampleSimples!=null&&rssOdSampleSimples.size()!=0){
                    for (RssOdSampleSimple rssOdSampleSimple:rssOdSampleSimples){
                       trainingData=transOdSampleToTrainingData(rssOdSampleSimple);
                    }
                }
                break;
            case "sc":
                QueryWrapper<RssScSampleSimple> queryWrapperSc = new QueryWrapper<>();
                queryWrapperSc.eq("dataset_id", collectionIdInt);
                queryWrapperSc.eq("scsample.id",trainingDataIdInt);
                String combinationParamSc = "";
                List<RssScSampleSimple> rssScSampleSimples = rssPostgresqlService.getScSamples(queryWrapperSc, combinationParamSc);
                if (rssScSampleSimples != null && rssScSampleSimples.size() != 0) {
                    for (RssScSampleSimple rssScSampleSimple : rssScSampleSimples) {
                        trainingData = transScSampleToTrainingData(rssScSampleSimple);
                    }
                }
                break;
            case "lc":
                QueryWrapper<RssLcSampleSimple> queryWrapperLc = new QueryWrapper<>();
                queryWrapperLc.eq("dataset_id", collectionIdInt);
                queryWrapperLc.eq("rls.id",trainingDataIdInt);
                String combinationParamLc = "";
                List<RssLcSampleSimple> rssLcSampleSimples = rssPostgresqlService.getLcSamples(queryWrapperLc, combinationParamLc);
                if (rssLcSampleSimples != null && rssLcSampleSimples.size() != 0) {
                    for (RssLcSampleSimple rssLcSampleSimple : rssLcSampleSimples) {
                        trainingData = transLcSampleToTrainingData(rssLcSampleSimple);

                    }
                }
                break;
            case "cd":
                QueryWrapper<RssCdSampleSimple> queryWrapperCd = new QueryWrapper<>();
                queryWrapperCd.eq("dataset_id", collectionIdInt);
                queryWrapperCd.eq("rcs.id",trainingDataIdInt);
                String combinationParamCd = "";
                List<RssCdSampleSimple> rssCdSampleSimples = rssPostgresqlService.getCdSamples(queryWrapperCd, combinationParamCd);
                if (rssCdSampleSimples != null && rssCdSampleSimples.size() != 0) {
                    for (RssCdSampleSimple rssCdSampleSimple : rssCdSampleSimples) {
                        trainingData = transCdSampleToTrainingData(rssCdSampleSimple);

                    }
                }
                break;
            default:
                break;
        }

        return trainingData;
    }


    public HttpServletResponse getImage(HttpServletResponse response,String collectionId,String trainingDataId){
        Integer collectionIdInt=Integer.parseInt(collectionId);
        Integer trainingDataIdInt=Integer.parseInt(trainingDataId);
        //获得数据集任务类型
        List<RssDataset> rssDatasets =rssQuery.queryDatasetDetail(collectionId);
        String taskType="";
        if (rssDatasets.size()!=0){
            taskType=rssDatasets.get(0).getTaskType();
        }
        System.out.println("taskType:"+taskType);
        String bucketName=null;
        String imagePath=null;
        switch (taskType){
            case "lc":
                bucketName="land-cover";
                QueryWrapper<RssLcSampleSimple> queryWrapperLc = new QueryWrapper<>();
                queryWrapperLc.eq("dataset_id", collectionIdInt);
                queryWrapperLc.eq("rls.id",trainingDataIdInt);
                String combinationParamLc = "";
                List<RssLcSampleSimple> rssLcSampleSimples = rssPostgresqlService.getLcSamples(queryWrapperLc, combinationParamLc);
                if (rssLcSampleSimples != null && rssLcSampleSimples.size() != 0) {
                    for (RssLcSampleSimple rssLcSampleSimple : rssLcSampleSimples) {
                        imagePath=rssLcSampleSimple.getImagePath().toString().replace("land-cover/","");
                        System.out.println("imagePath:"+imagePath);
                    }
                }
                break;
            case "od":
                bucketName="object-detection";
                QueryWrapper<RssOdSampleSimple> queryWrapperOd =new QueryWrapper<>();
                queryWrapperOd.eq("dataset_id",collectionIdInt);
                queryWrapperOd.eq("ros.id",trainingDataIdInt);
                String combinationParam="";
                List<RssOdSampleSimple> rssOdSampleSimples=rssPostgresqlService.getOdSamples(queryWrapperOd,combinationParam);
                if (rssOdSampleSimples!=null&&rssOdSampleSimples.size()!=0){
                    for (RssOdSampleSimple rssOdSampleSimple:rssOdSampleSimples){
                        //获得对应的影像数据
                        imagePath=rssOdSampleSimple.getImagePath();
                    }
                }
                break;
            case "sc":
                bucketName="scene-classification";
                QueryWrapper<RssScSampleSimple> queryWrapperSc = new QueryWrapper<>();
                queryWrapperSc.eq("dataset_id", collectionIdInt);
                queryWrapperSc.eq("scsample.id",trainingDataIdInt);
                String combinationParamSc = "";
                List<RssScSampleSimple> rssScSampleSimples = rssPostgresqlService.getScSamples(queryWrapperSc, combinationParamSc);
//                System.out.println("rssScSampleSimples.size():"+rssScSampleSimples.size());
                if (rssScSampleSimples != null && rssScSampleSimples.size() != 0) {
                    for (RssScSampleSimple rssScSampleSimple : rssScSampleSimples) {
                        //获得对应的影像数据
//                        System.out.println("rssScSampleSimple.getImagePath():"+rssScSampleSimple.getImagePath());
                        imagePath=rssScSampleSimple.getImagePath().toString().replace("scene-classification/","");
//                        System.out.println("imagePath:"+imagePath);
                    }
                }
                break;
            default:
                break;
        }
        try {
            //获取存储捅名
            //获取对象信息和对象的元数据。
            ObjectStat objectStat = rssMinioUtil.statObject(bucketName, imagePath);
            //setContentType 设置发送到客户机的响应的内容类型
            response.setContentType(objectStat.contentType());
            //设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(objectStat.name(), "UTF-8"));
            //文件流
            InputStream object = rssMinioUtil.getObject(bucketName, imagePath);
            //设置文件大小
            response.setHeader("Content-Length", String.valueOf(objectStat.length()));
            IOUtils.copy(object, response.getOutputStream());
            //关闭流
            object.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return response;
    }


    public List<AITrainingData.AILabel> getLabels(String collectionId,String trainingDataId){
        List<AITrainingData.AILabel> labels=new LinkedList<>();

        Integer collectionIdInt=Integer.parseInt(collectionId);
        Integer trainingDataIdInt=Integer.parseInt(trainingDataId);
        //获得数据集任务类型
        List<RssDataset> rssDatasets =rssQuery.queryDatasetDetail(collectionId);
        String taskType="";
        if (rssDatasets.size()!=0){
            taskType=rssDatasets.get(0).getTaskType();

        }
        switch (taskType){
            case "od":
                String bucketName="object-detection";
                QueryWrapper<RssOdSampleSimple> queryWrapperOd =new QueryWrapper<>();
                queryWrapperOd.eq("dataset_id",collectionIdInt);
                queryWrapperOd.eq("ros.id",trainingDataIdInt);
                String combinationParam="";
                List<RssOdSampleSimple> rssOdSampleSimples=rssPostgresqlService.getOdSamples(queryWrapperOd,combinationParam);
                if (rssOdSampleSimples!=null&&rssOdSampleSimples.size()!=0){
                    for (RssOdSampleSimple rssOdSampleSimple:rssOdSampleSimples){
                        //获得对应的标签数据
                        String labelPath=rssOdSampleSimple.getLabelPath();
                        String labelPathUnit=labelPath.replace("label_hbb","label_unit_hbb");
                        InputStream inputStream = rssMinioUtil.getObject(bucketName, labelPathUnit);
                        RssOdAnn rssOdAnn=rssFileUtil.parseOdAXML(inputStream);
                        ArrayList<RssOdAnn.Object> objects=rssOdAnn.getObject();
                        if (objects!=null){
                            for (RssOdAnn.Object object:objects){
                                AITrainingData.AILabel label =transOdSampleToLabel(object);
                                labels.add(label);
                            }
                        }
                    }
                }
                break;
            case "sc":
                QueryWrapper<RssScSampleSimple> queryWrapperSc = new QueryWrapper<>();
                queryWrapperSc.eq("dataset_id", collectionIdInt);
                queryWrapperSc.eq("scsample.id",trainingDataIdInt);
                String combinationParamSc = "";
                List<RssScSampleSimple> rssScSampleSimples = rssPostgresqlService.getScSamples(queryWrapperSc, combinationParamSc);
                if (rssScSampleSimples != null && rssScSampleSimples.size() != 0) {
                    for (RssScSampleSimple rssScSampleSimple : rssScSampleSimples) {
                        AITrainingData.AILabel label=transScSampleToLabel(rssScSampleSimple);
                        labels.add(label);
                    }
                }
                break;
            case "lc":
                QueryWrapper<RssLcSampleSimple> queryWrapperLc = new QueryWrapper<>();
                queryWrapperLc.eq("dataset_id", collectionIdInt);
                queryWrapperLc.eq("rls.id",trainingDataIdInt);
                String combinationParamLc = "";
                List<RssLcSampleSimple> rssLcSampleSimples = rssPostgresqlService.getLcSamples(queryWrapperLc, combinationParamLc);
                if (rssLcSampleSimples != null && rssLcSampleSimples.size() != 0) {
                    for (RssLcSampleSimple rssLcSampleSimple : rssLcSampleSimples) {
                        AITrainingData.AILabel label=transLcSampleToLabel(rssLcSampleSimple);
                        labels.add(label);
                    }
                }
            default:
                break;
        }
        return labels;
    }
    public AITrainingData.AILabel transScSampleToLabel(RssScSampleSimple rssScSampleSimple){
        AITrainingData.AILabel label=new AITrainingData.AILabel();
        String className=rssScSampleSimple.getClassNames();
        label.setClassName(className);
        label.setIsNegative(false);
        return label;
    }

    /**
     * 获取 Geo 几何类型
     * @param wktStr WKT 字符串
     * @return Geo 几何类型
     */
    public static String getGeometryType(String wktStr) {
        String type = null;
        if (StrUtil.isNotEmpty(wktStr)) {
            try {
                Geometry read = READER.read(wktStr);
                type = read.getGeometryType();
            }catch (Exception e) {
                System.out.println("非规范 WKT 字符串："+ e);
                e.printStackTrace();
            }
        }
        return type;
    }
    public AITrainingData.AILabel transOdSampleToLabel(RssOdAnn.Object object) {
        AITrainingData.AILabel label=new AITrainingData.AILabel();
        JSONObject jsonObject = new JSONObject();
        try {
            String className=object.getName();
            label.setClassName(className);
            label.setIsNegative(false);
            RssOdAnn.Object.Bndbox bndbox=object.getBndbox();
            String wkt=rssGsUtil.bndBoxToWKT(bndbox);
            WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(wkt);
//        Feature feature= GeoTools.wktToFeature(wkt);
            SimpleFeatureType type = DataUtilities.createType("Link", "geometry:"+getGeometryType(wkt));
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
            // 按照TYPE中声明的顺序为属性赋值就可以，其他方法我暂未尝试
            featureBuilder.add(READER.read(wkt));
            SimpleFeature feature = featureBuilder.buildFeature(null);
            StringWriter writer = new StringWriter();
            FeatureJSON fJson = new FeatureJSON();
            fJson.writeFeature(feature, writer);
            jsonObject = JSONUtil.parseObj(writer.toString());
            label.setObject(jsonObject);
            String difficult=object.getDifficult();
            if (difficult!=null){
                if (difficult.equals("0")){
                    label.setIsDiffDetectable(false);
                }else if (difficult.equals("1")){
                    label.setIsDiffDetectable(true);
                }
            }
            label.setGeometryType("polygon");
        }catch (ParseException e ){
            e.printStackTrace();
        }catch (SchemaException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

        return label;
    }
    public AITrainingData.AILabel transLcSampleToLabel(RssLcSampleSimple rssLcSampleSimple){
        AITrainingData.AILabel label=new AITrainingData.AILabel();
        String className=rssLcSampleSimple.getClassNames();
        label.setClassName(className);
//        label.setImageURL(rssLcSampleSimple.getImagePath().toString());
        if (rssLcSampleSimple.getLabelPath()!=null){
            label.setImageURL(rssLcSampleSimple.getLabelPath().toString());
        }
        label.setIsNegative(false);
        return label;
    }

    public AITrainingData transScSampleToTrainingData(RssScSampleSimple rssScSampleSimple) {
        AITrainingData trainingData = new AITrainingData();

        trainingData.setId(rssScSampleSimple.getId().toString());
        Integer trnValueTest = rssScSampleSimple.getTrnValueTest();
        String trainingTypeCode = "training";
        if (trnValueTest != null) {
            switch (trnValueTest) {
                case 0:
                    trainingTypeCode = "training";
                    break;
                case 1:
                    trainingTypeCode = "validation";
                    break;
                case 2:
                    trainingTypeCode = "test";
                    break;
                default:
                    break;
            }
        }

        trainingData.setTrainingTypeCode(trainingTypeCode);
        String[] instruments = {rssScSampleSimple.getInstrument()};
        trainingData.setDataSourceId(instruments);
        Integer imageChannels = rssScSampleSimple.getImageChannels();
        AITrainingData.Size size = new AITrainingData.Size();
        size.setDepth(imageChannels);
        size.setHeight(rssScSampleSimple.getSampleHeight());
        size.setWidth(rssScSampleSimple.getSampleWidth());
        ArrayList<AITrainingData.AILabel> labels=new ArrayList<>();
        AITrainingData.AILabel label=new AITrainingData.AILabel();
        label=transScSampleToLabel(rssScSampleSimple);
        labels.add(label);
        trainingData.setLabels(labels);
//        trainingData.setSize(size);
        if (rssScSampleSimple.getImagePath()!=null){
            String imagePath=rssScSampleSimple.getImagePath().toString();
            trainingData.setDataUrl(imagePath);
        }
        return trainingData;
    }
    public AITrainingData transLcSampleToTrainingData(RssLcSampleSimple rssLcSampleSimple) {
        AITrainingData trainingData = new AITrainingData();
        trainingData.setId(rssLcSampleSimple.getId().toString());
//        String obnum = rssOdSampleSimple.getObjectNums();
//        System.out.println("obnum:"+obnum);
//        int amount = 0;
//        if (obnum != null && !obnum.isEmpty()) {
//            String[] nums = rssOdSampleSimple.getObjectNums().split(",");
//            int[] numsInt = StringToInt(nums);
//            amount = getSum(numsInt);
//        }
//        trainingData.setNumberOfLabels(amount);
        Integer trnValueTest = rssLcSampleSimple.getTrnValueTest();
        String trainingTypeCode = "training";
        if (trnValueTest != null) {
            switch (trnValueTest) {
                case 0:
                    trainingTypeCode = "training";
                    break;
                case 1:
                    trainingTypeCode = "validation";
                    break;
                case 2:
                    trainingTypeCode = "test";
                    break;
                default:
                    break;
            }
        }
        trainingData.setTrainingTypeCode(trainingTypeCode);
        String[] instruments = {rssLcSampleSimple.getInstrument()};
        trainingData.setDataSourceId(instruments);
        Integer imageChannels = rssLcSampleSimple.getImageChannels();
        AITrainingData.Size size = new AITrainingData.Size();
        size.setDepth(imageChannels);
        size.setHeight(rssLcSampleSimple.getSampleHeight());
        size.setWidth(rssLcSampleSimple.getSampleWidth());
        ArrayList<AITrainingData.AILabel> labels=new ArrayList<>();
        AITrainingData.AILabel label=new AITrainingData.AILabel();
        label=transLcSampleToLabel(rssLcSampleSimple);
        labels.add(label);
        trainingData.setLabels(labels);
//        String imagePathObject=rssLcSampleSimple.getImagePath().toString();
//        System.out.println("imagePathObject"+imagePathObject);
//        trainingData.setSize(size);
        if (rssLcSampleSimple.getImagePath()!=null){
            String imagePath=rssLcSampleSimple.getImagePath().toString();
            trainingData.setDataUrl(imagePath);
        }

//        System.out.println("rssLcSampleSimple.getImagePath():"+rssLcSampleSimple.getImagePath());
        return trainingData;
    }

    public AITrainingData transCdSampleToTrainingData(RssCdSampleSimple rssCdSampleSimple) {
        AITrainingData trainingData = new AITrainingData();

        trainingData.setId(rssCdSampleSimple.getId().toString());
//        String obnum = rssOdSampleSimple.getObjectNums();
//        System.out.println("obnum:"+obnum);
//        int amount = 0;
//        if (obnum != null && !obnum.isEmpty()) {
//            String[] nums = rssOdSampleSimple.getObjectNums().split(",");
//            int[] numsInt = StringToInt(nums);
//            amount = getSum(numsInt);
//        }
//        trainingData.setNumberOfLabels(amount);
        Integer trnValueTest = rssCdSampleSimple.getTrnValueTest();
        String trainingTypeCode = "training";
        if (trnValueTest != null) {
            switch (trnValueTest) {
                case 0:
                    trainingTypeCode = "training";
                    break;
                case 1:
                    trainingTypeCode = "validation";
                    break;
                case 2:
                    trainingTypeCode = "test";
                    break;
                default:
                    break;
            }
        }

        trainingData.setTrainingTypeCode(trainingTypeCode);
        String postInstrument = "postInstrument:" + rssCdSampleSimple.getPostInstrument();
        String preInstrument = "preInstrument:" + rssCdSampleSimple.getPreInstrument();
        String[] instruments = {preInstrument,postInstrument};
        trainingData.setDataSourceId(instruments);
        Integer imageChannels = rssCdSampleSimple.getPostImageChannels();
        AITrainingData.Size size = new AITrainingData.Size();
        size.setDepth(imageChannels);
        size.setHeight(rssCdSampleSimple.getSampleHeight());
        size.setWidth(rssCdSampleSimple.getSampleWidth());
//        trainingData.setSize(size);
        if (rssCdSampleSimple.getPreImagePath()!=null&&rssCdSampleSimple.getPostImagePath()!=null){
            String preImagePathStr=rssCdSampleSimple.getPreImagePath().toString();
            String postImagePathStr=rssCdSampleSimple.getPostImagePath().toString();
            System.out.println("preImagePathStr:"+preImagePathStr);
            System.out.println("postImagePathStr:"+postImagePathStr);
            String preImagePath = "preImagePath:" + preImagePathStr;
            String postImagePath = "postImagePath:" + postImagePathStr;
            String dataUrl=preImagePath +","+ postImagePath;
            trainingData.setDataUrl(dataUrl);
        }

        return trainingData;
    }
    public AITrainingData transOdSampleToTrainingData(RssOdSampleSimple rssOdSampleSimple){
        AITrainingData trainingData=new AITrainingData();

        trainingData.setId(rssOdSampleSimple.getId().toString());
        String obnum=rssOdSampleSimple.getObjectNums();
//        System.out.println("obnum:"+obnum);
        int amount=0;
        if (obnum!=null&&!obnum.isEmpty()){
            String[] nums=rssOdSampleSimple.getObjectNums().split(",");
            int[] numsInt=StringToInt(nums);
            amount=getSum(numsInt);
        }
        trainingData.setNumberOfLabels(amount);
        Integer trnValueTest=rssOdSampleSimple.getTrnValueTest();
        String trainingTypeCode="training";
        if (trnValueTest!=null){
            switch (trnValueTest){
                case 0:
                    trainingTypeCode="training";
                    break;
                case 1:
                    trainingTypeCode="validation";
                    break;
                case 2:
                    trainingTypeCode="test";
                    break;
                default:
                    break;
            }
        }

        trainingData.setTrainingTypeCode(trainingTypeCode);
        String[] instruments={rssOdSampleSimple.getInstrument()};
        trainingData.setDataSourceId(instruments);
        Integer imageChannels=rssOdSampleSimple.getImageChannels();
        AITrainingData.Size size=new AITrainingData.Size();
        size.setDepth(imageChannels);
        size.setHeight(rssOdSampleSimple.getSampleHeight());
        size.setWidth(rssOdSampleSimple.getSampleWidth());
//        trainingData.setSize(size);
        String imagePath=rssOdSampleSimple.getImagePath();
        trainingData.setDataUrl(imagePath);
        return trainingData;
    }
    public int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
    // 求和方法
    public  int getSum(int intArr[]) { // 传参intArr数组
        int sum = 0;
        for (int i = 0; i < intArr.length; i++) {
            sum += intArr[i];
        }
        return sum; // 返回sum
    }
    public AITrainingDataCollection getCollectionById(String collectionId){
        Integer collectionIdInt=Integer.parseInt(collectionId);
        //获得数据集类别
        List<AITrainingDataClassMap> trainingDataClassMaps=new LinkedList<>();
        HashMap<Integer,String[]> datasetIdClassesArrMap=new HashMap<>();
        trainingDataClassMaps=rssPostgresqlService.getDatasetIdClasses();
        for (int i=0;i<trainingDataClassMaps.size();i++){
            AITrainingDataClassMap datasetIdClassesMap=trainingDataClassMaps.get(i);
            String[] classPairs=datasetIdClassesMap.getClasses().split(",");
            String[] classes=new String[classPairs.length];
            for (int j=0;j<classPairs.length;j++){
                String[] classPair=classPairs[j].split("~");
                if (classPair.length==2){
                    classes[j]=classPair[1];
                }
            }
            datasetIdClassesArrMap.put(datasetIdClassesMap.getDatasetId(),classes);
        }
        QueryWrapper<RssDataset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",collectionIdInt);
        List<RssDataset> rssDatasets=rssPostgresqlService.getCollections(queryWrapper);
        AITrainingDataCollection trainingDataCollection=new AITrainingDataCollection();
        if (rssDatasets!=null&&rssDatasets.size()!=0){
            for (RssDataset rssDataset:rssDatasets){
                if (rssDataset.getId()==collectionIdInt){
                    trainingDataCollection=transDatasetToCollection(rssDataset,datasetIdClassesArrMap);
                }
            }
        }
        return trainingDataCollection;
    }



    public AITrainingDataCollection transDatasetToCollection(RssDataset rssDataset, HashMap<Integer,String[]> datasetIdClassesArrMap){
        AITrainingDataCollection trainingDataCollection=new AITrainingDataCollection();
        trainingDataCollection.setId(rssDataset.getId().toString());
        if (rssDataset.getDescription()!=null){
            trainingDataCollection.setDescription(rssDataset.getDescription().toString());
        }
        trainingDataCollection.setVersion(rssDataset.getDatasetVersion());
        if (rssDataset.getKeyword()!=null){
            trainingDataCollection.setKeywords(rssDataset.getKeyword().split(","));
        }
        if (rssDataset.getDatasetCopy()!=null){
            trainingDataCollection.setLicence(rssDataset.getDatasetCopy().toString());
        }
        trainingDataCollection.setName(rssDataset.getName());
        if (rssDataset.getSampleSum()!=null){
            trainingDataCollection.setAmountOfTrainingData(rssDataset.getSampleSum());
        }
        String taskType=rssDataset.getTaskType();
        AITrainingDataCollection.Task task=new AITrainingDataCollection.Task();
        if (rssDataset.getDatasetCopy()!=null){
            String providerName=rssDataset.getDatasetCopy().toString();
            List<AITrainingDataCollection.Provider> providers=new LinkedList<>();
            AITrainingDataCollection.Provider provider=new AITrainingDataCollection.Provider();
            provider.setName(providerName);
            providers.add(provider);
            trainingDataCollection.setProviders(providers);
        }
        if (taskType!=null){
            switch (taskType){
                case "od":
//                    trainingDataCollection.setLabelType("xml");
                    task.setTaskType("object");
                    break;
                case "sc":
                    task.setDescription("Structural high-resolution satellite image indexing");
//                    trainingDataCollection.setLabelType("text");
                    task.setTaskType("scene");
                    break;
                case "lc":
//                    trainingDataCollection.setLabelType("png");
                    task.setTaskType("pixel");
                    break;
                case "cd":
//                    trainingDataCollection.setLabelType("png");
                    task.setTaskType("pixel");
                    break;
                default:
                    break;
            }
            trainingDataCollection.setTask(task);
        }
        Date createdDate= rssDataset.getCreateDate();
        if (createdDate!=null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(createdDate);
            Integer year = calendar.get(Calendar.YEAR);
            Integer mouth=calendar.get(Calendar.MONTH);					//获取月份
            Integer date=calendar.get(Calendar.DATE);					//获取日
            String createdDateStr=String.valueOf(year);
            if ((mouth!=0||date!=1)){
                createdDateStr=createdDateStr+"-"+String.format("%02d",(mouth+1))+"-"+String.format("%02d",(date));
            }
            trainingDataCollection.setCreatedTime(createdDateStr);
        }
        Date updatedDate=rssDataset.getUpdateTime();
        if (updatedDate!=null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(updatedDate);
            Integer year = calendar.get(Calendar.YEAR);
            Integer mouth=calendar.get(Calendar.MONTH);					//获取月份
            Integer date=calendar.get(Calendar.DATE);					//获取日
            String updatedDateStr=String.valueOf(year);
            if ((mouth!=0||date!=1)){
                updatedDateStr=updatedDateStr+"-"+String.format("%02d",(mouth+1))+"-"+String.format("%02d",(date));
            }
            trainingDataCollection.setUpdatedTime(updatedDateStr    );
        }
        try {
            InetAddress ia=InetAddress.getLocalHost();
//        String localname=ia.getHostName();
            String localIp=ia.getHostAddress();
            trainingDataCollection.setData("http://"+localIp+":18066/geois-boot/ogc/collections/"+rssDataset.getId().toString()+"/trainingData");
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
        String instrumentsStr=rssDataset.getInstrument();
        if (instrumentsStr!=null&&!instrumentsStr.equals("")){
            String[] instruments=rssDataset.getInstrument().split(",");
            String imageType=rssDataset.getImageType();
            List<AITrainingDataCollection.DataSource> dataSources=new ArrayList<>();
            if (instruments.length!=0){
                for (int i=0;i<instruments.length;i++){
                    AITrainingDataCollection.DataSource dataSource=new AITrainingDataCollection.DataSource();
                    AITrainingDataCollection.DataSource.SourceCitation sourceCitation=new AITrainingDataCollection.DataSource.SourceCitation();
                    sourceCitation.setTitle(instruments[i]);
                    dataSource.setSourceCitation(sourceCitation);
                    dataSource.setId(instruments[i]);
                    dataSource.setDataType(imageType);
                    dataSource.setSensor(instruments[i]);
                    dataSources.add(dataSource);

                }
            }
            trainingDataCollection.setDataSources(dataSources);
        }

        trainingDataCollection.setClasses(datasetIdClassesArrMap.get(rssDataset.getId()));
        return trainingDataCollection;
    }

}
