package cn.whu.geois.modules.rssample.service.impl;

import cn.whu.geois.modules.rssample.entity.*;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingDataClassMap;
import cn.whu.geois.modules.rssample.mapper.RssPostgresqlMapper;
import cn.whu.geois.modules.rssample.service.RssPostgresqlService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/2/2 11:38
 */
@Service
@DS("postgres")
public class RssPostgresqlServiceImpl implements RssPostgresqlService {
    @Resource
    private RssPostgresqlMapper rssPostgresqlMapper;

//    @Override
//    public Integer getSCClassNum(Integer datasetId, String uniClassId){
//        return rssPostgresqlMapper.getSCClassNum(datasetId,uniClassId);
//    }
//
//    @Override
//    public Integer getODClassNum(Integer datasetId, String uniClassId){
//        return rssPostgresqlMapper.getODClassNum(datasetId,uniClassId);
//    }
//    @Override
//    public List<HashMap<String,Integer>> getSCClassNum(Integer datasetId){
//        return rssPostgresqlMapper.getSCClassNum(datasetId);
//    }
//    @Override
//    public List<HashMap<String,Integer>> getODClassNum(Integer datasetId){
//        return rssPostgresqlMapper.getODClassNum(datasetId);
//    }
//
    @Override
    public Integer getMaxCdSampleId(){
    return rssPostgresqlMapper.getMaxCdSampleId();
}
    @Override
    public Integer getMaxCdSampleClassId(){
        return rssPostgresqlMapper.getMaxCdSampleClassId();
    }
    @Override
    public String getLcClassCode(String name) {
        return rssPostgresqlMapper.getLcClassCode(name);
    }
    @Override
    public Integer getMaxClassMapId() {
        return rssPostgresqlMapper.getMaxClassMapId();
    }
    @Override
    public Integer getDatasetCount(){return rssPostgresqlMapper.getDatasetCount();}
    @Override
    public Integer getDataCount(){
        String[] taskTypeList = {"od","td","lc","cd"};
        Integer sumNum = 0;
        for(int i = 0;i < taskTypeList.length;i++){
            sumNum = sumNum + rssPostgresqlMapper.getDataCount(taskTypeList[i]);
        }
        sumNum = sumNum + rssPostgresqlMapper.getSCDataCount();
        return sumNum;
    }
    /**
     * 获取场景检测样本总数
     * @return
     */
    @Override
    public Integer getSCDataCount() {
        return rssPostgresqlMapper.getSCDataCount();
    }

    /**
     * 获取目标检测目样本中目标总数
     * @return
     */
    @Override
    public Integer getODDataTargetSum() {
        return rssPostgresqlMapper.getODDataTargetSum();
    }

    /**
     * 获取样本总数，尺寸 除以 （512 * 512）
     * 地物分类、变化检测、多视三维
     * 通过替换任务类型
     */
    @Override
    public Integer getDataCountByWidthHeigth(String taskType) {
        return rssPostgresqlMapper.getDataCount(taskType);
    }


    @Override
    public List<HashMap<String,String>> getSCClassNum(Integer datasetId){
        return rssPostgresqlMapper.getSCClassNum(datasetId);
    }
    @Override
    public List<HashMap<String,String>> getODClassNum(Integer datasetId){
        return rssPostgresqlMapper.getODClassNum(datasetId);
    }


    @Override
    public List<RssOdClass> getOdClass(String combinationParam){
        return rssPostgresqlMapper.getOdClass(combinationParam);
    }
    @Override
    public List<RssScClass> getScClass(String combinationParam){
        return rssPostgresqlMapper.getScClass(combinationParam);
    }
    @Override
    public List<RssGqjcClass> getGqjcClass(String combinationParam){
        return rssPostgresqlMapper.getGqjcClass(combinationParam);
    }
    @Override
    public boolean updateDatasetImage(String field,String base,String datasetName,String version){
        return rssPostgresqlMapper.updateDatasetImage(field,base,datasetName,version);
    }
    @Override
    public boolean updateDatasetSize(Integer datasetId,Double size){
        return rssPostgresqlMapper.updateDatasetSize(datasetId,size);
    }
    @Override
    public boolean updateOrderInfo(Integer tradeStatus,String downloadUrl,String orderNum){
        return rssPostgresqlMapper.updateOrderInfo(tradeStatus,downloadUrl,orderNum);
    }
    @Override
    public boolean updateSampleThumb(Object minioRenderPngPath,String minioImagePath){
        return rssPostgresqlMapper.updateSampleThumb(minioRenderPngPath,minioImagePath);
    }


