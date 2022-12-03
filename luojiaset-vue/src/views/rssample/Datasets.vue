<template>
  <a-layout :style="'min-height:' + screenHeight + 'px;'" class="mainBody">
    <a-layout-sider width="200" class="siderLayout">
      <a-card>
        <template slot="title">
          {{$t('taskTypeMenu')}}
        </template>
        <a-tree
          :tree-data="treeData"
          show-icon
          default-expand-all
          :default-selected-keys="['0-0-0']"
          @select="onTaskTypeChanged"
        ><template slot="icon" slot-scope="{ selected }">
            <a-icon :type="selected ? 'folder-open' : 'folder'"></a-icon>
          </template>
          <template slot="title" slot-scope="item">
            <j-ellipsis :value="$t(item.title)" :length="15" />
            <!-- {{$t(item.title)}} -->
          </template>
        </a-tree>

      </a-card>
      <a-card :title="$t('taskTypeMenu')" v-show="showDescribe">
        <a-card :title="$t(onTaskDescribe())">
          {{ contentDescribeList[this.taskType] }}
        </a-card>
      </a-card>
    </a-layout-sider>
    <a-layout-content>
      <a-card>
<!--        <table>-->
<!--          <tr>-->
<!--            <th class="th">{{$t('subject_term')}} : </th>-->
<!--            <td class="td">-->
<!--              <template v-for="tag in tags">-->
<!--                <a-checkable-tag-->
<!--                  :key="tag"-->
<!--                  :checked="selectedTags.indexOf(tag) > -1"-->
<!--                  class="checkableTag"-->
<!--                  @change="(checked) => handleTagChange(tag, checked)"-->
<!--                >-->
<!--                  {{ tag }}-->
<!--                </a-checkable-tag>-->
<!--              </template>-->
<!--            </td>-->
<!--          </tr>-->
<!--        </table>-->

        <div class="searchBox">
          <label>{{$t('keyword')}} : </label>
          <a-select style="width: 150px" placeholder="keyword" @change="keywordHandleChange" allowClear :value="keyword">
            <a-select-option v-for="keyword in keywords" :key="keyword" >
              {{ keyword }}
            </a-select-option>

          </a-select>
          &nbsp;
          <label>{{$t('time_range')}} : </label>
          <a-range-picker style="width: 315px" v-model="timeRange">
            <a-icon slot="suffixIcon" type="calendar" />
          </a-range-picker>
          <a-button-group size="small" style="margin-left: 10px">
            <a-button icon="redo" @click="clearQueryParams">{{$t('reset')}}</a-button>
            <a-button icon="search" @click="onSearch">{{$t('submit')}}</a-button>
          </a-button-group>
        </div>

        <div class="filterBox">
          <label>{{$t('sortord')}} : </label>
          <a-radio-group v-model="sortMethod">
            <a-radio value="1">{{$t('relevancy')}}</a-radio>
            <a-radio value="2">{{$t('page_view')}}</a-radio>
            <a-radio value="3">{{$t('update_time')}}</a-radio>
          </a-radio-group>
          <span style="float: right; margin-right: 5px">{{ $t('total_items', {'total': page.totalItem})}}</span>
        </div>

        <a-list
          :grid="{ gutter: 16, column: 4 }"
          :data-source="datasets"
          :style="'height:' + (screenHeight - 177) + 'px; padding:8px; overflow-y: auto'"
        >
          <a-list-item slot="renderItem" slot-scope="item">
            <a-card :title="item.name" :bodyStyle="{ height: '205px', 'overflow-y': 'auto' }">
              <a-card :bodyStyle="{ height: '145px' }">
                <img v-if="item.thumb != null" :src="item.thumb" height="100%" style="display: block; margin: auto" />
                <a-empty v-else :image-style="{ height: '60px' }" description="暂无封面" />
              </a-card>
              <ul class="detailTable">
                <li class="detailRow">
                  <span class="detaillb">{{$t('taskType')}}：</span>
                  <span class="detailvl"
                    ><mark>{{ $t(taskTypes[item.taskType]) }}</mark></span
                  >
                </li>
                <!-- <li class="detailRow">
                  <span class="detaillb">版权归属：</span>
                  <span class="detailvl"><mark>{{item.datasetCopy}}</mark></span>
                </li>
                <li class="detailRow">
                  <span class="detaillb">版本号：</span>
                  <span class="detailvl"><mark>{{item.datasetVersion}}</mark></span>
                </li>`/datasets/datasetDetail/${item.id}` -->
              </ul>

              <a-tooltip slot="extra">
                <template slot="title"> {{$t('click_to_detail')}} </template>
                <router-link
                  :to="{ path: `/datasets/datasetDetail/${item.id}`, query: { id: item.id, taskType: item.taskType } }"
                >
                  <a-icon type="more" :style="{ fontSize: '17px' }" />
                </router-link>
              </a-tooltip>
            </a-card>
          </a-list-item>
        </a-list>

        <a-pagination
          show-size-changer
          :total="page.totalItem"
          @showSizeChange="onShowSizeChange"
          :page-size.sync="page.pageSize"
          v-model="page.currentPageNum"
          @change="onPaginationChange"
          :pageSizeOptions="['2', '4', '6', '12', '18', '24']"
          class="pagination"
        />
      </a-card>
    </a-layout-content>
  </a-layout>
