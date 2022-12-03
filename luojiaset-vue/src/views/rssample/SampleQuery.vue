<template>
  <div class="mainBody">
    <a-layout :style="'height:' + screenHeight + 'px;'">
      <a-layout-sider width="450" class="siderLayout">
        <div style="padding: 12px">
          <a-tabs v-model="activeTabKey">
            <a-tab-pane key="1" :tab="$t('query_params')">
              <a-form :form="form" layout="vertical">
                <a-form-item >
                  <template slot="label">
                    {{$t('taskType')}}
                  </template>
                  <a-select
                    v-decorator="[
                      'taskType',
                      {
                        rules: [
                          {
                            required: true,
                            message: '请选择任务类型！',
                          },
                        ],
                        initialValue: 'od',
                      },
                    ]"
                    @change="onTaskTypeChange"
                  >
                    <a-select-option value="od"> {{$t('目标识别')}} </a-select-option>
                    <a-select-option value="sc"> {{$t('场景检索')}} </a-select-option>
                    <a-select-option value="lc"> {{$t('地物分类')}} </a-select-option>
                    <a-select-option value="cd"> {{$t('变化检测')}} </a-select-option>
                    <a-select-option value="td"> {{$t('多视三维')}} </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :label="$t('datasetName')">
                  <a-auto-complete :data-source="datasetNames" :placeholder="$t('datasetName_placeholder')" :filter-option="filterDatasetOption" v-decorator="['datasetName']" allowClear/>
                </a-form-item>
                <a-form-item :label="$t('imageType')">
                  <a-select v-decorator="['imageType']" allowClear  @change="imageTypeChange" :placeholder="$t('imageTypePlaceholder')">
                    <a-select-option value="Sar">Sar</a-select-option>
                    <a-select-option value="optical">optical</a-select-option>
                    <a-select-option value="multi-spectral">multi-spectral</a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item :label="$t('className')">
                  <a-select mode="multiple" style="width: 100%" :placeholder="$t('className_placeholder')" v-decorator="['classes']" allowClear @dropdownVisibleChange="queryClasses(true,localeValue)" :disabled="this.form.getFieldValue('taskType') == 'td'">
                    <a-select-option v-for="c in classes" :key="c[1]">
                      {{ c[0] }}
                    </a-select-option>
                  </a-select>
                </a-form-item>

                <a-row type="flex" align="middle">
                  <a-col flex="1">
                    <a-form-item :label="$t('min_longitude')">
                      <a-input
                        v-decorator="[
                          'minValueLong',
                          {
                            rules: [
                              {
                                pattern: /^(-?\d+)(\.\d+)?$/,
                                message: $t('num_format_warning'),
                              },
                            ],
                          },
                        ]"
                        allowClear
                      ></a-input>
                    </a-form-item>
                  </a-col>
                  <a-col style="padding: 0 3px; display: flex; flex-direction: column; justify-content: center"><span>~</span></a-col>
                  <a-col flex="1">
                    <a-form-item :label="$t('max_longitude')">
                      <a-input
                        v-decorator="[
                          'maxValueLong',
                          {
                            rules: [
                              {
                                pattern: /^(-?\d+)(\.\d+)?$/,
                                message: $t('num_format_warning'),

                              },
                            ],
                          },
                        ]"
                        allowClear
                      >
                      </a-input>
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row type="flex">
                  <a-col flex="1">
                    <a-form-item :label="$t('min_latitude')">
                      <a-input
                        v-decorator="[
                          'minValueLat',
                          {
                            rules: [
                              {
                                pattern: /^(-?\d+)(\.\d+)?$/,
                                message: $t('num_format_warning'),
                              },
                            ],
                          },
                        ]"
                        allowClear
                      ></a-input>
                    </a-form-item>
                  </a-col>
                  <a-col style="padding: 0 3px; display: flex; flex-direction: column; justify-content: center"><span>~</span></a-col>
                  <a-col flex="1">
                    <a-form-item :label="$t('max_latitude')">
                      <a-input
                        v-decorator="[
                          'maxValueLat',
                          {
                            rules: [
                              {
                                pattern: /^(-?\d+)(\.\d+)?$/,
                                message: $t('num_format_warning'),
                              },
                            ],
                          },
                        ]"
                        allowClear
                      ></a-input>
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row>
                  <a-switch style="float: right" v-model="checked" @change="onDrawerSwitched"></a-switch>
                  <label style="float: right; margin-right: 1em">{{$t('recDraw')}}</label>
                </a-row>
                <a-form-item :label="$t('time_range')">
                  <a-range-picker v-decorator="['dateRange']" style="width: 100%" allowClear />
                </a-form-item>
                <a-form-item>
                  <a-button-group style="float: right">
                    <a-button type="primary" icon="redo" @click="onReset">{{$t('reset')}}</a-button>
                    <a-button type="primary" icon="search" @click="onSubmit">{{$t('submit')}}</a-button>
                  </a-button-group>
                </a-form-item>
              </a-form>
            </a-tab-pane>
            <a-tab-pane key="2" :tab="$t('query_results')" :disabled="resultTabDisabled">
              <div>
                <a-checkbox style="margin-left: 16px" @change="onSelectAllChange" :indeterminate="selectedCount > 0 && selectedCount < currentAvailableKeys.length">{{$t('select_all')}}</a-checkbox>
                <div class="cart">
                  <a-icon type="shopping-cart" style="font-size: 21px" />
                  {{ $t('selected') + "  " + $t('total_items', {'total': selectedCount}) }}
                </div>
              </div>
              <a-table :columns="columns" :rowSelection="rowSelection" :dataSource="tableDatasource" @change="onTableChange" :pagination="pagination" :scroll="{ x: 600 }">
                <template slot="thumbnail" slot-scope="text, record">
                  <a-tooltip>
                    <template slot="title"> {{$t('click_to_detail')}} </template>
                    <img :src="text" width="30px" height="30px" class="pointer" @click="showDetailModal(record)" />
                  </a-tooltip>
                  <a-tooltip>
                    <template slot="title" v-if="record.bbox"> {{$t('click_to_3d')}} </template>
                    <template slot="title" v-else>🛇</template>
                    <a-icon v-if="record.bbox" type="eye" :theme="record.eyeClicked ? 'twoTone' : 'outlined'" @click="onEyeClicked(record)" class="pointer"></a-icon>
                    <a-icon v-else type="eye-invisible" class="pointer" />
                  </a-tooltip>
                </template>
              </a-table>
              <a-button type="primary" style="float: right" @click="showOrderList" :disabled="this.$store.state.user.username == 'guest1'">{{$t('submit')}}</a-button>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-layout-sider>
      <a-layout-content>
        <cesium-viewer ref="cesiumViewer" style="width: 100%; height: 100%; position: relative" />
      </a-layout-content>
    </a-layout>
    <div style="z-index: 10000">
      <a-modal v-model="detailModalVisible" :title="$t('sample_detail')" :footer="null" :width="700" :destroyOnClose="true" @cancel="detailModalVisible = falsesampleDetail = {}">
        <div style="display: flex" v-if="sampleDetail != {}">
          <div style="padding-right: 12px;display: flex; justify-content: center; flex-direction: column">
            <img :src="detailThumb" width="300px" height="auto" />
            <a-button  @click="changeDetailThumb(sampleDetail.thumb, sampleDetail.taskType)"
              ><a-icon type="sync"
            /></a-button>
          </div>
          <div style="flex: 1 1 auto; align-items: center">
            <p class="detailTitle">{{$t("sample_detail")}}</p>
            <ul>
              <li class="detailRow">
                <span class="detaillb">{{$t('sample_id')}}</span>
                <span class="detailvl">{{ sampleDetail.id != null ? sampleDetail.id : $t('sample_none') }}</span>
              </li>
              <li class="detailRow">
                <span class="detaillb">{{$t('sample_class')}}</span>
                <span class="detailvl">{{
                  sampleDetail.id != null
                    ? tableDatasource.filter((v) => {
                        return v.id == sampleDetail.id
                      })[0].classNames
                    : $t('sample_none')
                }}</span>
              </li>
              <li class="detailRow">
                <span class="detaillb">{{$t('sample_instrument')}}</span>
                <span class="detailvl">{{ sampleDetail.instrument != null ? sampleDetail.instrument : $t('sample_none') }}</span>
              </li>
              <li class="detailRow">
                <span class="detaillb">{{$t('数据集')}}</span>
                <span class="detailvl">{{
                  sampleDetail.id != null
                    ? tableDatasource.filter((v) => {
                        return v.id == sampleDetail.id
                      })[0].datasetName
                    : $t('sample_none')
                }}</span>
              </li>
              <li class="detailRow">
                <span class="detaillb">{{$t('sample_size')}}</span>
                <span class="detailvl">{{
                  sampleDetail.id != null
                    ? tableDatasource.filter((v) => {
                        return v.id == sampleDetail.id
                      })[0].size
                    : $t('sample_none')
                }}</span>
              </li>
              <li class="detailRow">
                <span class="detaillb">{{$t('sample_min_extent')}}</span>
                <span class="detailvl">{{ $t('sample_none') }}</span>
              </li>
              <li class="detailRow">
                <span class="detaillb">{{$t('sample_max_extent')}}</span>
                <span class="detailvl">{{ $t('sample_none') }}</span>
              </li>
            </ul>
          </div>
        </div>
      </a-modal>
    </div>
    <div style="z-index: 10000">
      <a-modal v-model="userNotesModalVisible" :footer="null" :width="700" :destroyOnClose="true" @cancel="userNotesModalVisible = falsestepCurrent = 0submitSampleArray = []specificationSampleArray = []">
        <div>
          <div style="padding: 20px">
            <a-steps :current="stepCurrent">
              <a-step :title="$t('use_agreement')" :description="$t('agree_use_agreement')">
                <a-icon slot="icon" type="notification" />
              </a-step>
              <a-step :title="$t('done')" :description="$t('get_download_link')">
                <a-icon slot="icon" type="check-circle" />
              </a-step>
            </a-steps>
          </div>
          <div style="flex: 1 1 auto; align-items: center" v-if="stepCurrent == 0">
            <h3><b>{{ $t('download_instructions') }}</b></h3>
            <div style="padding: 7px">
              <p style="text-indent: 2em">
                {{ $t('clause one') }}</p>
              <ul>
                <li>{{ $t('clause two') }}</li>
                <li>{{ $t('clause three') }} </li>
                <li>{{ $t('clause four') }}</li>
                <li>{{ $t('clause five') }}</li>
                <li>{{ $t('clause six') }}</li>
                <li>{{ $t('clause seven') }}</li>
                <li>{{ $t('clause eight') }}</li>
                <li>{{ $t('clause nine') }}</li>
                <li>{{ $t('clause ten') }}</li>
              </ul>
            </div>
            <h3><b>{{ $t('dataset_include') }}</b></h3>
            <div class="userNote">
              <a-list :data-source="submitSampleArray" :split="false">
                <a-list-item slot="renderItem" slot-scope="item, index"> {{ index + 1 }}、{{ item }} </a-list-item>
              </a-list>
            </div>
            <h3><b>{{ $t('reference') }}</b></h3>
            <div class="userNote">
              <p v-if="specificationSampleArray.toString() === ''">尚无任何引用规范</p>
              <a-list v-else :data-source="specificationSampleArray" :split="false">
                <a-list-item slot="renderItem" slot-scope="item, index">
                  {{ index + 1 }}、{{ item.toString() }}
                </a-list-item>
              </a-list>
            </div>
            <a-checkbox @change="onCheckboxChange"> {{ $t('self_agree_use_agreement') }} </a-checkbox>
          </div>
          <div style="flex: 1 1 auto; text-align: center" v-if="stepCurrent == 1">
            <h2>
              <span>
                <a-icon slot="icon" style="color: #1890ff" type="check-circle" />
                {{ $t('order_submitted') }}
              </span>
            </h2>
          </div>
          <div class="steps-action">
            <a-button :disabled="nextButtonDisabled" v-if="stepCurrent < 3" type="primary" @click="next">
              {{ $t('next_step') }}
            </a-button>
          </div>
        </div>
      </a-modal>
    </div>
  </div>