    @Override
    public Integer getMaxOdSampleId(){
        return rssPostgresqlMapper.getMaxOdSampleId();
    }
    @Override
    public Integer getMaxOdSampleClassId(){
        return rssPostgresqlMapper.getMaxOdSampleClassId();
    }
    @Override
    public Integer getMaxOrderInfoId(){return rssPostgresqlMapper.getMaxOrderInfoId();}
    @Override
    public Integer getMaxOrderItemId(){return rssPostgresqlMapper.getMaxOrderItemId();}
    @Override
    public boolean insertRssOdSampleBatch(List<RssOdSample> rssOdSampleList){
        String list="";
        long startTime=System.currentTimeMillis();
        System.out.println("=====od_sample 开始插入共"+rssOdSampleList.size()+"个数据=====");
        for (int i = 0; i < rssOdSampleList.size() ; i++) {
            RssOdSample rssOdSample=rssOdSampleList.get(i);
            Integer id=rssOdSample.getId();
            Integer datasetId=rssOdSample.getDatasetId();
            Integer sampleWidth=rssOdSample.getSampleWidth();
            Integer sampleHeight=rssOdSample.getSampleHeight();
            String sampleArea=rssOdSample.getSampleArea().toString();
            Timestamp sampleDate=new Timestamp(rssOdSample.getSampleDate().getTime());
            String sampleQuality=rssOdSample.getSampleQuality();
            String sampleLabeler=rssOdSample.getSampleLabeler();
            Timestamp annotationDate=new Timestamp(rssOdSample.getAnnotationDate().getTime());
            String lableBbox=rssOdSample.getLabelBbox();
            String imagePath=rssOdSample.getImagePath().toString();
            String lablePath="";
            if (rssOdSample.getLabelPath()!=null){
                lablePath=rssOdSample.getLabelPath().toString();
            }
            String imageType=rssOdSample.getImageType();
            Integer imageChannels=rssOdSample.getImageChannels();
            String imageResolution=rssOdSample.getImageResolution();
            String instrument="";
            Object instrumentOb=rssOdSample.getInstrument();
            if (instrumentOb!=null){
                instrument=instrumentOb.toString();
            }
            Integer trnValueTest=rssOdSample.getTrnValueTest();
            String createBy=rssOdSample.getCreateBy();
            Timestamp createTime=new Timestamp(rssOdSample.getCreateTime().getTime());
            String updateBy=rssOdSample.getUpdateBy();
            String updateTimeStr=null;
            if (rssOdSample.getUpdateTime()!=null){
                Timestamp updateTime=new Timestamp(rssOdSample.getUpdateTime().getTime());
                updateTimeStr="'"+updateTime+"'";
            }
            String subSql="("+
                    id+","+
                    datasetId+","+
                    sampleWidth+","+
                    sampleHeight+","+
                    sampleArea+","+
                    "'"+sampleDate+"',"+
                    "'"+sampleQuality+"',"+
                    "'"+sampleLabeler+"',"+
                    "'"+annotationDate+"',"+
                    "'"+lableBbox+"',"+
                    "'"+imagePath+"',"+
                    "'"+lablePath+"',"+
                    "'"+imageType+"',"+
                    imageChannels+","+
                    "'"+imageResolution+"',"+
                    "'"+instrument+"',"+
                    trnValueTest+","+
                    "'"+createBy+"',"+
                    "'"+createTime+"',"+
                    "'"+updateBy+"',"+
                    updateTimeStr+
                    ")";
            if (i < rssOdSampleList.size()-1) {
                subSql=subSql+",";
            }
            list=list+subSql;
        }
//        System.out.println(list);
        boolean insert= rssPostgresqlMapper.insertRssOdSampleBatch(list);
        long endTime=System.currentTimeMillis();
        System.out.println("总共插入"+rssOdSampleList.size()+"个耗时"+(endTime-startTime));
        return insert;
    }
    public List<RssOdSample> queryOdSampleDetail(String combinationParam){
        return rssPostgresqlMapper.queryOdSampleDetail(combinationParam);
    }public List<RssScSample> getScSample(String combinationParam){
        return rssPostgresqlMapper.getScSample(combinationParam);
    }

