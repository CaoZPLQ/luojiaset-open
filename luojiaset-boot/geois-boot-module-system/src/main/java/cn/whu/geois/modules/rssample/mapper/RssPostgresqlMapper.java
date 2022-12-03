package cn.whu.geois.modules.rssample.mapper;

import cn.whu.geois.modules.rssample.dto.ClassNumDto;
import cn.whu.geois.modules.rssample.dto.TaskDatasetDto;
import cn.whu.geois.modules.rssample.entity.*;
import cn.whu.geois.modules.rssample.entity.ogc.AITrainingDataClassMap;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.HashMap;
import java.util.List;

/**
 * @author czp
 * @version 1.0
 * @date 2021/2/2 22:01
 */
public interface RssPostgresqlMapper {


//    @Select("select count(*) from rss_sc_sample where dataset_id = #{datasetId} and class_id = #{uniClassId}")
//    Integer getSCClassNum(@Param("datasetId") Integer datasetId, @Param("uniClassId") String uniClassId);
//
//    @Select("select count(*) from rss_od_sample inner join rss_od_sample_class on rss_od_sample.id = rss_od_sample_class.sample_id\n" +
//            "where rss_od_sample.dataset_id = #{datasetId} and rss_od_sample_class.class_id = #{uniClassId}")
//    Integer getODClassNum(@Param("datasetId") Integer datasetId, @Param("uniClassId") String uniClassId);

//    @Select("SELECT class2.name,count(*) FROM rss_sc_sample LEFT JOIN rss_sc_class class2 on rss_sc_sample.class_id = class2.code WHERE dataset_id=#{datasetId} group by class2.name;")
//    List<HashMap<String,Integer>> getSCClassNum(@Param("datasetId") Integer datasetId);
//
//    @Select("select roc.name, count(*) from (rss_od_sample Left Join rss_od_sample_class rosc on rss_od_sample.id = rosc.sample_id) left join rss_od_class roc on rosc.class_id = roc.code where dataset_id = #{datasetId} group by roc.name")
//    List<HashMap<String, Integer>> getODClassNum(@Param("datasetId") Integer datasetId);
    @Select("SELECT class2.name,count(*) FROM rss_sc_sample LEFT JOIN rss_sc_class class2 on rss_sc_sample.class_id = class2.code WHERE dataset_id=#{datasetId} group by class2.name;")
    List<HashMap<String,String>> getSCClassNum(@Param("datasetId") Integer datasetId);

    @Select("select roc.name,sum(object_num) as count from (rss_od_sample Left Join rss_od_sample_class rosc on rss_od_sample.id = rosc.sample_id) left join rss_od_class roc on rosc.class_id = roc.code where dataset_id = #{datasetId} group by roc.name")
    List<HashMap<String, String>> getODClassNum(@Param("datasetId") Integer datasetId);

    @Update("UPDATE rss_dataset SET ${field}=#{base} WHERE name=#{datasetName} AND dataset_version=#{version};")
    boolean updateDatasetImage(@Param("field") String field, @Param("base") String base, @Param("datasetName") String datasetName, @Param("version") String version);

    @Update("UPDATE rss_dataset SET data_size=#{size} WHERE id=#{datasetId};")
    boolean updateDatasetSize(@Param("datasetId") Integer datasetId, @Param("size") Double size);

    @Update("UPDATE rss_order_info SET trade_status=#{tradeStatus},download_url=#{downloadUrl},update_by='admin',update_time=current_timestamp WHERE order_num=#{orderNum};")
    boolean updateOrderInfo(@Param("tradeStatus") Integer tradeStatus, @Param("downloadUrl") String downloadUrl, @Param("orderNum") String orderNum);

    @Update("UPDATE rss_od_sample SET thumb=#{minioRenderPngPath} WHERE image_path=#{minioImagePath};")
    boolean updateSampleThumb(@Param("minioRenderPngPath") Object minioRenderPngPath,@Param("minioImagePath") String minioImagePath);

    @Select("SELECT * FROM rss_od_class WHERE 1=1 ${combinationParam}")
    List<RssOdClass> getOdClass(@Param("combinationParam") String combinationParam);

    @Select("SELECT * FROM rss_sc_class WHERE 1=1 ${combinationParam}")
    List<RssScClass> getScClass(@Param("combinationParam") String combinationParam);