</template>

<script>
import { AdjustHeightMixin } from '@/mixins/AdjustHeightMixin'
import axios from 'axios'
import JEllipsis from "@/components/jeecg/JEllipsis";

const treeData = [
  {
    title: '场景检索',
    key: 'sc',
    scopedSlots: {
      icon: 'icon',
      title: 'title',
    },
  },
  {
    title: '目标识别',
    key: 'od',
    scopedSlots: {
      icon: 'icon',
      title: 'title',
    },
  },
  {
    title: '地物分类',
    key: 'lc',
    scopedSlots: {
      icon: 'icon',
      title: 'title',
    },
  },
  {
    title: '变化检测',
    key: 'cd',
    scopedSlots: {
      title: 'title',
      icon: 'icon',
    },
  },
  {
    title: '多视三维',
    key: 'td',
    width: 150,
    ellipsis: true,
    scopedSlots: {
      title: 'title',
      icon: 'icon',
    },
  },
]

const taskTypes = {
  od: '目标识别',
  sc: '场景检索',
  lc: '地物分类',
  cd: '变化检测',
  td: '多视三维',
}

export default {
  name: 'Datasets',
  components: {
      JEllipsis
  },
  activated() {
    console.log('activated')
  },
  deactivated() {
    console.log('deactivated')
  },
  created() {
    console.log('created')
  },
  computed: {
    tags: function() {
      return [       this.$t('WHU'),
        this.$t('google'),
        this.$t('optical'),
        this.$t('SAR')]
    }
  },
  data() {
    return {
      treeData,
      // tags: [
      //   this.$t('aerial_image'),
      //   this.$t('satellite_image'),
      //   this.$t('radar_image'),
      //   this.$t('object_spectrum'),
      //   this.$t('inversion_data_product'),
      //   this.$t('rs_interpretation_product'),
      // ],
      keywords: [
        'Google','optical','Sar','WHU'
      ],
      selectedTags: [],
      sortMethod: '1',
      taskType: '',
      keyword: '',
      timeRange: null,
      page: {
        totalItem: 0,
        pageSize: 8,
        currentPageNum: 1,
      },
      taskTypes,
      datasets: [],
      showDescribe: false,
      contentDescribeList: {
        sc: this.$t('sc_description'),
        od: this.$t('od_description'),
        lc: this.$t('lc_description'),
        cd: this.$t('cd_description'),
        td: this.$t('td_description'),
      },
    }
  },
  methods: {
    keywordHandleChange(value) {
      this.keyword = value
      this.page.currentPageNum = 1
      console.log(this.keyword)
    },
    clearKeyword() {
      this.keyword = ''
    },
    handleTagChange(tag, checked) {
      const { selectedTags } = this
      const nextSelectedTags = checked ? [...selectedTags, tag] : selectedTags.filter((t) => t !== tag)
      console.log('You are interested in: ', nextSelectedTags)

      this.selectedTags = nextSelectedTags
    },
    onShowSizeChange(current, pageSize) {
      //改变了当前页数显示size时重新获取数据
      this.$nextTick(() => {
        this.onSearch()
      })
    },
    onPaginationChange(page, pageSize) {
      //改变了current页码时触发重新获取数据
      this.onSearch()
    },
    queryDatasets(params) {
      params = {
        keyword: params.keyword != undefined ? params.keyword : '',
        taskType: params.taskType != undefined ? params.taskType : '',
        pageSize: params.pageSize != undefined ? params.pageSize : 16,
        pageNo: params.pageNo != undefined ? params.pageNo : 1,
        startTime: params.startTime != undefined ? params.startTime : '',
        endTime: params.endTime != undefined ? params.endTime : '',
      }
      var apiUrl = this.$urlFactory.getApiURL('QUERY_DATASET')

      return axios.get(apiUrl, { params })
    },
    onTaskTypeChanged(e) {
      //任务类型改变时，重置page，刷新数据
      this.keyword = ''

      this.page.currentPageNum = 1
      var taskType = e[0]
      this.taskType = taskType == undefined ? '' : taskType
      this.onSearch()
      this.showDescribe = this.taskType == '' ? false : true
      // this.showDescribe = true
    },
    onTaskDescribe() {
      var a = this.treeData.filter((v) => {
        return v.key == this.taskType
      })
      if (a.length == 0) {
        return null
      }

      return a[0].title
    },
    clearQueryParams() {
      //清除所有查询条件选项中的内容，并触发一次数据更新
      this.keyword = ''
      this.timeRange = null
      this.selectedTags = ''
      this.onSearch()
    },
    onSearch() {
      //根据查询条件更新数据
      this.queryDatasets({
        keyword: this.keyword,
        taskType: this.taskType,
        pageSize: this.page.pageSize,
        pageNo: this.page.currentPageNum,
        startTime: this.timeRange == null ? '' : this.timeRange[0].format('YYYY-MM-DD'),
        endTime: this.timeRange == null ? '' : this.timeRange[1].format('YYYY-MM-DD'),
      }).then((response) => {
        console.log(this.keyword)
        const { data } = response
        this.page.totalItem = data.result.total
        this.datasets = data.result.records
      })
    },
  },
  mounted() {
    this.onSearch()
  },
  mixins: [AdjustHeightMixin],
}
</script>