    @Override
    public String getScClassCode(String name) {
        return rssPostgresqlMapper.getScClassCode(name);
    }

    @Override
    public Integer getDatasetID(String name) {
        return rssPostgresqlMapper.getDatasetID(name);
    }

    @Override
    public Integer getMaxScSampleID() {
        return rssPostgresqlMapper.getMaxScSampleID();
    }
    @Override
    public List<RssDataset> getDatasetDetail(String combinationParam){
        return rssPostgresqlMapper.getDatasetDetail(combinationParam);
    }
    @Override
    public IPage<RssDatasetSimple> getDatasets(IPage<RssDatasetSimple> page, QueryWrapper<RssDatasetSimple> queryWrapper){
        return rssPostgresqlMapper.getDatasets(page,queryWrapper);
    }

    @Override
    public IPage<RssOrderInfo> getOrderInfo(IPage<RssOrderInfo> page, QueryWrapper<RssOrderInfo> queryWrapper){
        return rssPostgresqlMapper.getOrderInfo(page,queryWrapper);
    }
    @Override
    public RssFilter getOrderInfoFilter(QueryWrapper<String> queryWrapper){
        List<String> tradeStatusesStr=rssPostgresqlMapper.getOrderInfoDistinct(queryWrapper,"trade_status");
        List<String> userIds=rssPostgresqlMapper.getOrderInfoDistinct(queryWrapper,"user_id");
        List<Integer> tradeStatuses=new LinkedList<>();
        for (String tradeStatusStr:tradeStatusesStr){
            tradeStatuses.add(Integer.parseInt(tradeStatusStr));
        }
        RssFilter rssFilter=new RssFilter();
        rssFilter.setTradeStatus(tradeStatuses);
        rssFilter.setUserId(userIds);
        return rssFilter;
    }
    @Override
    public IPage<RssOrderItem> getOrderItem(IPage<RssOrderItem> page, QueryWrapper<RssOrderItem> queryWrapper){
        return rssPostgresqlMapper.getOrderItem(page,queryWrapper);
    }
    @Override
    public List<RssPair> getOrderItemPath( QueryWrapper<String> queryWrapper,String taskType,String columns){
        return rssPostgresqlMapper.getOrderItemPath(queryWrapper,taskType,columns);
    }
    @Override
    public String getOrderInfoTaskType( QueryWrapper<String> queryWrapper){
        return rssPostgresqlMapper.getOrderInfoTaskType(queryWrapper);
    }
    @Override
    public String getOrderInfoDownloadUrl( QueryWrapper<String> queryWrapper){
        return rssPostgresqlMapper.getOrderInfoDownloadUrl(queryWrapper);
    }
    @Override
    public List<RssDatasetSimple> listDatasets(){
        return rssPostgresqlMapper.listDatasets();
    }

    @Override
    public List<RssDataset> getCollections(QueryWrapper<RssDataset> queryWrapper){return rssPostgresqlMapper.getCollections(queryWrapper);}

    @Override
    public List<AITrainingDataClassMap> getDatasetIdClasses(){return rssPostgresqlMapper.getDatasetIdClasses();}