    @Select("SELECT * FROM rss_gqjc_class WHERE 1=1 ${combinationParam}")
    List<RssGqjcClass> getGqjcClass(@Param("combinationParam") String combinationParam);

    @Select("SELECT max(id) FROM rss_od_sample;")
    Integer getMaxOdSampleId();
    @Select("SELECT max(id) FROM rss_od_sample_class;")
    Integer getMaxOdSampleClassId();


    @Select("SELECT code FROM rss_gqjc_class WHERE name=#{name}")
    String getLcClassCode(@Param("name") String name);
    @Select("SELECT max(id) FROM rss_class_map")
    Integer getMaxClassMapId();
    @Select("SELECT max(id) FROM rss_cd_sample;")
    Integer getMaxCdSampleId();

    @Select("SELECT max(id) FROM rss_cd_sample_class;")
    Integer getMaxCdSampleClassId();



    @Select("SELECT max(id) FROM rss_order_info;")
    Integer getMaxOrderInfoId();
    @Select("SELECT max(id) FROM rss_order_item;")
    Integer getMaxOrderItemId();

    @Insert(" INSERT INTO  rss_od_sample(id,dataset_id, sample_width,sample_height, sample_area, sample_date, " +
            "sample_quality, sample_labeler, annotation_date, label_bbox, image_path, " +
            "label_path, image_type, image_channels, image_resolution, instrument, " +
            "trn_value_test, create_by, create_time, update_by, update_time) VALUES ${values}")
    boolean insertRssOdSampleBatch(@Param("values") String values);


    @Select("SELECT  thumb,id ,dataset_id ,sample_width,sample_height ,st_astext(sample_area)as sample_area ,\n" +
            "sample_date ,sample_quality, sample_labeler ,annotation_date ,\n" +
            "label_bbox ,image_path ,label_path ,image_type,image_channels ,\n" +
            "  image_resolution ,instrument,trn_value_test FROM rss_od_sample WHERE 1=1 ${combinationParam}")
    List<RssOdSample> queryOdSampleDetail(@Param("combinationParam") String combinationParam);
    @Select("SELECT * FROM rss_sc_sample WHERE 1=1 ${combinationParam}")
    List<RssScSample> getScSample(@Param("combinationParam") String combinationParam);

    @Select("SELECT code FROM rss_sc_class WHERE name=#{name}")
    String getScClassCode(@Param("name") String name);
    @Select("SELECT id FROM rss_dataset WHERE name=#{name}")
    Integer getDatasetID(@Param("name") String name);
    @Select("SELECT max(id) FROM rss_sc_sample")
    Integer getMaxScSampleID();

    @Select("SELECT id,name,dataset_version,task_type,dataset_link,dataset_copy,dataset_cite,dataset_mode,keyword,classes,\n" +
            "  sample_sum,round(data_size::numeric,0) as data_size,sample_size,image_type,resolution ,band_size,image_form,instrument,contacter,\n" +
            "phone_number,email,address,description,overview,st_astext(location)as location,thumb\n" +
            "FROM rss_dataset WHERE 1=1 ${combinationParam}")
    List<RssDataset> getDatasetDetail(@Param("combinationParam") String combinationParam);


    @Select("SELECT id,name,dataset_copy,dataset_version,thumb,task_type,visit FROM rss_dataset ${ew.customSqlSegment}")
    IPage<RssDatasetSimple> getDatasets(IPage<RssDatasetSimple> page, @Param(Constants.WRAPPER) QueryWrapper<RssDatasetSimple> queryWrapper);

    @Select("SELECT id,name,dataset_copy,dataset_version,thumb,task_type FROM rss_dataset;")
    List<RssDatasetSimple> listDatasets();

