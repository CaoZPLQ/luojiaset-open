<template>
  <div class="main-container">
    <h2 style="text-align: center">{{$t('订单列表')}}</h2>
    <div style="margin-bottom: 10px; margin-left: 100px">
      <a-button type="primary" :disabled="!hasSelected" @click="deleteSelectedRow"> {{$t("batch_deletion")}} </a-button>
      <span style="margin-left: 10px">
        <template v-if="hasSelected">
          {{ $t('selected') + "  " + $t('total_items', {'total': selectedRowKeys.length}) }}
        </template>
      </span>
    </div>
    <div class="mainTable">
      <!-- <CustomTable></CustomTable> -->

      <a-table
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :columns="columns"
        :row-key="(record) => record.orderNum"
        :data-source="dataSource"
        @change="onTableChange"
        :pagination="pagination"
        bordered
      >
        <!-- <a slot="name" slot-scope="text">{{ text }}</a> -->
        <span slot="taskType" slot-scope="text">
          <a-tag :color="taskTypeMatchStyle[text][1]">{{ taskTypeMatchStyle[text][0] }}</a-tag>
        </span>

        <span slot="checkStatus" slot-scope="text, record">
          <a-badge v-if="checkStatus(record.tradeStatus)" status="success" :text="$t('order_checked')" />
          <a-badge v-else status="error" :text="$t('order_to_be_checked')" />
        </span>
        <span slot="operation" slot-scope="text, record">
          <a @click="() => showOrderDetail(record.orderNum)">{{$t('order_detail')}}</a>
          <a-divider type="vertical" />
          <a-popconfirm v-if="dataSource.length"  @confirm="() => onDelete(record)">
            <template slot="title">
              {{$t('dele_confirm')}}
            </template>
            <a href="javascript:;">{{$t('delete')}}</a>
          </a-popconfirm>
          <a-divider type="vertical" />
          <a :disabled="!checkStatus(record.tradeStatus)" @click="downloadSample(record.downloadUrl)">{{$t('download')}}</a>
          <!-- <a class="ant-dropdown-link"> Details <a-icon type="down" /> </a> -->
        </span>
      </a-table>
    </div>
  </div>
</template>


<script>
import axios from 'axios'
import Vue from 'vue'