    @Override
    public  IPage<RssOdSampleSimple> getOdSamples(IPage<RssOdSampleSimple> page, QueryWrapper<RssOdSampleSimple> queryWrapper,String combinationParam){
        return rssPostgresqlMapper.getOdSamples(page,queryWrapper,combinationParam);
    }
    @Override
    public  List<RssOdSampleSimple> getOdSamples( QueryWrapper<RssOdSampleSimple> queryWrapper,String combinationParam){
        return rssPostgresqlMapper.getOdSamples(queryWrapper,combinationParam);
    }
    @Override
    public RssFilter getOdSamplesFilter(QueryWrapper<String> queryWrapper,String combinationParam){
        List<String> instruments=rssPostgresqlMapper.getOdDistinct(queryWrapper,combinationParam,"ros.instrument");
        List<String> datasetIdsStr=rssPostgresqlMapper.getOdDistinct(queryWrapper,combinationParam,"dataset_id");
        List<String> idsStr=rssPostgresqlMapper.getOdDistinct(queryWrapper,combinationParam,"ros.id");
//        List<String> classNames=rssPostgresqlMapper.getOdDistinct(queryWrapper,combinationParam,"c2.name as class_name");
        List<String> classCodes=rssPostgresqlMapper.getOdDistinct(queryWrapper,combinationParam,"c2.code as class_code");
        List<Integer> ids=new LinkedList<>();
        for (String idStr:idsStr){
            ids.add(Integer.parseInt(idStr));
        }
        List<Integer> datasetIds=new LinkedList<>();
        for (String datasetIdStr:datasetIdsStr){
            datasetIds.add(Integer.parseInt(datasetIdStr));
        }
//        List<String> classNamesNew=new LinkedList<>();
//        for (String className:classNames){
//            if (className!=null){
//                classNamesNew.add(className);
//            }
//        }
        List<String> classCodesNew=new LinkedList<>();
        for (String classCode:classCodes){
            if (classCode!=null){
                classCodesNew.add(classCode);
            }
        }
        RssFilter rssFilter=new RssFilter();
        rssFilter.setDatasetIds(datasetIds);
        rssFilter.setIds(ids);
        rssFilter.setInstruments(instruments);
//        rssFilter.setUniCLassNames(classNamesNew);
        rssFilter.setUniCLassCodes(classCodesNew);
        return rssFilter;
    }
    @Override
    public List<RssScSample> queryScSampleDetail(String combinationParam){
        return rssPostgresqlMapper.queryScSampleDetail(combinationParam);
    }
    @Override
    public IPage<RssScSampleSimple> getScSamples(IPage<RssScSampleSimple> page, QueryWrapper<RssScSampleSimple> queryWrapper, String combinationParam) {
        return rssPostgresqlMapper.getScSamples(page,queryWrapper,combinationParam);
    }
    @Override
    public  List<RssScSampleSimple> getScSamples( QueryWrapper<RssScSampleSimple> queryWrapper,String combinationParam){
        return rssPostgresqlMapper.getScSamples(queryWrapper,combinationParam);
    }
    @Override
    public RssFilter getScSamplesFilter(QueryWrapper<String> queryWrapper,String combinationParam){
        List<String> instruments=rssPostgresqlMapper.getScDistinct(queryWrapper,combinationParam,"rss.instrument");
        List<String> datasetIdsStr=rssPostgresqlMapper.getScDistinct(queryWrapper,combinationParam,"dataset_id");
        List<String> idsStr=rssPostgresqlMapper.getScDistinct(queryWrapper,combinationParam,"rss.id");
//        List<String> classNames=rssPostgresqlMapper.getScDistinct(queryWrapper,combinationParam,"c2.name as class_name");
        List<String> classCodes=rssPostgresqlMapper.getScDistinct(queryWrapper,combinationParam,"c2.code as class_code");
        List<Integer> ids=new LinkedList<>();
        for (String idStr:idsStr){
            ids.add(Integer.parseInt(idStr));
        }
        List<Integer> datasetIds=new LinkedList<>();
        for (String datasetIdStr:datasetIdsStr){
            datasetIds.add(Integer.parseInt(datasetIdStr));
        }
//        List<String> classNamesNew=new LinkedList<>();
//        for (String className:classNames){
//            if (className!=null){
//                classNamesNew.add(className);
//            }
//        }
        List<String> classCodesNew=new LinkedList<>();
        for (String classCode:classCodes){
            if (classCode!=null){
                classCodesNew.add(classCode);
            }
        }
        RssFilter rssFilter=new RssFilter();
        rssFilter.setDatasetIds(datasetIds);
        rssFilter.setIds(ids);
        rssFilter.setInstruments(instruments);
//        rssFilter.setUniCLassNames(classNamesNew);
        rssFilter.setUniCLassCodes(classCodesNew);
        return rssFilter;
    }