</template>
<script>
import { AdjustHeightMixin } from '@/mixins/AdjustHeightMixin'
import CesiumViewer from './components/scene/CesiumViewer'
import ImageLoader from './utils/ImageLoader'
import CesiumController from './utils/CesiumController'
import ShapeDrawer from './utils/ShapeDrawer'
import axios from 'axios'
import Vue from 'vue'
import {mapState, mapGetters} from 'vuex'
const sensorOptions = [
  {
    value: '2 米',
    label: '2 米',
    children: [
      {
        value: 'ZY3-01',
        label: 'ZY3-01',
        children: [
          {
            value: 'MUX',
            label: 'MUX',
          },
          {
            value: 'NAD',
            label: 'NAD',
          },
          {
            value: 'DLC',
            label: 'DLC',
          },
        ],
      },
      {
        value: 'ZY3-02',
        label: 'ZY3-02',
        children: [
          {
            value: 'MUX',
            label: 'MUX',
          },
          {
            value: 'NAD',
            label: 'NAD',
          },
          {
            value: 'DLC',
            label: 'DLC',
          },
        ],
      },
      {
        value: 'GF-1',
        label: 'GF-1',
        children: [
          {
            value: 'PMS',
            label: 'PMS',
          },
          {
            value: 'WFV',
            label: 'WFV',
          },
        ],
      },
      {
        value: 'GF-6',
        label: 'GF-6',
        children: [
          {
            value: 'PMS',
            label: 'PMS',
          },
          {
            value: 'WFV',
            label: 'WFV',
          },
        ],
      },
    ],
  },
  {
    value: '亚米',
    label: '亚米',
    children: [
      {
        value: 'GF-2',
        label: 'GF-2',
        children: [
          {
            value: 'PMS',
            label: 'PMS',
          },
        ],
      },
      {
        value: 'GF-7',
        label: 'GF-7',
        children: [
          {
            value: 'MUX',
            label: 'MUX',
          },
          {
            value: 'BWD',
            label: 'BWD',
          },
          {
            value: 'FWD',
            label: 'FWD',
          },
        ],
      },
      {
        value: 'BJ-2',
        label: 'BJ-2',
        children: [
          {
            value: 'PMS',
            label: 'PMS',
          },
        ],
      },
    ],
  },
  {
    value: '其它',
    label: '其它',
    children: [
      {
        value: 'GF-3/SAR',
        label: 'GF-3/SAR',
        children: [
          {
            value: 'SL',
            label: 'SL',
          },
          {
            value: 'UFS',
            label: 'UFS',
          },
          {
            value: 'FSI',
            label: 'FSI',
          },
          {
            value: 'FSII',
            label: 'FSII',
          },
          {
            value: 'SS',
            label: 'SS',
          },
          {
            value: 'QPSI',
            label: 'QPSI',
          },
          {
            value: 'QPSII',
            label: 'QPSII',
          },
          {
            value: 'NSC',
            label: 'NSC',
          },
          {
            value: 'WSC',
            label: 'WSC',
          },
          {
            value: 'GLO',
            label: 'GLO',
          },
          {
            value: 'WAV',
            label: 'WAV',
          },
          {
            value: 'EXT',
            label: 'EXT',
          },
        ],
      },
      {
        value: 'GF-4',
        label: 'GF-4',
        children: [
          {
            value: 'PMI',
            label: 'PMI',
          },
          {
            value: 'IRS',
            label: 'IRS',
          },
        ],
      },
      {
        value: '珠海一号',
        label: '珠海一号',
        children: [
          {
            value: 'OHS-2',
            label: 'OHS-2',
          },
          {
            value: 'OHS-3',
            label: 'OHS-3',
          },
          {
            value: 'OVS-1',
            label: 'OVS-1',
          },
          {
            value: 'OVS-2',
            label: 'OVS-2',
          },
          {
            value: 'OVS-3',
            label: 'OVS-3',
          },
        ],
      },
    ],
  },
]
function compareStr(a, b) {
  while (true) {
    if (a == b) return 0
    const lena = a.length
    const lenb = b.length
    if (lena == 0 || lenb == 0) return lena > lenb ? 1 : -1

    if (a[0] > b[0]) {
      return 1
    } else if (a[0] < b[0]) {
      return -1
    } else {
      a = a.slice(1)
      b = b.slice(1)
    }
  }
}
export default {
  name: 'SampleQuery',
  components: {
    CesiumViewer,
  },
    data() {
    return {
      form: this.$form.createForm(this, { name: 'SampleQuery' }),
      datasets: {},
      sensorOptions,
      rowSelection: {
        onChange: (selectedRowKeys) => {
          this.rowSelection.selectedRowKeys = selectedRowKeys
        },
        selectedRowKeys: [],
      },
      pagination: {
        pageSize: 5,
        size: 'small',
        showTotal: (total) => this.$t('total_items', {'total': total}),
        total: 0,
        current: 1,
      },
      tableDatasource: [],
      imageLoader: null,
      cesiumController: null,
      shapeDrawer: null,
      detailModalVisible: false,
      classes: [],
      activeTabKey: '1',
      tableFilterOptions: {},
      sampleDetail: {},
      userId: Vue.ls.get('Login_Userinfo').id,
      userNotesModalVisible: false,
      submitSampleArray: [],
      specificationSampleArray: [],
      stepCurrent: 0,
      noteChecked: false,
      nextButtonDisabled: false,
      classesOfResultDisabled: false,
      instrumentsOfResultDisabled: false,
      imgVisibleIdArray: [],
      detailThumb: '',
      checked: false,
      imageType: ''
    }
  },
  computed: {
    selectedCount() {
      return this.rowSelection.selectedRowKeys.length
    },
    datasetNames() {
      return Object.keys(this.datasets)
    },
    resultTabDisabled() {
      return this.activeTabKey == '1' ? true : false
    },
    currentAvailableKeys() {
      return this.tableFilterOptions.ids
    },
    columns() {
      var columns = [
        {
          title: this.$t('thumb_nail'),
          dataIndex: 'thumbnail',
          key: 'thumbnail',
          scopedSlots: {
            customRender: 'thumbnail',
          },
          width: 80,
          fixed: 'left',
        },
        {
          title: this.$t('sample_class'),
          dataIndex: 'classNames',
          key: 'classNames',
          width: 150,
          filters:
            this.tableFilterOptions.uniCLassCodes == undefined
              ? []
              : this.tableFilterOptions.uniCLassCodes.map((v) => {
                  if (
                    this.classes.filter((c) => {
                      return c[1] == v
                    }).length == 0
                  ) {
                    return {
                      text: v,
                      value: v,
                    }
                  }
                  return {
                    text: this.classes.filter((c) => {
                      return c[1] == v
                    })[0][0],
                    value: this.classes.filter((c) => {
                      return c[1] == v
                    })[0][1],
                  }
                }),
          ellipsis: true,
        },
        {
          title: this.$t('sample_instrument'),
          dataIndex: 'instrument',
          key: 'instrument',
          width: 150,
          filters:
            this.tableFilterOptions.instruments == undefined
              ? []
              : this.tableFilterOptions.instruments.map((v) => {
                  return {
                    text: v == null ? this.$t('sample_none') : v,
                    value: v == null ? '' : v,
                  }
                }),
        },
        {
          title: this.$t('sample_preInstrument'),
          dataIndex: 'preInstrument',
          key: 'preInstrument',
          width: 150,
          filters:
            this.tableFilterOptions.preInstruments == undefined
              ? []
              : this.tableFilterOptions.preInstruments.map((v) => {
                  return {
                    text: v == null ? this.$t('sample_none') : v,
                    value: v == null ? '' : v,
                  }
                }),
        },
        {
          title: this.$t('sample_postInstrument'),
          dataIndex: 'postInstrument',
          key: 'postInstrument',
          width: 150,
          filters:
            this.tableFilterOptions.postInstruments == undefined
              ? []
              : this.tableFilterOptions.postInstruments.map((v) => {
                  return {
                    text: v == null ? this.$t('sample_none') : v,
                    value: v == null ? '' : v,
                  }
                }),
        },
        {
          title: this.$t('数据集'),
          key: 'datasetName',
          dataIndex: 'datasetName',
          width: 150,
          filters:
            this.tableFilterOptions.datasetIds == undefined
              ? []
              : this.tableFilterOptions.datasetIds.map((v) => {
                  return {
                    text: Object.keys(this.datasets).filter((k) => {
                      return this.datasets[k] == v
                    })[0],
                    value: v + '',
                  }
                }),
        },
        {
          title: this.$t('sample_size'),
          key: 'size',
          dataIndex: 'size',
          width: 150,
        },
      ]
      if (this.classesOfResultDisabled) {
        columns = columns.filter((item) => item.dataIndex !== 'classNames')
      }
      if (this.instrumentsOfResultDisabled) {
        columns = columns.filter((item) => item.dataIndex !== 'instrument')
      } else {
        columns = columns.filter((item) => !['postInstrument', 'preInstrument'].includes(item.dataIndex))
      }
      return columns
    },
    ...mapState([        'localeValue',      ]),      ...mapGetters([        'localeValue',      ]),
  },
  watch: {
 localeValue(val) {
   this.localeValue = val
   this.activeTabKey = '1'
   this.onReset()
 }
 },
  methods: {
    filterDatasetOption(input, option) {
      return option.componentOptions.children[0].text.toUpperCase().indexOf(input.toUpperCase()) >= 0
    },
    filterSensorCascader(inputValue, path) {
      return path.some((option) => option.label.toLowerCase().indexOf(inputValue.toLowerCase()) > -1)
    },
    onSubmit() {
      this.form.validateFields((errors, values) => {
        if (!errors) {
          this.querySamples(1, 5)
          this.querySamplesFilterInfo({})
          this.activeTabKey = '2'
          this.imgVisibleIdArray = []
          this.$refs.cesiumViewer.sliderVisible = false
          this.columns[1].filteredValue = []
          this.columns[2].filteredValue = []
          this.columns[3].filteredValue = []
        }
      })
    },
    onEyeClicked(record) {
      if (record.eyeClicked) {
        this.imgVisibleIdArray = this.imgVisibleIdArray.filter((v) => {
          return v != record.id
        })
        if (this.imgVisibleIdArray.length == 0) this.$refs.cesiumViewer.sliderVisible = false
        record.eyeClicked = false
        if (!(record.bbox[0] instanceof Array)) {
          this.cesiumController.removeEnvelopes(record.id)
        } else if (record.taskType == 'lc') {
          this.cesiumController.removeEnvelopes(record.id)
          this.imageLoader.removeImageByURL(record.thumb[0])
          this.imageLoader.removeImageByURL(record.thumb[1])
          this.cesiumController.removeEnvelopes(record.id)
        } else {
          this.cesiumController.removeEnvelopes(record.id)
          this.imageLoader.removeImageByURL(record.thumbnail)
        }
        return
      }
      this.imgVisibleIdArray.push(record.id)
      record.eyeClicked = true
      if (!(record.bbox[0] instanceof Array)) {
        this.cesiumController.drawPoint(record.thumbnail, record.bbox, record.id)
      } else {
        if (record.taskType == 'lc') {
          this.$refs.cesiumViewer.sliderVisible = true
          this.$nextTick(() => {
            this.imageLoader.loadLCImageSliderLabel(record.thumb[0], record.thumb[1], record.bbox)
            this.cesiumController.drawEnvelopes(record.bbox, record.id)
          })
        } else {
          this.imageLoader.loadImage(record.thumbnail, record.bbox)
          this.cesiumController.drawEnvelopes(record.bbox, record.id)
        }
      }
    },
    clickCesiumObject(record){
      const viewer = this.$refs.cesiumViewer.getViewer()
      var handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
      console.log("begin test")
      handler.setInputAction(function(movement) {
           var ray=viewer.camera.getPickRay(movement.position);
           var position = viewer.scene.globe.pick(ray, viewer.scene);
           var cartographic= Cesium.Cartographic.fromCartesian(position)
           cartographic= Cesium.Math.toDegrees(cartographic.latitude)

        },Cesium.ScreenSpaceEventType.LEFT_CLICK)
    },
    showDetailModal(record) {
      this.querySampleDetail(record.key)
    },
    onDrawerSwitched(checked) {
      if (!checked) {
        this.shapeDrawer.switch(false)
        return
      }
      this.shapeDrawer.switch(true)
    },
    onTableChange(pagination, filters, sorter, { currentDataSource }) {
      console.log(pagination, filters, sorter)
      this.querySamples(pagination.current, this.pagination.pageSize, filters)
      this.querySamplesFilterInfo(filters)
      this.pagination.current = pagination.current
    },
    onSelectAllChange(e) {
      if (!e.target.checked) {
        this.rowSelection.selectedRowKeys = []
        return
      }
      this.rowSelection.selectedRowKeys = this.currentAvailableKeys
    },
    onReset() {
      this.form.resetFields()
      this.onTaskTypeChange(this.form.getFieldValue('taskType'))
      this.checked = false
      this.shapeDrawer.switch(true)
    },
    queryDatasets(params) {
      params = {
        keyword: params.keyword != undefined ? params.keyword : '',
        taskType: params.taskType != undefined ? params.taskType : '',
        pageSize: params.pageSize != undefined ? params.pageSize : 999999,
        pageNo: params.pageNo != undefined ? params.pageNo : 1,
        startTime: params.startTime != undefined ? params.startTime : '',
        endTime: params.endTime != undefined ? params.endTime : '',
      }
      var apiUrl = this.$urlFactory.getApiURL('QUERY_DATASET')
      axios.get(apiUrl, { params }).then((response) => {
        const { data } = response
        this.datasets = {}
        for (var record of data.result.records) {
          this.datasets[record.name + '_v' + record.datasetVersion] = record.id
        }
      })
    },
    onTaskTypeChange(taskType) {
      this.form.resetFields()
      this.queryDatasets({ taskType })
      this.$nextTick(() => {
        this.queryClasses(true, this.localeValue)
      })
    },
    imageTypeChange(imageType){
      this.imageType = imageType
    },
    queryClasses(shown, localeValue) {
      if (!shown) return
      const datasetName = this.form.getFieldValue('datasetName')
      const datasetId = datasetName == undefined ? '' : this.datasets[datasetName]
      var localBoolean = false
      if(localeValue == 'en') localBoolean = true
      var params = {
        taskType: this.form.getFieldValue('taskType'),
        datasetId: datasetId,
        isEnglish:localBoolean,
      }
      var apiUrl = this.$urlFactory.getApiURL('QUERY_CLASSES')
      axios.get(apiUrl, { params }).then((response) => {
        const { data } = response
        this.classes = data.result
      })
    },
    bbox2WKT(bbox) {
      const [blon, blat, tlon, tlat] = bbox
      const sw = [blon, blat]
      const nw = [blon, tlat]
      const ne = [tlon, tlat]
      const se = [tlon, blat]
      return `POLYGON((${sw[0].toFixed(4)} ${sw[1].toFixed(4)},${nw[0].toFixed(4)} ${nw[1].toFixed(4)},${ne[0].toFixed(
        4
      )} ${ne[1].toFixed(4)},${se[0].toFixed(4)} ${se[1].toFixed(4)},${sw[0].toFixed(4)} ${sw[1].toFixed(4)}))`
    },
    querySamples(pageNo = 1, pageSize = 5, filters = {}) {
      this.classesOfResultDisabled = false
      this.instrumentsOfResultDisabled = false
      const values = this.form.getFieldsValue()
      var params = {
        pageSize,
        pageNo,
        datasetId: values.datasetName == undefined ? '' : this.datasets[values.datasetName],
        sampleSize: '',
        startTime: values.dateRange == undefined ? '' : values.dateRange[0].format('YYYY-MM-DD'),
        endTime: values.dateRange == undefined ? '' : values.dateRange[1].format('YYYY-MM-DD'),
        sampleQuality: '',
        sampleLabeler: '',
        labelBbox: '',
        imageType: values.imageType == undefined ? '' : values.imageType,
        instrument: values.instrument == undefined ? '' : values.instrument.join(','),
        trnValueTest: '',
        wkt:
          values.maxValueLong && values.minValueLong && values.maxValueLat && values.minValueLat
            ? this.bbox2WKT([values.minValueLong, values.minValueLat, values.maxValueLong, values.maxValueLat])
            : '',
        codes: values.classes == undefined ? '' : values.classes.join(','),
      }
      console.log(params)
      if (
        Object.values(filters).some((v) => {
          return v.length > 0
        })
      ) {
        for (const key of Object.keys(filters)) {
          switch (key) {
            case 'classNames':
              if (filters.classNames.length > 0) params.codes = filters.classNames.join(',')
              break
            case 'instrument':
              if (filters.instrument.length > 0) params.instrument = filters.instrument.join(',')
              break
            case 'datasetName':
              if (filters.datasetName.length > 0) params.datasetId = filters.datasetName.join(',')
              break
            case 'postInstrument':
              if (filters.postInstrument.length > 0) params.postInstrument = filters.postInstrument.join(',')
              break
            case 'preInstrument':
              if (filters.preInstrument.length > 0) params.preInstrument = filters.preInstrument.join(',')
              break
            default:
              break
          }
        }
      }
      var apiUrl
      switch (this.form.getFieldValue('taskType')) {
        case 'od':
          apiUrl = this.$urlFactory.getApiURL('QUERY_OD_SAMPLES')
          break
        case 'sc':
          apiUrl = this.$urlFactory.getApiURL('QUERY_SC_SAMPLES')
          break
        case 'lc':
          apiUrl = this.$urlFactory.getApiURL('QUERY_LC_SAMPLES')
          break
        case 'cd':
          apiUrl = this.$urlFactory.getApiURL('QUERY_CD_SAMPLES')
          params.postInstrument = ''
          params.preImageType = values.imageType == undefined ? '' : values.imageType
          params.postImageType = values.imageType == undefined ? '' : values.imageType
          params.preInstrument = ''
          this.instrumentsOfResultDisabled = true
          break
        case 'td':
          apiUrl = this.$urlFactory.getApiURL('QUERY_TD_SAMPLES')
          this.classesOfResultDisabled = true
          break
        default:
          this.$message.info('目前暂不支持此样本类型查询!')
      }
      axios.get(apiUrl, { params }).then((response) => {
        const { data } = response
        console.log(data);
        this.tableDatasource = data.result.records.map((v) => {
          v.taskType = this.form.getFieldValue('taskType')
          v.key = v.id
          v.size = v.sampleHeight == undefined ? '' : v.sampleHeight + ' x ' + v.sampleWidth
          if (v.bbox == null) v.bbox = Math.floor(Math.random() * 4) % 2 == 0 ? [112, 23, 113, 24] : null
          else if (v.bbox.indexOf('POLYGON') >= 0) v.bbox = this.convertBboxToPolygon(v.bbox)
          else if (v.bbox.indexOf('POINT') >= 0) {
            v.bbox = this.convertBboxToPoint(v.bbox)
          } else {
            v.bbox = Math.floor(Math.random() * 4) % 2 == 0 ? [112, 23, 113, 24] : null
          }
          v.eyeClicked = this.imgVisibleIdArray.includes(v.id) ? true : false
          v.thumbnail =
            v.thumb != null
              ? this.getThumbNail(v.thumb[0], this.form.getFieldValue('taskType'))
              : this.getThumbNail(v.thumb, this.form.getFieldValue('taskType'))
          v.thumb = this.getThumbNail(v.thumb, this.form.getFieldValue('taskType'))

          if (v.classNames == undefined) {
            return v
          } else {
            const classCodes = v.classCodes.split(',')
            v.classNames = this.classes
              .filter((c) => {
                return classCodes.includes(c[1])
              })
              .map((v) => {
                return v[0]
              })
              .join('，')
            return v
          }

        })
        this.pagination.total = data.result.total
        this.cesiumController.removeAll()
        this.imageLoader.removeAllImage()
      })
    },
    querySamplesFilterInfo(filters) {
      const values = this.form.getFieldsValue()
      var params = {
        datasetId: values.datasetName == undefined ? '' : this.datasets[values.datasetName],
        imageType: values.imageType == undefined ? '' : values.imageType,
        instrument: values.instrument == undefined ? '' : values.instrument.join(','),
        trnValueTest: '',
        wkt:
          values.maxValueLong && values.minValueLong && values.maxValueLat && values.minValueLat
            ? this.bbox2WKT([values.minValueLong, values.minValueLat, values.maxValueLong, values.maxValueLat])
            : '',
        codes: values.classes == undefined ? '' : values.classes.join(','),
      }
      if (
        Object.values(filters).some((v) => {
          return v.length > 0
        })
      ) {
        for (const key of Object.keys(filters)) {
          switch (key) {
            case 'classNames':
              params.codes = filters.classNames.join(',')
              break
            case 'instrument':
              params.instrument = filters.instrument.join(',')
              break
            case 'datasetName':
              params.datasetId = filters.datasetName.join(',')
              break
            case 'postInstrument':
              params.postInstrument = filters.postInstrument.join(',')
              break
            case 'preInstrument':
              params.preInstrument = filters.preInstrument.join(',')
              break
            default:
              break
          }
        }
      }
      var apiUrl
      switch (this.form.getFieldValue('taskType')) {
        case 'od': {
          apiUrl = this.$urlFactory.getApiURL('QUERY_OD_SAMPLES_FILTER')
          params['sampleSize'] = ''
          params.startTime = values.dateRange == undefined ? '' : values.dateRange[0].format('YYYY-MM-DD')
          params.endTime = values.dateRange == undefined ? '' : values.dateRange[1].format('YYYY-MM-DD')
          params.sampleQuality = ''
          params.sampleLabeler = ''
          params.labelBbox = ''
          params.imageType = values.imageType == undefined ? '' : values.imageType
          break
        }
        case 'sc':
          apiUrl = this.$urlFactory.getApiURL('QUERY_SC_SAMPLES_FILTER')
          break
        case 'lc':
          apiUrl = this.$urlFactory.getApiURL('QUERY_LC_SAMPLES_FILTER')
          params.startTime = values.dateRange == undefined ? '' : values.dateRange[0].format('YYYY-MM-DD')
          params.endTime = values.dateRange == undefined ? '' : values.dateRange[1].format('YYYY-MM-DD')
          params.sampleQuality = ''
          params.sampleLabeler = ''
          params.labelBbox = ''
          params.imageType = values.imageType == undefined ? '' : values.imageType
          break
        case 'cd':
          apiUrl = this.$urlFactory.getApiURL('QUERY_CD_SAMPLES_FILTER')
          params.startTime = values.dateRange == undefined ? '' : values.dateRange[0].format('YYYY-MM-DD')
          params.endTime = values.dateRange == undefined ? '' : values.dateRange[1].format('YYYY-MM-DD')
          params.sampleQuality = ''
          params.sampleLabeler = ''
          params.labelBbox = ''
          params.preImageType = values.imageType == undefined ? '' : values.imageType
          params.postImageType = values.imageType == undefined ? '' : values.imageType
          params.postInstrument = ''
          params.preInstrument = ''
          break
        case 'td':
          apiUrl = this.$urlFactory.getApiURL('QUERY_TD_SAMPLES_FILTER')
          params.startTime = values.dateRange == undefined ? '' : values.dateRange[0].format('YYYY-MM-DD')
          params.endTime = values.dateRange == undefined ? '' : values.dateRange[1].format('YYYY-MM-DD')
          params.sampleQuality = ''
          params.sampleLabeler = ''
          params.labelBbox = ''
          params.imageType = values.imageType == undefined ? '' : values.imageType
          break
        default:
          this.$message.error('目前暂不支持此样本类型查询!')
      }
      axios.get(apiUrl, { params }).then((response) => {
        const {
          data: { result },
        } = response
        if (
          Object.values(filters).some((v) => {
            return v.length > 0
          })
        ) {
          this.tableFilterOptions.ids = result.ids
        } else {
          this.tableFilterOptions = result
        }
        this.$nextTick(() => {
          this.rowSelection.selectedRowKeys = this.rowSelection.selectedRowKeys.filter((v) => {
            return this.currentAvailableKeys.indexOf(v) != -1
          })
        })
      })
    },
    convertBboxToPolygon(bbox) {
      var coordinateArray = bbox.split('((')[1].split('))')[0].split(',')
      var pointArray = []
      for (let corrdinate of coordinateArray) {
        var point = corrdinate.split(' ')
        pointArray.push(point)
      }
      return pointArray
    },
    convertBboxToPoint(bbox) {
      var coordinateArray = bbox.split('(')[1].split(')')[0].split(' ')
      for (let i in coordinateArray) coordinateArray[i] = parseFloat(coordinateArray[i])
      return coordinateArray
    },
    querySampleDetail(sampleId) {
      var apiUrl
      switch (this.form.getFieldValue('taskType')) {
        case 'od':
          apiUrl = this.$urlFactory.getApiURL('QUERY_OD_SAMPLES_DETAIL')
          break
        case 'sc':
          apiUrl = this.$urlFactory.getApiURL('QUERY_SC_SAMPLES_DETAIL')
          break
        case 'lc':
          apiUrl = this.$urlFactory.getApiURL('QUERY_LC_SAMPLES_DETAIL')
          break
        case 'cd':
          apiUrl = this.$urlFactory.getApiURL('QUERY_CD_SAMPLES_DETAIL')
          break
        case 'td':
          apiUrl = this.$urlFactory.getApiURL('QUERY_TD_SAMPLES_DETAIL')
          break
        default:
          this.$message.info('目前暂不支持此样本类型查询!')
      }
      axios
        .get(apiUrl, {
          params: {
            id: sampleId,
          },
        })
        .then((response) => {
          const {
            data: [detail],
          } = response
          this.sampleDetail = detail
          this.sampleDetail.taskType = this.form.getFieldValue('taskType')
          this.detailThumb = this.getThumbNail(this.sampleDetail.thumb, this.sampleDetail.taskType)[0]
          this.detailModalVisible = true
        })
    },
    showOrderList() {
      if (this.rowSelection.selectedRowKeys.length == 0) {
        this.$message.info(this.$t('no_sample_warn'))
        return
      }
      this.noteChecked = false
      var apiUrl = this.$urlFactory.getApiURL('QUERY_USER_NOTES')
      axios
        .post(apiUrl, {
          taskType: this.form.getFieldValue('taskType'),
          sampleId: this.rowSelection.selectedRowKeys,
        })
        .then((response) => {
          const {
            data: { result },
          } = response
          this.submitSampleArray = Object.keys(result)
          console.log(Object.values(result))
          this.specificationSampleArray = Object.values(result).filter((v) => {
            return v != null
          })
          console.log(this.specificationSampleArray)
          this.userNotesModalVisible = true
          this.stepCurrent = 0
          this.nextButtonDisabled = false
        })
    },
    next() {
      if (!this.noteChecked) {
        this.$message.error(this.$t('check_agreement'))
        return
      }
      var apiUrl = this.$urlFactory.getApiURL('CREATE_ORDER')
      axios
        .post(apiUrl, {
          userId: Vue.ls.get('Login_Userinfo').id,
          taskType: this.form.getFieldValue('taskType'),
          sampleId: this.rowSelection.selectedRowKeys,
        })
        .then(() => {
          this.$nextTick(() => {
            this.nextButtonDisabled = true
          })
          this.$message.success(this.$t('submit_success'))
          this.stepCurrent++
          setTimeout(() => {
            this.submitSampleArray = []
            this.specificationSampleArray = []
            this.userNotesModalVisible = false
            if (
              Vue.ls.get('Login_Userinfo').id == 'e9ca23d68d884d4ebb19d07889727dae' ||
              Vue.ls.get('Login_Userinfo').id == '429dcae30a418daa08f4d97b28183ca2'
            ) {
              console.log('跳转到管理员界面！')
              this.$router.push({
                path: '/orderListAdmin',
              })
            } else
              this.$router.push({
                path: '/orderList',
              })
          }, 3000)
        })
    },
    onCheckboxChange(e) {
      this.noteChecked = e.target.checked
    },
    getThumbNail(thumb, taskType) {
      var apiUrl = this.$urlFactory.getApiURL('QUERY_THUMB_PATH')
      if (thumb == null) {
        return '/luojiaSet/thumbnails/loc.png'
      } else if (!(thumb instanceof Array)) {
        return apiUrl + '?fileName=' + thumb + '&taskType=' + taskType
      } else {
        var apiArray = []
        for (let t of thumb) {
          apiArray.push(apiUrl + '?fileName=' + t + '&taskType=' + taskType)
        }
        return apiArray
      }
    },
    changeDetailThumb(thumb, taskType) {
      if (thumb instanceof Array) {
        if (thumb.length == 2) {
          var thumbArray = this.getThumbNail(thumb, taskType)
          thumbArray.splice(
            thumbArray.findIndex((t) => t == this.detailThumb),
            1
          )
          this.detailThumb = thumbArray[0]
          console.log(this.detailThumb)
        }
      }
    },
  },
  mounted() {
    this.$nextTick(() => {
      const viewer = this.$refs.cesiumViewer.getViewer()
      this.imageLoader = new ImageLoader(viewer)
      this.cesiumController = new CesiumController(viewer)
      this.shapeDrawer = new ShapeDrawer(viewer, (positions) => {
        this.form.setFieldsValue({
          minValueLong: positions[0],
          maxValueLong: positions[2],
          minValueLat: positions[1],
          maxValueLat: positions[3],
        })
      })
    })
    this.queryDatasets({ taskType: this.form.getFieldValue('taskType') })
    this.queryClasses(true,this.localeValue)
  },
  created() {
    this.localeValue = this.$i18n.locale
  },
  mixins: [AdjustHeightMixin],
}
</script>
<style scoped>
.mainBody {
  margin: 0 auto;
}
.siderLayout {
  background: #fff !important;
  border-right: 1px solid #f0f2f5;
  padding-top: 0px;
}
.ant-form-item {
  margin-bottom: 10px;
}
.pointer {
  cursor: pointer;
}
.detailTitle {
  background-color: #efeeec;
  text-align: center;
  padding: 8px;
  border: 1px solid #ccc;
  margin-bottom: 0;
}
ul {
  padding: 0;
}
li {
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
  word-break: break-all;
}
.detailRow {
  border-bottom: 1px solid #ccc;
  border-right: 1px solid #ccc;
  border-left: 1px solid #ccc;
  display: flex;
}
.cart {
  float: right;
  display: flex;
  align-items: center;
}
.userNote {
  display: flex;
  flex-direction: row;
  padding: 7px;
}
</style>
<style>
.ant-table-filter-dropdown .ant-dropdown-menu {
  max-height: 256px;
}
.ant-table-thead > tr > th,
.ant-table-tbody > tr > td {
  padding: 16px 10px;
}
.steps-action {
  margin-top: 24px;
}
</style>