<style scoped>
.siderLayout {
  background: #fff !important;
  border-right: 1px solid #f0f2f5;
  padding-top: 0px;
}
.mainBody {
  /* width: 1080px; */
  /* width: 1258px; */
  margin: 0 auto;
}
.th {
  text-align: right;
  width: 150px;
  color: #787878;
  font-weight: bold;
  background-color: #f2f2f2;
}
.td {
  padding: 8px;
  border-bottom: 1px solid #ebf0fe;
  border-top: 1px solid #ebf0fe;
}
.checkableTag:hover {
  cursor: pointer;
}
.checkableTag {
  border: 1px solid #dadada;
  margin-bottom: 4px;
  margin-top: 4px;
}
.searchBox {
  margin-top: 5px;
  border-top: 1px solid #dcdcdc;
  border-bottom: 1px solid #dcdcdc;
  padding: 3px;
  background-color: #f2f2f2;
}
.filterBox {
  padding: 3px;
  background-color: #f2f2f2;
  border-bottom: 1px solid #dcdcdc;
}
/* 穿透覆盖adv库中的样式 */
>>> .ant-card-body {
  padding: 12px;
}

.pagination {
  margin-top: 5px;
  float: right;
}

.detailTable {
  padding: 0;
  /* border-top: 1px solid #ccc; */
  margin-bottom: 0;
}
.detailTable > li {
  list-style: none;
}
.detaillb {
  display: inline-block;
  width: 120px;
  border-right: 1px solid #ccc;
  text-align: center;
  line-height: 32px;
}
.detailvl {
  display: inline-block;
  flex: 1;
  text-align: center;
  line-height: 32px;
}
.detailRow {
  border-bottom: 1px solid #ccc;
  border-right: 1px solid #ccc;
  border-left: 1px solid #ccc;
  display: flex;
}
</style>