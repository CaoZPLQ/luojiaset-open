<template>
  <a-layout class="mainBody">
    <a-layout-content>
      <a-card>
        <!-- 右上角的小Tag -->
        <div class="dataVolume">
          <table>
            <tr>
              <th>
                <a-icon type="database" theme="twoTone"></a-icon>
              </th>
              <td>
                {{$t('dataset_size')}}<br />
<!--                {{ // $t("data_exist") }}-->
                {{datasetInfo.dataSize}}
              </td>
            </tr>
          </table>
        </div>
        <!-- 数据集名称 -->
        <h2 class="title">
          {{ datasetInfo.name }}
        </h2>
        <!-- 数据集贡献栏 -->
        <span>{{$t('dataset_contributor')}}: {{datasetInfo.contributor }}</span>
        <a-divider></a-divider>
        <a-row :gutter="8">
          <!-- 左半边基本信息 -->
          <a-col :span="12">
            <h3 style="font-weight: bold">{{$t('data_info')}}</h3>
            <table class="InfoTable">
              <!-- <tr>
                              <th>
                                  数据量：
                              </th>
                              <td>
                                  20GB
                              </td>
                          </tr> -->
              <tr>
                <th>{{$t('sample_sum')}}：</th>
                <td>
                  {{ datasetInfo.sampleSum }}
                </td>
              </tr>
              <tr>
                <th>{{$t('sample_size')}}：</th>
                <td>
                  {{ datasetInfo.sampleSize }}
                </td>
              </tr>
              <tr>
                <th>{{$t('task_type')}}：</th>
                <td>
                  {{ $t(datasetInfo.taskType) }}
                </td>
              </tr>
              <tr>
                <th>{{$t('category')}}：</th>
                <td>
                  {{ datasetInfo.keyword }}
                </td>
              </tr>
              <tr>
                <th>{{$t('image_type')}}：</th>
                <td>
                  {{ datasetInfo.imageType }}
                </td>
              </tr>
              <tr>
                <th>{{$t('resolution')}}：</th>
                <td>
                  {{ datasetInfo.resolution }}
                </td>
              </tr>
              <tr>
                <th>{{$t('band_size')}}：</th>
                <td>
                  {{ datasetInfo.bandSize }}
                </td>
              </tr>
              <tr>
                <th>{{$t('image_form')}}：</th>
                <td>
                  {{ datasetInfo.imageForm }}
                </td>
              </tr>
              <tr>
                <th>{{$t('instrument')}}：</th>
                <td>
                  {{ datasetInfo.instrument }}
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <a :href="datasetInfo.datasetLink" target="_blank">{{$t('dataset_link')}}</a>
                  |
                  <a :href="datasetInfo.datasetLink" target="_blank">{{$t('download_link')}}</a>
                </td>
              </tr>
            </table>
          </a-col>
          <!-- 右半边联系信息 -->
          <a-col :span="12">
            <h3 style="font-weight: bold">{{$t('data_link_info')}}</h3>
            <table class="InfoTable">
              <tr>
                <th>{{$t('contacter')}}：</th>
                <td>{{ datasetInfo.contacter }}</td>
              </tr>
              <tr>
                <th>{{$t('phone_number')}}：</th>
                <td>{{ datasetInfo.phoneNumber }}</td>
              </tr>
              <tr>
                <th>{{$t('email')}}：</th>
                <td>{{ datasetInfo.email }}</td>
              </tr>
              <tr>
                <th>{{$t('dataset_owner')}}：</th>
                <td>{{datasetInfo.contributor}}</td>
              </tr>
              <tr>
                <th>{{$t('address')}}：</th>
                <td>{{ datasetInfo.address }}</td>
              </tr>
            </table>
          </a-col>
        </a-row>
        <h3 style="font-weight: bold">{{$t('dataset_description')}}</h3>
        <div>
          <p class="descParagraph">
            {{ datasetInfo.description }}
          </p>
          <h3 style="font-weight: bold">{{$t('dataset_copy_explanation')}}</h3>
          <div>
            <p class="descParagraph">
              {{$t('dataset_copy')}}：<mark>{{ datasetInfo.cite }}</mark>
            </p>
            <p class="descParagraph">
              {{$t('dataset_state')}}<br />
              <!--            <b>{{$t('data_normalization')}}</b><br />-->
              <!--            {{$t('Chinese_presentation')}}<br />-->
              <!--            {{$t('English_presentation')}}<br />-->
              <!--            <b>{{$t('acknowledgement_normalization')}}</b><br />-->
              <!--            {{$t('Chinese_acknowledgment')}}<br />-->
              <!--            {{$t('English_acknowledgment')}}-->
            </p>
          </div>
          <img :src="datasetInfo.overview" width="100%" style="padding: 15px" />
        </div>


      </a-card>
    </a-layout-content>
    <a-layout-sider width="350" class="siderLayout">
      <div style="padding: 24px">
        <h3 style="font-weight: bold; padding: 5px; background: #f2f2f2">{{$t('dataset_location')}}</h3>
        <cesium-viewer style="width: 100%; height: 262px" />
        <h3 style="font-weight: bold; padding: 5px; background: #f2f2f2; margin-top: 0.5em">{{$t('dataset_sum')}}</h3>

        <div id="echartsBar" style="width: auto; height: 400px"></div>

        <h3 style="font-weight: bold; padding: 5px; background: #f2f2f2; margin-top: 0.5em">{{$t('similar_dataset')}}</h3>
        <a-card class="alikeDataset">
          <a-card-grid style="width: 100%; cursor: pointer">{{$t('dataset')}} 1</a-card-grid>
          <a-card-grid style="width: 100%; cursor: pointer">{{$t('dataset')}} 2</a-card-grid>
          <a-card-grid style="width: 100%; cursor: pointer">{{$t('dataset')}} 3</a-card-grid>
          <a-card-grid style="width: 100%; cursor: pointer">{{$t('dataset')}} 4</a-card-grid>
        </a-card>
      </div>
    </a-layout-sider>
  </a-layout>
</template>

<script>
import CesiumViewer from './components/scene/CesiumViewer'
import axios from 'axios'
import {mapState, mapGetters} from 'vuex'

const taskTypes = {
  od: '目标识别',
  sc: '场景检索',
  lc: '地物分类',
  cd: '变化检测',
  '3d': '多视三维',
}

export default {
  components: {
    CesiumViewer,
  },
  data() {
    return {
      datasetInfo: {
        name: '',
        contributor: '',
        dataVolume: '',
        datasetLink: '',
        taskType: '',
        description: '',
        cite: '',
        sampleSum: '',
        sampleSize: '',
        imageType: '',
        bandSize: '',
        imageForm: '',
        instrument: '',
        resolution: '',
        contacter: '',
        phoneNumber: '',
        email: '',
        address: '',
        keyword: '',
        overview: '',
        echarts: null,
      },

      dataSource: {
        categoryData: [],
        valueData: [],
        length: 0},
    }
  },
  computed: {
    ...mapState([
        'localeValue',
      ]),
      ...mapGetters([
        'localeValue',
      ]),
    data_not_available: function () {
      return this.$t('data_exist')
    }
  },
  watch: {
    localeValue(val) {
      if(val === "en"){
        this.getClassNum(true)
      }
      else if(val == "zh") this.getClassNum(false)
    }
  },
  methods: {
    getClassNum(isEnglish) {
      this.dataSource.categoryData = []
      this.dataSource.valueData= []
      var { id, taskType } = this.$route.query
      var apiUrl = this.$urlFactory.getApiURL('QUERY_CLASSES_NUM')
      axios
        .get(apiUrl, {
          params: {
            datasetId: id,
            taskType: taskType,
            isEnglish: isEnglish,
          },
        })
        .then((res) => {
          const { data } = res
          for (let d of data.result) {
            this.dataSource.categoryData.push(Object.values(d)[0])
            this.dataSource.valueData.push(Object.values(d)[1])
          }
          this.dataSource.length = data.result.length
          this.drawEChartsBar()
        })
    },

    drawEChartsBar() {
      var option = {
        title: {
          text: this.dataSource.length + this.$t('category_number'),
          left: 15, //标题偏移的像素值
        },
        toolbox: {
          feature: {
            saveAsImage: {
              pixelRatio: 2,
            },
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        grid: {
          bottom: 90,
          containLabel:true
        },
        dataZoom: [
          {
            type: 'inside',
          },
          {
            type: 'slider',
          },
        ],
        xAxis: {
          data: this.dataSource.categoryData,
          silent: false,
          splitLine: {
            show: false,
          },
          splitArea: {
            show: false,
          },
        },
        yAxis: {

          splitArea: {
            show: false,
          },
        },
        series: [
          {
            type: 'bar',
            data: this.dataSource.valueData,
            // Set `large` for large data amount
            large: true,
          },
        ],
      }
      this.echarts =  this.$echarts.init(document.getElementById("echartsBar"), 'light').setOption(option);
    },
  },
  mounted() {
    var id = this.$route.query.id
    //TODO: 根据id获取数据集元信息，渲染该页数据项
    var apiUrl = this.$urlFactory.getApiURL('QUERY_DATASET_DETAIL')
    axios
      .get(apiUrl, {
        params: {
          datasetId: id,
        },
      })
      .then((response) => {
        const { data } = response
        this.datasetInfo.name = data[0].name
        this.datasetInfo.contributor = data[0].datasetCopy
        this.datasetInfo.datasetLink = data[0].datasetLink
        this.datasetInfo.taskType = taskTypes[data[0].taskType]
        this.datasetInfo.description = data[0].description == null ? '暂无' : data[0].description
        this.datasetInfo.cite = data[0].datasetCite == null ? '暂无' : data[0].datasetCite
        this.datasetInfo.sampleSum = data[0].sampleSum == null ? '暂无' : data[0].sampleSum
        this.datasetInfo.sampleSize = data[0].sampleSize == null ? '暂无' : data[0].sampleSize
        this.datasetInfo.imageType = data[0].imageType == null ? '暂无' : data[0].imageType
        this.datasetInfo.bandSize = data[0].bandSize == null ? '暂无' : data[0].bandSize
        this.datasetInfo.imageForm = data[0].imageForm == null ? '暂无' : data[0].imageForm
        this.datasetInfo.instrument = data[0].instrument == null ? '暂无' : data[0].instrument
        this.datasetInfo.resolution = data[0].resolution == null ? '暂无' : data[0].resolution
        this.datasetInfo.contacter = data[0].contacter == null ? '暂无' : data[0].contacter
        this.datasetInfo.phoneNumber = data[0].phoneNumber == null ? '暂无' : data[0].phoneNumber
        this.datasetInfo.email = data[0].email == null ? '暂无' : data[0].email
        this.datasetInfo.address = data[0].address == null ? '暂无' : data[0].address
        this.datasetInfo.keyword = data[0].keyword == null ? '暂无' : data[0].keyword
        this.datasetInfo.overview = data[0].overview == null ? '/luojiaSet/datasetInfo/SSDD/SSDD.png' : data[0].overview
        this.datasetInfo.dataSize = data[0].dataSize == 0 ?this.data_not_available:data[0].dataSize + 'MB'
        this.getClassNum(this.$i18n.locale == 'en')
      })
  },
  created() {
      // 初始化语言
      this.localeValue = this.$i18n.locale
  }
}
</script>

<style scoped>
.mainBody {
  /* width: 1080px; */
  /* width: 1258px; */
  margin: 0 auto;
}
.siderLayout {
  background: #fff !important;
  border-right: 1px solid #f0f2f5;
  padding-top: 0px;
}
.title {
  font: 20px '微软雅黑';
  color: #458fce;
}
.dataVolume {
  float: right;
  background: #f2f2f2;
}
.dataVolume tr,
.dataVolume td,
.dataVolume th {
  padding: 5px;
}
.InfoTable th {
  color: #9a9a9a;
  min-width: 78px;
  font-weight: normal;
  text-align: right;
  padding: 0px 4px 0px 0px;
  /* width: 70px; */
}
.InfoTable td {
  padding-top: 4px;
  padding-bottom: 4px;
}
.descParagraph {
  padding-left: 1em;
  text-indent: 2em;
  word-wrap: break-word;
}
.alikeDataset .ant-card-grid {
  padding: 10px;
}
</style>