    @Select("SELECT * FROM rss_dataset ${ew.customSqlSegment}")
    List<RssDataset> getCollections(@Param(Constants.WRAPPER) QueryWrapper<RssDataset> queryWrapper);
    @Select("SELECT dataset_id,array_to_string(array_agg(concat(concat(self_class_name,'~'),uni_class_name)),',') as classes FROM rss_class_map GROUP BY dataset_id;")
    List<AITrainingDataClassMap> getDatasetIdClasses();


//    @Select("SELECT ros.id,c2.name as class_name,dataset.name as dataset_name,object_num FROM rss_od_sample ros left join rss_dataset dataset on ros.dataset_id = dataset.id  join rss_od_sample_class rosc on ros.id = rosc.sample_id LEFT JOIN rss_od_class c2 on rosc.class_id = c2.code  ${ew.customSqlSegment} ${combinationParam}")
    @Select("SELECT image_resolution,string_agg(c2.code,',') as class_codes,label_path,image_path,image_channels,trn_value_test,ros.thumb,ros.id,string_agg(c2.name,',') as class_names,array_to_string(array_agg(object_num),',') as object_nums,string_agg(DISTINCT(dataset.name),',') as dataset_name,sample_height,sample_width,st_astext(sample_area)as bbox,string_agg(DISTINCT(ros.instrument),',') as instrument FROM rss_od_sample ros left join rss_dataset dataset on ros.dataset_id = dataset.id left join rss_od_sample_class rosc on ros.id = rosc.sample_id LEFT JOIN rss_od_class c2 on rosc.class_id = c2.code  ${ew.customSqlSegment} ${combinationParam}  group by ros.id order by ros.id")
    IPage<RssOdSampleSimple> getOdSamples(IPage<RssOdSampleSimple> page, @Param(Constants.WRAPPER) QueryWrapper<RssOdSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);

    @Select("SELECT image_resolution,string_agg(c2.code,',') as class_codes,label_path,image_path,image_channels,trn_value_test,ros.thumb,ros.id,string_agg(c2.name,',') as class_names,array_to_string(array_agg(object_num),',') as object_nums,string_agg(DISTINCT(dataset.name),',') as dataset_name,sample_height,sample_width,st_astext(sample_area)as bbox,string_agg(DISTINCT(ros.instrument),',') as instrument FROM rss_od_sample ros left join rss_dataset dataset on ros.dataset_id = dataset.id left join rss_od_sample_class rosc on ros.id = rosc.sample_id LEFT JOIN rss_od_class c2 on rosc.class_id = c2.code  ${ew.customSqlSegment} ${combinationParam}  group by ros.id order by ros.id")
    List<RssOdSampleSimple> getOdSamples( @Param(Constants.WRAPPER) QueryWrapper<RssOdSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);

    @Select("SELECT * FROM rss_order_info  ${ew.customSqlSegment}")
    IPage<RssOrderInfo> getOrderInfo(IPage<RssOrderInfo> page, @Param(Constants.WRAPPER) QueryWrapper<RssOrderInfo> queryWrapper);
    @Select("SELECT DISTINCT ${field} FROM rss_order_info ${ew.customSqlSegment}")
    List<String> getOrderInfoDistinct(@Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper, @Param("field") String field);
    @Select("SELECT * FROM rss_order_item  ${ew.customSqlSegment}")
    IPage<RssOrderItem> getOrderItem(IPage<RssOrderItem> page, @Param(Constants.WRAPPER) QueryWrapper<RssOrderItem> queryWrapper);
    @Select("SELECT ${columns} FROM rss_order_item LEFT JOIN rss_${taskType}_sample on rss_order_item.sample_id=rss_${taskType}_sample.id ${ew.customSqlSegment}")
    List<RssPair> getOrderItemPath( @Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper,@Param("taskType")String taskType,@Param("columns")String columns);

    @Select("SELECT task_type FROM rss_order_info ${ew.customSqlSegment}")
    String getOrderInfoTaskType( @Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper);
    @Select("SELECT download_url FROM rss_order_info ${ew.customSqlSegment}")
    String getOrderInfoDownloadUrl( @Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper);

    @Select("SELECT DISTINCT ${field} FROM rss_od_sample ros left join rss_dataset dataset on ros.dataset_id = dataset.id left join rss_od_sample_class rosc on ros.id = rosc.sample_id LEFT JOIN rss_od_class c2 on rosc.class_id = c2.code ${ew.customSqlSegment} ${combinationParam}")
    List<String> getOdDistinct(@Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper, @Param("combinationParam") String combinationParam, @Param("field") String field);


