package cn.whu.geois.modules.rssample.service;

import cn.whu.geois.modules.rssample.entity.*;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingDataClassMap;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/2/2 11:39
 */
@Service
public interface RssPostgresqlService {
    boolean updateOrderInfo(Integer tradeStatus,String downloadUrl,String orderNum);
    boolean updateDatasetImage(String field, String base, String datasetName, String version);
    boolean updateDatasetSize(Integer datasetId, Double size);

    boolean updateSampleThumb(Object minioRenderPngPaths,String minioImagePath);

    boolean insertRssOdSampleBatch(List<RssOdSample> rssOdSampleList);
    List<RssOdClass> getOdClass(String combinationParam);
    List<RssScClass> getScClass(String combinationParam);
    List<RssGqjcClass> getGqjcClass(String combinationParam);
    Integer getMaxOdSampleId();
    Integer getMaxOdSampleClassId();
    Integer getMaxCdSampleId();
    Integer getMaxCdSampleClassId();
    String getLcClassCode(String name);
    Integer getMaxClassMapId();
    Integer getMaxOrderInfoId();
    Integer getMaxOrderItemId();
    List<RssScSample> getScSample(String combinationParam);
    String getScClassCode(String name);
    Integer getDatasetID(String name);
    Integer getMaxScSampleID();
    List<RssDataset> getDatasetDetail(String combinationParam);
    IPage<RssDatasetSimple> getDatasets(IPage<RssDatasetSimple> page, QueryWrapper<RssDatasetSimple> queryWrapper);
    IPage<RssOrderInfo> getOrderInfo(IPage<RssOrderInfo> page, QueryWrapper<RssOrderInfo> queryWrapper);
    RssFilter getOrderInfoFilter(QueryWrapper<String> queryWrapper);
    IPage<RssOrderItem> getOrderItem(IPage<RssOrderItem> page, QueryWrapper<RssOrderItem> queryWrapper);
    List<RssPair> getOrderItemPath( QueryWrapper<String> queryWrapper,String taskType,String columns);
    String getOrderInfoTaskType( QueryWrapper<String> queryWrapper);
    String getOrderInfoDownloadUrl( QueryWrapper<String> queryWrapper);

    List<RssDatasetSimple> listDatasets();
    List<RssDataset> getCollections(QueryWrapper<RssDataset> queryWrapper);
    List<AITrainingDataClassMap> getDatasetIdClasses();

    //适配ogc
    List<RssOdSampleSimple> getOdSamples( QueryWrapper<RssOdSampleSimple> queryWrapper, String combinationParam);
    List<RssScSampleSimple> getScSamples( QueryWrapper<RssScSampleSimple> queryWrapper, String combinationParam);
    List<RssLcSampleSimple> getLcSamples( QueryWrapper<RssLcSampleSimple> queryWrapper, String combinationParam);
    List<RssCdSampleSimple> getCdSamples( QueryWrapper<RssCdSampleSimple> queryWrapper, String combinationParam);

    List<RssOdSample> queryOdSampleDetail(String combinationParam);
    IPage<RssOdSampleSimple> getOdSamples(IPage<RssOdSampleSimple> page, QueryWrapper<RssOdSampleSimple> queryWrapper, String combinationParam);
    RssFilter getOdSamplesFilter(QueryWrapper<String> queryWrapper, String combinationParam);

    List<RssLcSample> queryLcSampleDetail(String combinationParam);
    IPage<RssLcSampleSimple> getLcSamples(IPage<RssLcSampleSimple> page, QueryWrapper<RssLcSampleSimple> queryWrapper, String combinationParam);
    RssFilter getLcSamplesFilter(QueryWrapper<String> queryWrapper, String combinationParam);

    List<RssScSample> queryScSampleDetail(String combinationParam);
    IPage<RssScSampleSimple> getScSamples(IPage<RssScSampleSimple> page,QueryWrapper<RssScSampleSimple> queryWrapper,String combinationParam);
    RssFilter getScSamplesFilter(QueryWrapper<String> queryWrapper, String combinationParam);

    List<RssTdSample> queryTdSampleDetail(String combinationParam);
    IPage<RssTdSampleSimple> getTdSamples(IPage<RssTdSampleSimple> page, QueryWrapper<RssTdSampleSimple> queryWrapper, String combinationParam);
    RssFilter getTdSamplesFilter(QueryWrapper<String> queryWrapper, String combinationParam);

    List<RssCdSample> queryCdSampleDetail(String combinationParam);
    IPage<RssCdSampleSimple> getCdSamples(IPage<RssCdSampleSimple> page, QueryWrapper<RssCdSampleSimple> queryWrapper, String combinationParam);
    RssFilter getCdSamplesFilter(QueryWrapper<String> queryWrapper, String combinationParam);

    List<RssClassMapSimple> getClassMaps(QueryWrapper<RssClassMapSimple> queryWrapper);

    List<RssOrderItem> getSampleIdDatasetMap(String taskType);

//    Integer getSCClassNum(Integer datasetId, String uniClassId);
//    Integer getODClassNum(Integer datasetId, String uniClassId);
//    List<HashMap<String,Integer>> getSCClassNum(Integer datasetId);
//    List<HashMap<String,Integer>> getODClassNum(Integer datasetId);
    List<HashMap<String,String>> getSCClassNum(Integer datasetId);
    List<HashMap<String,String>> getODClassNum(Integer datasetId);

    Integer getDatasetCount();
    Integer getDataCount();

    /**
     * 获取场景检测样本总数
     * @return
     */
    Integer getSCDataCount();

    /**
     * 获取目标检测目样本中目标总数
     * @return
     */
    Integer getODDataTargetSum();

    /**
     * 获取样本总数，尺寸 除以 （512 *512）
     * 地物分类、变化检测、多视三维
     * 通过替换任务类型
     */
    Integer getDataCountByWidthHeigth(String taskType);


    String getUserIdOfOrder(String orderNum);

    Boolean insertUserDatasetInfo(String filePath, String userCode, String datasetName,String isPublic,
                                  String datasetTask,String datasetSensor,
                                  String datasetResolution, String datasetImageType,
                                  String datasetRef, String datasetKeyword,String datasetContactor,
                                  String datasetContact,String datasetEmail,String datasetAddress,
                                  String datasetRemark);

}