export default {
  data() {
    return {
      dataSource: [],
      selectedRowKeys: [],
      tableFilterOptions: {},
      userId: Vue.ls.get('Login_Userinfo').id,
      pagination: { pageSize: 10, size: 'small', showTotal: (total) => this.$t('total_items', {'total': total}), total: 0, current: 1 },
      taskTypeMatchStyle: {
        od: [this.$t('目标识别'), 'orange'],
        sc: [this.$t('场景检索'), 'blue'],
        lc: [this.$t('地物分类'), 'red'],
        cd: [this.$t('变化检测'), 'green'],
        td: [this.$t('多视三维'), 'purple'],
      },
    }
  },

  computed: {
    hasSelected() {
      console.log(this.selectedRowKeys)
      return this.selectedRowKeys.length > 0
    },
    columns() {
      const columns = [
        {
          title: this.$t('order_num'),
          dataIndex: 'orderNum',
          key: 'orderNum',
          ellipsis: true,
        },
        {
          title: this.$t('sample_num'),
          dataIndex: 'sampleNum',
          key: 'sampleNum',
          ellipsis: true,
        },

        {
          title: this.$t('taskType'),
          dataIndex: 'taskType',
          key: 'taskType',
          scopedSlots: { customRender: 'taskType' },
        },
        {
          title: this.$t('check_status'),
          dataIndex: 'tradeStatus',
          key: 'tradeStatus',
          scopedSlots: { customRender: 'checkStatus' },
          filters:
            this.tableFilterOptions.tradeStatus == undefined
              ? []
              : this.tableFilterOptions.tradeStatus.map((v) => {
                  return {
                    text: v == 0 ? this.$t('order_to_be_checked') : this.$t('order_checked'),
                    value: '' + v,
                  }
                }),
        },
        {
          title: this.$t('create_time'),
          dataIndex: 'createTime',
          key: 'createTime',
          ellipsis: true,
          sorter: true,
          //  defaultSortOrder: 'ascend',
          sortDirections: ['descend', 'ascend'],
        },

        {
          title: this.$t('operation'),
          key: 'operation',
          scopedSlots: { customRender: 'operation' },
        },
      ]
      return columns
    },
  },

  methods: {
    onSelectChange(selectedRowKeys) {
      // console.log('selectedRowKeys changed: ', selectedRowKeys)
      this.selectedRowKeys = selectedRowKeys
    },
    deleteSelectedRow() {
      for (let orderNum of this.selectedRowKeys) {
        var apiUrl = this.$urlFactory.getApiURL('DELETE_ORDER')
        axios.get(apiUrl, { params: { orderNum: orderNum } }).then(() => {
          this.queryOrderInfo(
            {
              tradeStatus: '',
              userId: this.userId,
              pageSize: this.pagination.pageSize,
              pageNo: this.pagination.current,
              isAsc: '',
            },
            { tradeStatus: [] },
            {}
          )
          this.queryOrderInfoFilters({ userId: this.userId, tradeStatus: [] })
        })
      }
      this.$message.success(this.$t('success_delete'))
      this.selectedRowKeys = []
    },
    checkStatus(status) {
      return status == 0 ? false : true
    },

    downloadSample(downloadUrl) {
      var apiUrl = this.$urlFactory.getApiURL('DOWNLOAD_SAMPLE')
      // axios.get(apiUrl, {params:downloadUrl}).then(()=>{
      //   this.$message.success("开始下载！")
      // })
      window.open(apiUrl + '/' + downloadUrl)
    },

    onDelete(record) {
      this.editable = true
      const dataRemained = [...this.dataSource]
      //
      this.dataSource = dataRemained.filter((item) => item.key !== record.key)
      var apiUrl = this.$urlFactory.getApiURL('DELETE_ORDER')
      axios.get(apiUrl, { params: { orderNum: record.orderNum } }).then(() => {
        this.queryOrderInfo(
          {
            tradeStatus: '',
            userId: this.userId,
            pageSize: this.pagination.pageSize,
            pageNo: this.pagination.current,
            isAsc: '',
          },
          { tradeStatus: [] },
          {}
        )
        this.queryOrderInfoFilters({ userId: this.userId, tradeStatus: [] })
        this.$message.info(this.$t('success_delete'))
      })
    },
    showOrderDetail(orderNum) {
      this.$router.push({
        path: '/orderdetail/' + orderNum,
      })
    },
    queryOrderInfo(params, filters, sorter) {
      // console.log( "     " +sorter)
      if (filters.tradeStatus && filters.tradeStatus.length > 0) params.tradeStatus = filters.tradeStatus.join(',')
      if (sorter) {
        params.isAsc = sorter.order == 'ascend' ? false : sorter.order == null ? '' : true
      }
      var apiUrl = this.$urlFactory.getApiURL('QUERY_ORDER_INFO')
      axios.get(apiUrl, { params }).then((response) => {
        const { data } = response
        this.dataSource = data.result.records
        this.pagination.total = data.result.total
      })
      // console.log(this.taskTypeMatchStyle['od'][1])
    },

    queryOrderInfoFilters(filters) {
      var params = {
        userId: this.userId,
        tradeStatus: '',
      }
      if (filters.length > 0) params.tradeStatus = filters.tradeStatus.join(',')
      var apiUrl = this.$urlFactory.getApiURL('QUERY_ORDER_INFO_FILTER')
      axios.get(apiUrl, { params }).then((response) => {
        const {
          data: { result },
        } = response
        // console.log(result)
        this.tableFilterOptions = result
      })
    },
    onTableChange(pagination, filters, sorter, { currentDataSource }) {
      // this.currentAvailableKeys = currentDataSource.map(v=>v.key)
      //TODO:这一步要到更新了availablekeys之后再做
      // this.rowSelection.selectedRowKeys = this.rowSelection.selectedRowKeys.filter((v)=>{return this.currentAvailableKeys.indexOf(v) != -1}
      // this.setState({
      //       filteredInfo: filters,
      //       sortedInfo: sorter,
      //     });
      this.pagination.current = pagination.current

      this.queryOrderInfo(
        {
          userId: this.userId,
          pageSize: this.pagination.pageSize,
          pageNo: pagination.current,
          isAsc: '',
          tradeStatus: '',
        },
        filters,
        sorter
      )
      this.queryOrderInfoFilters(filters)
    },
  },

  mounted() {
    this.queryOrderInfo(
      {
        tradeStatus: '',
        userId: this.userId,
        pageSize: this.pagination.pageSize,
        pageNo: this.pagination.current,
        isAsc: '',
      },
      { tradeStatus: [] },
      {}
    )
    this.queryOrderInfoFilters({ userId: this.userId, tradeStatus: [] })
  },
}
</script>





<style scoped>
.main-container {
  /* width: 1258px; */
  margin: auto;
  padding: 30px 110px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.mainTable {
  margin: 20px 100px;
}
</style>
