<template>
  <div class="main-container">
    <h2 style="text-align: center">{{$t('订单审核')}}</h2>
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
          <!-- <a :disabled="checkStatus(record.tradeStatus)">审核通过</a> -->
          <a v-if="!checkStatus(record.tradeStatus)" @click="compressSample(record)"> {{$t('order_to_be_checked')}} </a>
          <a v-else disabled>{{$t('order_checked')}}</a>
          <a-divider type="vertical" />
          <a-popconfirm v-if="dataSource.length"  @confirm="() => onDelete(record)">
            <template slot="title">
              {{$t('dele_confirm')}}
            </template>
            <a href="javascript:;">{{$t('delete')}}</a>
          </a-popconfirm>
          <!-- <a class="ant-dropdown-link"> Details <a-icon type="down" /> </a> -->
        </span>
      </a-table>
    </div>
    <sys-announcement-list v-show="false" ref="announceModalList" ></sys-announcement-list>
  </div>
</template>


<script>
import axios from 'axios'
import Vue from 'vue'
import SysAnnouncementList from '../system/SysAnnouncementList'
import { httpAction,getAction } from '@/api/manage'
// import {doReleaseData} from '@/api/api'

export default {
  components: {
      SysAnnouncementList
    },
  data() {
    return {
      dataSource: [],
      //   columns,
      selectedRowKeys: [],
      tableFilterOptions: {},
      // userId: Vue.ls.get('Login_Userinfo').id,
      //   checkStatus: false,
      pagination: { pageSize: 10, size: 'small', showTotal:(total) => this.$t('total_items', {'total': total}), total: 0, current: 1 },
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
      return this.selectedRowKeys.length > 0
    },
    columns() {
      const columns = [
        {
          title: this.$t('user_id'),
          dataIndex: 'userId',
          key: 'userId',
          ellipsis: true,
          filters:
            this.tableFilterOptions.userId == undefined
              ? []
              : this.tableFilterOptions.userId.map((v) => {
                  return {
                    text: v,
                    value: '' + v,
                  }
                }),
        },
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
              userId: '',
              pageSize: this.pagination.pageSize,
              pageNo: this.pagination.current,
              isAsc: '',
            },
            { userId: [], tradeStatus: [] },
            {}
          )
          this.queryOrderInfoFilters({ userId: [], tradeStatus: [] })
        })
      }
      this.$message.success(this.$t('success_delete'))
      this.selectedRowKeys = []
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
            userId: '',
            pageSize: this.pagination.pageSize,
            pageNo: this.pagination.current,
            isAsc: '',
          },
          { userId: [], tradeStatus: [] },
          {}
        )
        this.queryOrderInfoFilters({ userId: [], tradeStatus: [] })
        this.$message.info(this.$t('success_delete'))
      })
    },
    showOrderDetail(orderNum) {
      this.$router.push({
        path: '/orderdetail/' + orderNum,
      })
    },
    checkStatus(status) {
      return status == 0 ? false : true
    },
    compressSample(record) {
      const orderNum = record.orderNum
      var apiUrl = this.$urlFactory.getApiURL('COMPRESS_SAMPLE')
      axios.get(apiUrl, { params: { orderNum: orderNum } }).then((response) => {
        const { data } = response
        if (data.success) {
          this.$message.success(this.$t('compress_success'))
        } else {
          this.$message.error(this.$t('compress_failed'))
        }
        this.queryOrderInfo(
          {
            tradeStatus: '',
            userId: '',
            pageSize: this.pagination.pageSize,
            pageNo: this.pagination.current,
            isAsc: '',
          },
          { userId: [], tradeStatus: [] },
          {}
        )
        this.queryOrderInfoFilters({ userId: [], tradeStatus: [] })
        //给相关用户发送通知
        this.sendAnnouncement(record)
      })
    },
    queryOrderInfo(params, filters, sorter) {
      if (filters.tradeStatus && filters.tradeStatus.length > 0) params.tradeStatus = filters.tradeStatus.join(',')
      if (filters.userId && filters.userId.length > 0) params.userId = filters.userId.join(',')
      if (sorter) {
        params.isAsc = sorter.order == 'ascend' ? false : sorter.order == null ? '' : true
      }
      var apiUrl = this.$urlFactory.getApiURL('QUERY_ORDER_INFO_ADMIN')
      axios.get(apiUrl, { params }).then((response) => {
        const { data } = response
        this.dataSource = data.result.records
        this.pagination.total = data.result.total
      })
      // console.log(this.taskTypeMatchStyle['od'][1])
    },

    queryOrderInfoFilters(filters) {
      var params = {
        userId: '',
        tradeStatus: '',
      }
      if (filters.length > 0 && filters.tradeStatus.length > 0) params.tradeStatus = filters.tradeStatus.join(',')
      if (filters.length > 0 && filters.userId.length > 0) params.userId = filters.userId.join(',')
      var apiUrl = this.$urlFactory.getApiURL('QUERY_ORDER_INFO_FILTER_ADMIN')
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
      // console.log(sorter)
      this.pagination.current = pagination.current

      this.queryOrderInfo(
        { userId: '', pageSize: this.pagination.pageSize, pageNo: pagination.current, isAsc: '', tradeStatus: '' },
        filters,
        sorter
      )
      this.queryOrderInfoFilters(filters)
    },
    sendAnnouncement(record){
      var time = this.$moment(new Date()).format('YYYY-MM-DD HH:mm:ss')
      var params = {
      endTime: time,
      startTime: record.createTime,
      titile: "订单已审核通过",
      msgContent: "<p>订单编号为" + record.orderNum + "的订单已被审核通过</p>",
      priority: "M",
      msgCategory: 1,
      msgType: "USER",
      userIds: record.userId+",",
    }
    var apiUrl = "http://xxx.xxx.xxx.xxx:18066/geois-boot/sys/annountCement/add"
    httpAction(apiUrl,params,"post").then((res)=>{
      if(res.success){
        // console.log("shjshjhdjhskdsh")
        getAction('http://xxx.xxx.xxx.xxx:18066/geois-boot/sys/annountCement/list?column=createTime&order=desc&field=id,,,titile,msgCategory,sender,priority,msgType,sendStatus,sendTime,cancelTime,action&pageNo=1&pageSize=10','').then((response)=>{
          const data = response.result
          console.log(data)
          var notSendedRecords =  data.records.filter((v)=>v.sendStatus == 0)
          console.log(notSendedRecords)
          for(let r of notSendedRecords){
            console.log(this.$refs.announceModalList)
            this.$refs.announceModalList.releaseData(r.id)
          }
        })
      }else{
        console.log("failed")
      }
    })
    }
  },

  mounted() {
    this.queryOrderInfo(
      { tradeStatus: '', userId: '', pageSize: this.pagination.pageSize, pageNo: this.pagination.current, isAsc: '' },
      { userId: [], tradeStatus: [] },
      {}
    )
    this.queryOrderInfoFilters({ userId: [], tradeStatus: [] })
  },
}
</script>





<style scoped>
.main-container {
  /* width: 1258px;*/
  margin: 60px auto;
  padding: 30px 110px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.mainTable {
  margin: 20px;
  margin: 20px 100px;
}
</style>