    @Override
    public List<RssLcSample> queryLcSampleDetail(String combinationParam){
        return rssPostgresqlMapper.queryLcSampleDetail(combinationParam);
    }
    @Override
    public IPage<RssLcSampleSimple> getLcSamples(IPage<RssLcSampleSimple> page, QueryWrapper<RssLcSampleSimple> queryWrapper, String combinationParam){
        return rssPostgresqlMapper.getLcSamples(page,queryWrapper,combinationParam);
    }
    @Override
    public  List<RssLcSampleSimple> getLcSamples( QueryWrapper<RssLcSampleSimple> queryWrapper,String combinationParam){
        return rssPostgresqlMapper.getLcSamples(queryWrapper,combinationParam);
    }
    @Override
    public RssFilter getLcSamplesFilter(QueryWrapper<String> queryWrapper,String combinationParam){
        List<String> instruments=rssPostgresqlMapper.getLcDistinct(queryWrapper,combinationParam,"rls.instrument");
        List<String> datasetIdsStr=rssPostgresqlMapper.getLcDistinct(queryWrapper,combinationParam,"dataset_id");
        List<String> idsStr=rssPostgresqlMapper.getLcDistinct(queryWrapper,combinationParam,"rls.id");
//        List<String> classNames=rssPostgresqlMapper.getLcDistinct(queryWrapper,combinationParam,"c2.name as class_name");
        List<String> classCodes=rssPostgresqlMapper.getLcDistinct(queryWrapper,combinationParam,"c2.code as class_code");
        List<Integer> ids=new LinkedList<>();
        for (String idStr:idsStr){
            ids.add(Integer.parseInt(idStr));
        }
        List<Integer> datasetIds=new LinkedList<>();
        for (String datasetIdStr:datasetIdsStr){
            datasetIds.add(Integer.parseInt(datasetIdStr));
        }
//        List<String> classNamesNew=new LinkedList<>();
//        for (String className:classNames){
//            if (className!=null){
//                classNamesNew.add(className);
//            }
//        }
        List<String> classCodesNew=new LinkedList<>();
        for (String classCode:classCodes){
            if (classCode!=null){
                classCodesNew.add(classCode);
            }
        }
        RssFilter rssFilter=new RssFilter();
        rssFilter.setDatasetIds(datasetIds);
        rssFilter.setIds(ids);
        rssFilter.setInstruments(instruments);
//        rssFilter.setUniCLassNames(classNamesNew);
        rssFilter.setUniCLassCodes(classCodesNew);
        return rssFilter;
    }

    @Override
    public List<RssCdSample> queryCdSampleDetail(String combinationParam){
        return rssPostgresqlMapper.queryCdSampleDetail(combinationParam);
    }
    @Override
    public IPage<RssCdSampleSimple> getCdSamples(IPage<RssCdSampleSimple> page, QueryWrapper<RssCdSampleSimple> queryWrapper, String combinationParam){
        return rssPostgresqlMapper.getCdSamples(page,queryWrapper,combinationParam);
    }
    @Override
    public  List<RssCdSampleSimple> getCdSamples( QueryWrapper<RssCdSampleSimple> queryWrapper,String combinationParam){
        return rssPostgresqlMapper.getCdSamples(queryWrapper,combinationParam);
    }
    @Override
    public RssFilter getCdSamplesFilter(QueryWrapper<String> queryWrapper,String combinationParam){
        List<String> preInstruments=rssPostgresqlMapper.getCdDistinct(queryWrapper,combinationParam,"rcs.pre_instrument");
        List<String> postInstruments=rssPostgresqlMapper.getCdDistinct(queryWrapper,combinationParam,"rcs.post_instrument");
        List<String> datasetIdsStr=rssPostgresqlMapper.getCdDistinct(queryWrapper,combinationParam,"dataset_id");
        List<String> idsStr=rssPostgresqlMapper.getCdDistinct(queryWrapper,combinationParam,"rcs.id");
//        List<String> classNames=rssPostgresqlMapper.getCdDistinct(queryWrapper,combinationParam,"c2.name as class_name");
        List<String> classCodes=rssPostgresqlMapper.getCdDistinct(queryWrapper,combinationParam,"c2.code as class_code");
        List<Integer> ids=new LinkedList<>();
        for (String idStr:idsStr){
            ids.add(Integer.parseInt(idStr));
        }
        List<Integer> datasetIds=new LinkedList<>();
        for (String datasetIdStr:datasetIdsStr){
            datasetIds.add(Integer.parseInt(datasetIdStr));
        }
//        List<String> classNamesNew=new LinkedList<>();
//        for (String className:classNames){
//            if (className!=null){
//                classNamesNew.add(className);
//            }
//        }
        List<String> classCodesNew=new LinkedList<>();
        for (String classCode:classCodes){
            if (classCode!=null){
                classCodesNew.add(classCode);
            }
        }
        RssFilter rssFilter=new RssFilter();
        rssFilter.setDatasetIds(datasetIds);
        rssFilter.setIds(ids);
        rssFilter.setPreInstruments(preInstruments);
        rssFilter.setPostInstruments(postInstruments);
//        rssFilter.setUniCLassNames(classNamesNew);
        rssFilter.setUniCLassCodes(classCodesNew);
        return rssFilter;
    }