    @Select("SELECT  thumb,id ,dataset_id ,sample_width,sample_height ,st_astext(sample_area)as sample_area ,\n" +
            "sample_date ,sample_quality, sample_labeler ,annotation_date ,\n" +
            "image_path ,label_path ,image_type,image_channels ,\n" +
            "  image_resolution ,instrument,trn_value_test FROM rss_lc_sample WHERE 1=1 ${combinationParam}")
    List<RssLcSample> queryLcSampleDetail(@Param("combinationParam") String combinationParam);
    //@Select("SELECT ros.id,string_agg(c2.name,',') as class_names,string_agg(DISTINCT(dataset.name),',') as dataset_name,sample_height,sample_width,st_astext(sample_area)as bbox,instrument FROM rss_lc_sample ros left join rss_dataset dataset on ros.dataset_id = dataset.id  join rss_lc_sample_class rosc on ros.id = rosc.sample_id LEFT JOIN rss_gqjc_class c2 on rosc.uni_class_id = c2.code ${ew.customSqlSegment} ${combinationParam}  group by ros.id order by ros.id")
    @Select("SELECT label_path,image_resolution,string_agg(c2.code,',') as class_codes,rls.thumb,rls.id,string_agg(c2.name,',') as class_names,string_agg(DISTINCT(dataset.name),',') as dataset_name,rls.sample_height,rls.sample_width,st_astext(sample_area)as bbox,rls.instrument FROM rss_lc_sample rls left join rss_dataset dataset on rls.dataset_id = dataset.id left join rss_lc_sample_class rlsc on rls.id = rlsc.sample_id LEFT JOIN rss_gqjc_class c2 on rlsc.uni_class_id = c2.code  ${ew.customSqlSegment} ${combinationParam} group by rls.id order by rls.id")
    IPage<RssLcSampleSimple> getLcSamples(IPage<RssLcSampleSimple> page, @Param(Constants.WRAPPER) QueryWrapper<RssLcSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);

    @Select("SELECT label_path,image_resolution,string_agg(c2.code,',') as class_codes,rls.image_path,rls.image_channels,rls.trn_value_test,rls.thumb,rls.id,string_agg(c2.name,',') as class_names,string_agg(DISTINCT(dataset.name),',') as dataset_name,rls.sample_height,rls.sample_width,st_astext(sample_area)as bbox,rls.instrument FROM rss_lc_sample rls left join rss_dataset dataset on rls.dataset_id = dataset.id left join rss_lc_sample_class rlsc on rls.id = rlsc.sample_id LEFT JOIN rss_gqjc_class c2 on rlsc.uni_class_id = c2.code  ${ew.customSqlSegment} ${combinationParam} group by rls.id order by rls.id")
    List<RssLcSampleSimple> getLcSamples( @Param(Constants.WRAPPER) QueryWrapper<RssLcSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);

    @Select("SELECT DISTINCT ${field} FROM rss_lc_sample rls left join rss_dataset dataset on rls.dataset_id = dataset.id left join rss_lc_sample_class rlsc on rls.id = rlsc.sample_id LEFT JOIN rss_gqjc_class c2 on rlsc.uni_class_id = c2.code ${ew.customSqlSegment} ${combinationParam}")
    List<String> getLcDistinct(@Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper, @Param("combinationParam") String combinationParam, @Param("field") String field);



    @Select("SELECT  thumb,id,sample_id ,dataset_id ,class_id,sample_width,sample_height ,st_astext(sample_area)as sample_area ,\n" +
            "sample_date ,sample_quality, sample_labeler ,annotation_date ,\n" +
            "image_path ,image_type,image_channels ,\n" +
            "  image_resolution ,instrument,trn_value_test FROM rss_sc_sample WHERE 1=1 ${combinationParam}")
    List<RssScSample> queryScSampleDetail(@Param("combinationParam") String combinationParam);

    @Select("SELECT image_resolution,scclass.code as class_codes,scsample.image_path,scsample.image_channels,scsample.trn_value_test,scsample.thumb,scsample.id as id,scclass.name as class_names,dataset.name as dataset_name,sample_height,sample_width,scsample.instrument FROM rss_sc_sample scsample LEFT JOIN rss_dataset dataset on scsample.dataset_id = dataset.id LEFT JOIN rss_sc_class scclass on scsample.class_id = scclass.code ${ew.customSqlSegment} ${combinationParam} order by scsample.id")
    List<RssScSampleSimple> getScSamples( @Param(Constants.WRAPPER) QueryWrapper<RssScSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);

