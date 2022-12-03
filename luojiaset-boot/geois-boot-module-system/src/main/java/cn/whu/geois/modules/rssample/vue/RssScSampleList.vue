<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="sampleId">
              <a-input placeholder="请输入sampleId" v-model="queryParam.sampleId"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="classId">
              <a-input placeholder="请输入classId" v-model="queryParam.classId"></a-input>
            </a-form-item>
          </a-col>
        <template v-if="toggleSearchStatus">
        <a-col :md="6" :sm="8">
            <a-form-item label="datasetId">
              <a-input placeholder="请输入datasetId" v-model="queryParam.datasetId"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="sampleWidth">
              <a-input placeholder="请输入sampleWidth" v-model="queryParam.sampleWidth"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="sampleHeight">
              <a-input placeholder="请输入sampleHeight" v-model="queryParam.sampleHeight"></a-input>
            </a-form-item>
          </a-col>
          </template>
          <a-col :md="6" :sm="8" >
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('场景分类样本元数据表')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="batchDel"><a-icon type="delete"/>删除</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /></a-button>
      </a-dropdown>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        @change="handleTableChange">

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>

          <a-divider type="vertical" />
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    </div>
    <!-- table区域-end -->

    <!-- 表单区域 -->
    <rssScSample-modal ref="modalForm" @ok="modalFormOk"></rssScSample-modal>
  </a-card>
</template>

<script>
  import RssScSampleModal from './modules/RssScSampleModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'

  export default {
    name: "RssScSampleList",
    mixins:[JeecgListMixin],
    components: {
      RssScSampleModal
    },
    data () {
      return {
        description: '场景分类样本元数据表管理页面',
        // 表头
        columns: [
          {
            title: '#',
            dataIndex: '',
            key:'rowIndex',
            width:60,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
           },
		   {
            title: 'sampleId',
            align:"center",
            dataIndex: 'sampleId'
           },
		   {
            title: 'classId',
            align:"center",
            dataIndex: 'classId'
           },
		   {
            title: 'datasetId',
            align:"center",
            dataIndex: 'datasetId'
           },
		   {
            title: 'sampleWidth',
            align:"center",
            dataIndex: 'sampleWidth'
           },
		   {
            title: 'sampleHeight',
            align:"center",
            dataIndex: 'sampleHeight'
           },
		   {
            title: 'sampleArea',
            align:"center",
            dataIndex: 'sampleArea'
           },
		   {
            title: 'sampleDate',
            align:"center",
            dataIndex: 'sampleDate'
           },
		   {
            title: 'sampleQuality',
            align:"center",
            dataIndex: 'sampleQuality'
           },
		   {
            title: 'sampleLabeler',
            align:"center",
            dataIndex: 'sampleLabeler'
           },
		   {
            title: 'annotationDate',
            align:"center",
            dataIndex: 'annotationDate'
           },
		   {
            title: 'imagePath',
            align:"center",
            dataIndex: 'imagePath'
           },
		   {
            title: 'imageType',
            align:"center",
            dataIndex: 'imageType'
           },
		   {
            title: 'imageChannels',
            align:"center",
            dataIndex: 'imageChannels'
           },
		   {
            title: 'imageResolution',
            align:"center",
            dataIndex: 'imageResolution'
           },
		   {
            title: 'instrument',
            align:"center",
            dataIndex: 'instrument'
           },
		   {
            title: 'trnValueTest',
            align:"center",
            dataIndex: 'trnValueTest'
           },
		   {
            title: 'thumb',
            align:"center",
            dataIndex: 'thumb'
           },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' },
          }
        ],
		url: {
          list: "/rssample/rssScSample/list",
          delete: "/rssample/rssScSample/delete",
          deleteBatch: "/rssample/rssScSample/deleteBatch",
          exportXlsUrl: "rssample/rssScSample/exportXls",
          importExcelUrl: "rssample/rssScSample/importExcel",
       },
    }
  },
  computed: {
    importExcelUrl: function(){
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
    }
  },
    methods: {
     
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>