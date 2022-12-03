<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="24">

          <a-col :md="6" :sm="8">
            <a-form-item label="datasetId">
              <a-input placeholder="请输入datasetId" v-model="queryParam.datasetId"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="sampleSize">
              <a-input placeholder="请输入sampleSize" v-model="queryParam.sampleSize"></a-input>
            </a-form-item>
          </a-col>
        <template v-if="toggleSearchStatus">
        <a-col :md="6" :sm="8">
            <a-form-item label="sampleArea">
              <a-input placeholder="请输入sampleArea" v-model="queryParam.sampleArea"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="sampleDate">
              <a-input placeholder="请输入sampleDate" v-model="queryParam.sampleDate"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="sampleQuality">
              <a-input placeholder="请输入sampleQuality" v-model="queryParam.sampleQuality"></a-input>
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
      <a-button type="primary" icon="download" @click="handleExportXls('三维多视样本元数据表')">导出</a-button>
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
    <rssTdSample-modal ref="modalForm" @ok="modalFormOk"></rssTdSample-modal>
  </a-card>
</template>

<script>
  import RssTdSampleModal from './modules/RssTdSampleModal'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'

  export default {
    name: "RssTdSampleList",
    mixins:[JeecgListMixin],
    components: {
      RssTdSampleModal
    },
    data () {
      return {
        description: '三维多视样本元数据表管理页面',
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
            title: 'datasetId',
            align:"center",
            dataIndex: 'datasetId'
           },
		   {
            title: 'sampleSize',
            align:"center",
            dataIndex: 'sampleSize'
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
            title: 'mutiViewPaths',
            align:"center",
            dataIndex: 'mutiViewPaths'
           },
		   {
            title: 'mutiViewDepthPaths',
            align:"center",
            dataIndex: 'mutiViewDepthPaths'
           },
		   {
            title: 'mutiViewParmPaths',
            align:"center",
            dataIndex: 'mutiViewParmPaths'
           },
		   {
            title: 'imageMode',
            align:"center",
            dataIndex: 'imageMode'
           },
		   {
            title: 'parmMode',
            align:"center",
            dataIndex: 'parmMode'
           },
		   {
            title: 'depthMode',
            align:"center",
            dataIndex: 'depthMode'
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
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' },
          }
        ],
		url: {
          list: "/rssample/rssTdSample/list",
          delete: "/rssample/rssTdSample/delete",
          deleteBatch: "/rssample/rssTdSample/deleteBatch",
          exportXlsUrl: "rssample/rssTdSample/exportXls",
          importExcelUrl: "rssample/rssTdSample/importExcel",
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