    @Select("SELECT image_resolution,scclass.code as class_codes,scsample.thumb,scsample.id as id,scclass.name as class_names,dataset.name as dataset_name,sample_height,sample_width,scsample.instrument FROM rss_sc_sample scsample LEFT JOIN rss_dataset dataset on scsample.dataset_id = dataset.id LEFT JOIN rss_sc_class scclass on scsample.class_id = scclass.code ${ew.customSqlSegment} ${combinationParam} order by scsample.id")
    IPage<RssScSampleSimple> getScSamples(IPage<RssScSampleSimple> page,@Param(Constants.WRAPPER)QueryWrapper<RssScSampleSimple> queryWrapper,@Param("combinationParam")String combinationParam);
    @Select("SELECT DISTINCT ${field} FROM rss_sc_sample rss left join rss_dataset dataset on rss.dataset_id = dataset.id LEFT JOIN rss_sc_class c2 on rss.class_id = c2.code ${ew.customSqlSegment} ${combinationParam}")
    List<String> getScDistinct(@Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper, @Param("combinationParam") String combinationParam, @Param("field") String field);


    @Select("SELECT  id,dataset_id,sample_width,sample_height,st_astext(sample_area)as sample_area,\n" +
            "pre_sample_date,post_sample_date,sample_quality,sample_labeler,annotation_date,\n" +
            "pre_image_path,post_image_path,label_path,pre_image_type,post_image_type,pre_image_channels,post_image_channels,\n" +
            "pre_image_resolution,post_image_resolution,pre_instrument,post_instrument,trn_value_test FROM rss_cd_sample WHERE 1=1 ${combinationParam}")
    List<RssCdSample> queryCdSampleDetail(@Param("combinationParam") String combinationParam);
    @Select("SELECT pre_image_resolution,post_image_resolution,string_agg(c2.code,',') as class_codes,rcs.id,string_agg(c2.name,',') as class_names,string_agg(DISTINCT(dataset.name),',') as dataset_name,rcs.sample_height,rcs.sample_width,st_astext(sample_area)as bbox,rcs.pre_instrument,rcs.post_instrument FROM rss_cd_sample rcs left join rss_dataset dataset on rcs.dataset_id = dataset.id LEFT JOIN rss_cd_sample_class rcsc on rcs.id = rcsc.sample_id LEFT JOIN rss_gqjc_class c2 on rcsc.uni_class_id = c2.code  ${ew.customSqlSegment} ${combinationParam} group by rcs.id order by rcs.id")
    IPage<RssCdSampleSimple> getCdSamples(IPage<RssCdSampleSimple> page, @Param(Constants.WRAPPER) QueryWrapper<RssCdSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);

    @Select("SELECT pre_image_resolution,post_image_resolution,string_agg(c2.code,',') as class_codes,rcs.post_image_channels,rcs.pre_instrument,rcs.post_instrument,rcs.pre_image_path,rcs.post_image_path,rcs.id,string_agg(c2.name,',') as class_names,string_agg(DISTINCT(dataset.name),',') as dataset_name,rcs.sample_height,rcs.sample_width,st_astext(sample_area)as bbox,rcs.pre_instrument,rcs.post_instrument FROM rss_cd_sample rcs left join rss_dataset dataset on rcs.dataset_id = dataset.id LEFT JOIN rss_cd_sample_class rcsc on rcs.id = rcsc.sample_id LEFT JOIN rss_gqjc_class c2 on rcsc.uni_class_id = c2.code  ${ew.customSqlSegment} ${combinationParam} group by rcs.id order by rcs.id")
    List<RssCdSampleSimple> getCdSamples( @Param(Constants.WRAPPER) QueryWrapper<RssCdSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);

    @Select("SELECT DISTINCT ${field} FROM rss_cd_sample rcs left join rss_dataset dataset on rcs.dataset_id = dataset.id LEFT JOIN rss_cd_sample_class rcsc on rcs.id = rcsc.sample_id LEFT JOIN rss_gqjc_class c2 on rcsc.uni_class_id = c2.code ${ew.customSqlSegment} ${combinationParam}")
    List<String> getCdDistinct(@Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper, @Param("combinationParam") String combinationParam, @Param("field") String field);


