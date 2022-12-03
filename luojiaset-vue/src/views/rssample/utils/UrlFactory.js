export default class ApiURLFactory{
    constructor(baseURL="http://xxx.xxx.xxx.xxx:18066/geois-boot/rssample/"){
        this.baseURL = baseURL
    }

    getApiURL(apiName){
        const apiDict = {
            QUERY_DATA_NUM: 'getDataNumInfo',
            QUERY_DATASET: 'queryDatasets',
            QUERY_DATASET_DETAIL: 'queryDatasetDetail',
            QUERY_CLASSES_NUM: 'queryClassNumByDataset',
            QUERY_CLASSES: 'queryClasses',
            //获取缩略图
            QUERY_THUMB_PATH: 'file',
            //目标识别
            QUERY_OD_SAMPLES: 'queryOdSamples',
            QUERY_OD_SAMPLES_DETAIL: 'queryOdSampleDetail',
            QUERY_OD_SAMPLES_FILTER: 'queryOdSamplesFilter',
            //场景分类
            QUERY_SC_SAMPLES: 'queryScSamples',
            QUERY_SC_SAMPLES_DETAIL: 'queryScSampleDetail',
            QUERY_SC_SAMPLES_FILTER: 'queryScSamplesFilter',
            //地物分类
            QUERY_LC_SAMPLES: 'queryLcSamples',
            QUERY_LC_SAMPLES_DETAIL: 'queryLcSampleDetail',
            QUERY_LC_SAMPLES_FILTER: 'queryLcSamplesFilter',
            //TODO变化检测
            QUERY_CD_SAMPLES: 'queryCdSamples',
            QUERY_CD_SAMPLES_FILTER: 'queryCdSamplesFilter',
            QUERY_CD_SAMPLES_DETAIL: 'queryCdSampleDetail',
            //TODO多视三维
            QUERY_TD_SAMPLES: 'queryTdSamples',
            QUERY_TD_SAMPLES_DETAIL: 'queryTdSampleDetail',
            QUERY_TD_SAMPLES_FILTER: 'queryTdSamplesFilter',

            //订单操作
            QUERY_ORDER_INFO: 'queryOrderInfo',
            QUERY_ORDER_INFO_FILTER: 'queryOrderInfoFilter',
            QUERY_ORDER_INFO_ADMIN: 'queryOrderInfoAdmin',
            QUERY_ORDER_INFO_FILTER_ADMIN: 'queryOrderInfoFilterAdmin',
            QUERY_ORDER_ITEM: 'queryOrderItem',

            CREATE_ORDER: 'createOrder',
            DELETE_ORDER: 'deleteOrder',

            //下载
            DOWNLOAD_SAMPLE: 'download',

            //压缩上传
            COMPRESS_SAMPLE : 'compressSystem',

            //用户需知
            QUERY_USER_NOTES: 'queryUserNotes',

        }
        var path = apiDict[apiName]
        if(path == undefined){
            throw new Error('apiName错误无法构造相应URL')
        }
        return this.baseURL + path
    }
}