    @Override
    public List<RssTdSample> queryTdSampleDetail(String combinationParam){
        return rssPostgresqlMapper.queryTdSampleDetail(combinationParam);
    }
    @Override
    public  IPage<RssTdSampleSimple> getTdSamples(IPage<RssTdSampleSimple> page, QueryWrapper<RssTdSampleSimple> queryWrapper,String combinationParam){
        return rssPostgresqlMapper.getTdSamples(page,queryWrapper,combinationParam);
    }
    @Override
    public RssFilter getTdSamplesFilter(QueryWrapper<String> queryWrapper,String combinationParam){
        List<String> instruments=rssPostgresqlMapper.getTdDistinct(queryWrapper,combinationParam,"ros.instrument");
        List<String> datasetIdsStr=rssPostgresqlMapper.getTdDistinct(queryWrapper,combinationParam,"dataset_id");
        List<String> idsStr=rssPostgresqlMapper.getTdDistinct(queryWrapper,combinationParam,"ros.id");
        List<Integer> ids=new LinkedList<>();
        for (String idStr:idsStr){
            ids.add(Integer.parseInt(idStr));
        }
        List<Integer> datasetIds=new LinkedList<>();
        for (String datasetIdStr:datasetIdsStr){
            datasetIds.add(Integer.parseInt(datasetIdStr));
        }
        List<String> classNamesNew=new LinkedList<>();
        RssFilter rssFilter=new RssFilter();
        rssFilter.setDatasetIds(datasetIds);
        rssFilter.setIds(ids);
        rssFilter.setInstruments(instruments);
        rssFilter.setUniCLassNames(classNamesNew);
        return rssFilter;
    }

    @Override
    public  List<RssClassMapSimple> getClassMaps(QueryWrapper<RssClassMapSimple> queryWrapper){
        return rssPostgresqlMapper.getClassMaps(queryWrapper);
    }
    @Override
    public List<RssOrderItem> getSampleIdDatasetMap(String taskType){
        String idDefinition="id";
        if (taskType.equals("sc")){
            idDefinition="sample_id";
        }

        return rssPostgresqlMapper.getSampleIdDatasetMap(idDefinition,taskType);
    }
    @Override
    public String getUserIdOfOrder(String orderNum){
        return rssPostgresqlMapper.getUserIdOfOrder(orderNum);
    }

    @Override
    public Boolean insertUserDatasetInfo(String filePath, String userCode, String datasetName,String isPublic,
                                         String datasetTask,String datasetSensor,
                                         String datasetResolution, String datasetImageType,
                                         String datasetRef, String datasetKeyword,String datasetContactor,
                                         String datasetContact,String datasetEmail,String datasetAddress,
                                         String datasetRemark) {

        String subsql = "("+
                "'"+filePath + "',"+
                "'"+userCode + "',"+
                "'"+datasetName + "',"+
                "'"+ isPublic + "',"+
                "'"+datasetTask + "',"+
                "'"+datasetSensor + "',"+
                "'"+datasetResolution + "',"+
                "'"+datasetImageType + "',"+
                "'"+datasetRef + "',"+
                "'"+ datasetKeyword + "',"+
                "'"+ datasetContactor + "',"+
                "'"+datasetContact + "',"+
                "'"+ datasetEmail + "',"+
                "'"+ datasetAddress + "',"+
                "'"+datasetRemark+"')";

        return rssPostgresqlMapper.insertUserDatasetInfo(subsql);



    }

}