    @Select("SELECT  id ,dataset_id ,sample_width,sample_height ,st_astext(sample_area)as sample_area ,\n" +
            "sample_date ,sample_quality, sample_labeler ,annotation_date ,\n" +
            "muti_view_paths ,muti_view_depth_paths ,muti_view_parm_paths ,parm_mode ,depth_mode ,image_type,image_channels ,\n" +
            "  image_resolution ,instrument,trn_value_test FROM rss_td_sample WHERE 1=1 ${combinationParam}")
    List<RssTdSample> queryTdSampleDetail(@Param("combinationParam") String combinationParam);
    @Select("SELECT image_resolution,ros.id,string_agg(DISTINCT(dataset.name),',') as dataset_name,ros.sample_height,ros.sample_width,st_astext(sample_area)as bbox,ros.instrument FROM rss_td_sample ros left join rss_dataset dataset on ros.dataset_id = dataset.id ${ew.customSqlSegment} ${combinationParam} group by ros.id order by ros.id")
    IPage<RssTdSampleSimple> getTdSamples(IPage<RssTdSampleSimple> page, @Param(Constants.WRAPPER) QueryWrapper<RssTdSampleSimple> queryWrapper, @Param("combinationParam") String combinationParam);
    @Select("SELECT DISTINCT ${field} FROM rss_td_sample ros left join rss_dataset dataset on ros.dataset_id = dataset.id  ${ew.customSqlSegment} ${combinationParam}")
    List<String> getTdDistinct(@Param(Constants.WRAPPER) QueryWrapper<String> queryWrapper, @Param("combinationParam") String combinationParam, @Param("field") String field);

    @Select("SELECT s.${idDefition} as sample_id,concat(concat(dataset.name,'-'),dataset.dataset_version) as dataset_name  FROM rss_${taskType}_sample s LEFT JOIN rss_dataset dataset on s.dataset_id = dataset.id;")
    List<RssOrderItem> getSampleIdDatasetMap(@Param("idDefition") String idDefition,@Param("taskType") String taskType);
    @Select("SELECT rss_class_map.id,rss_class_map.dataset_id,uni_class_name,self_class_name,task_type\n" +
            "FROM rss_class_map LEFT JOIN rss_dataset on rss_class_map.dataset_id = rss_dataset.id ${ew.customSqlSegment}")
    List<RssClassMapSimple> getClassMaps(@Param(Constants.WRAPPER) QueryWrapper<RssClassMapSimple> queryWrapper);

    @Select("select count(*) from rss_dataset")
    Integer getDatasetCount();

    @Select("select sum(sample_width*sample_height)/(512*512) from rss_${taskType}_sample")
    Integer getDataCount(@Param("taskType") String taskType);

    @Select("select count(*) from rss_sc_sample")
    Integer getSCDataCount();

    @Select("SELECT sum(object_num) FROM rss_od_sample_class")
    Integer getODDataTargetSum();

    @Select("SELECT task_type as name , count(*) as data_num FROM rss_dataset GROUP BY task_type")
    List<TaskDatasetDto> getDatasetGroupByTaskType();

    @Select("SELECT count(*) from (SELECT class_id from rss_sc_sample sc GROUP BY class_id) t")
    Integer getClassNumOfSCClassed();

    @Select("SELECT count(*) from (SELECT uni_class_id from rss_lc_sample_class sc GROUP BY uni_class_id) t")
    Integer getClassNumOfLCClassed();

    @Select("SELECT count(*) from (SELECT class_id from rss_od_sample_class sc GROUP BY class_id) t")
    Integer getClassNumOfODClassed();

    @Select("SELECT count(*) from (SELECT uni_class_id from rss_cd_sample_class sc GROUP BY uni_class_id) t")
    Integer getClassNumOfCDClassed();

    @Select("SELECT c2.code AS class_code, c2.name AS class_name, count(ros.id) as data_num FROM rss_od_class c2  LEFT JOIN rss_od_sample_class rosc ON rosc.class_id = c2.code LEFT JOIN rss_od_sample ros ON ros.ID = rosc.sample_id GROUP BY class_code ORDER BY data_num desc LIMIT 5")
    List<ClassNumDto> getTop5ClassNameOfOdSample();

    @Select("SELECT c2.code AS class_code, c2.name AS class_name, count(rls.id) as data_num FROM rss_gqjc_class c2 LEFT JOIN rss_lc_sample_class rcsc ON rcsc.uni_class_id = c2.code LEFT JOIN rss_lc_sample rls ON rls.id = rcsc.sample_id GROUP BY class_code ORDER BY data_num desc LIMIT 5")
    List<ClassNumDto> getTop5ClassNameOfLcSample();

    @Select("select user_id from rss_order_info where order_num = #{orderNum}")
    String getUserIdOfOrder(@Param("orderNum")String orderNum);

    @Select("INSERT INTO rss_user_datasets(file_path,user_id,dataset_name,is_public,task,image_type,sensor," +
            "resolution,ref,keywords,contactor,contact,email,address,remark) VALUES ${values}")
    Boolean insertUserDatasetInfo(@Param("values")String values);

}
