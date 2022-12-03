<template>
  <div class="main-container">
    <h2 style="text-align: center">订单详情表</h2>
    <span style="text-align: right; margin-right: 100px">
      <template v-if="hasSelected">
        {{ `已选中 ${selectedRowKeys.length} 项` }}
      </template>
    </span>

    <div class="mainTable">
      <a-table
        :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        :columns="columns"
        :row-key="record => record.sampleId"
        :data-source="dataSource"
        @change="onTableChange"
        :pagination="pagination"
        bordered
      >
        <span slot="operation" slot-scope="text, record">
          <a @click="() => showDatasetDetail(record.key)">Details</a>
          <a-divider type="vertical" />
          <a>Download</a>
          <!-- <a class="ant-dropdown-link"> Details <a-icon type="down" /> </a> -->
        </span>
      </a-table>
    </div>
    <a-button type="primary" icon="download" style="width: 120px; height: 50px; float: right;margin:0 100px;"> Download </a-button>
  </div>
</template>


<script>
import axios from 'axios'


export default {
  data() {
    return {
      dataSource: [],
      // columns,
      selectedRowKeys: [],
      pagination: { pageSize: 10, size: 'small', showTotal: (total) => `共 ${total} 项`, total: 0, current: 1 },
    }
  },

  computed: {
    hasSelected() {
      return this.selectedRowKeys.length > 0
    },
    columns() {
      const columns = [
        {
          title: '订单编号',
          dataIndex: 'orderNum',
          key: 'orderNum',
          ellipsis: true,
        },
        {
          title: '样本编号',
          dataIndex: 'sampleId',
          key: 'sampleId',
          ellipis: true,
          defaultSortOrder: 'descend',
          sorter: (a, b) => a.sampleId - b.sampleId,
        },
        {
          title: '数据集名称',
          dataIndex: 'datasetName',
          key: 'datasetName',
          ellipis: true,
          // filters:
          //   this.dataSource.datasetName == undefined
        },

        {
          title: '操作',
          key: 'operation',
          scopedSlots: { customRender: 'operation' },
        },
      ]
      return columns
    },
  },

  methods: {
    onSelectChange(selectedRowKeys) {
      //   console.log('selectedRowKeys changed: ', selectedRowKeys);
      this.selectedRowKeys = selectedRowKeys
    },
    showDatasetDetail(key) {
      this.$router.push({
        path: '/datasets/datasetDetail/' + key,
      })
    },
    queryOrderItem(params) {
      var apiUrl = this.$urlFactory.getApiURL('QUERY_ORDER_ITEM')
      axios.get(apiUrl, { params }).then((response) => {
        const { data } = response
        this.dataSource = data.result.records
        this.pagination.total = data.result.total
      })
    },

    onTableChange(pagination, filters, sorter, { currentDataSource }) {
      // this.currentAvailableKeys = currentDataSource.map(v=>v.key)
      //TODO:这一步要到更新了availablekeys之后再做
      // this.rowSelection.selectedRowKeys = this.rowSelection.selectedRowKeys.filter((v)=>{return this.currentAvailableKeys.indexOf(v) != -1}
      this.pagination.current = pagination.current
      this.queryOrderItem({
        orderNum: this.$route.params.orderNum,
        pageSize: this.pagination.pageSize,
        pageNo: this.pagination.current,
      })
    },
  },

  mounted() {
    console.log(this.$route.params.orderNum)
    this.queryOrderItem({
      orderNum: this.$route.params.orderNum,
      pageSize: this.pagination.pageSize,
      pageNo: this.pagination.current,
    })
  },
}
</script>





<style scoped>
.main-container {
  /* width: 1258px; */
  margin:  auto;
  padding: 30px 110px;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.mainTable {
  /* width: 1000px; */
  margin: 20px 100px;
  /* align-content: center;
  padding: auto; */
}
</style>
