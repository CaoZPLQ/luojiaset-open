<template>
  <div class="mainBody">
    <div class="banner0">
      <div class="banner0-textWrapper">
        <div class="banner0-Title">
          <img src="../assets/images/logoTest1.png" width="100%" />
        </div>
        <div class="banner0-Content">
          <span>{{ $t('home_description') }}</span>
        </div>
        <a-button style="font-size: 16px" @click="directToSearch()" ghost>Get More!</a-button>
      </div>
      <div class="banner0-statistic">
        <div style="margin: 30px">
          <span style="color: #236a9e; font-size: 32px; margin-left: 16px">
            <countTo :startVal="startVal" :endVal="datasetVal" :duration="3000"></countTo></span
          >{{ $t('nums_datasets') }}
        </div>
        <div style="margin: 30px">
          <span style="color: #236a9e; font-size: 32px"
            ><countTo :startVal="startVal" :endVal="sampleVal" :duration="3000"></countTo></span
          >{{ $t('nums_samples') }}
        </div>
      </div>
    </div>

    <div class="content1-wrapper">
      <div class="content1">
        <div class="titleWrapper">
          <h1 style="font-size: 32px">{{ $t('home_title') }}</h1>
        </div>
        <div class="wow animate__animated animate__fadeIn" data-wow-duration="2s" data-wow-delay="0.3s">
          <a-list :grid="{ gutter: 36, column: 3 }" :data-source="listData">
            <a-list-item slot="renderItem" slot-scope="item" style="vertical-align: top">
              <div style="display: flex; align-items: center; margin-bottom: 100px">
                <a-icon :type="item.icon" style="color: #22abed; font-size: 54px" />
                <div style="padding-left: 30px">
                  <h2>{{ $t(item.title) }}</h2>
                  <div>{{ $t(item.description) }}</div>
                </div>
              </div>
            </a-list-item>
          </a-list>
        </div>
      </div>
    </div>

    <div class="content2-wrapper">
      <div class="content2">
        <div class="titleWrapper">
          <h1 style="font-size: 32px">{{ $t('home_title2') }}</h1>
        </div>
        <div class="wow animate__animated animate__fadeIn" data-wow-duration="2s" data-wow-delay="0.3s">
          <a-row :gutter="8">
            <a-col :span="16">
              <a-carousel
                autoplay
                style="background-color: rgb(145, 163, 165)"
                arrows
                :afterChange="afterCarouselChange"
              >
                <div slot="prevArrow" class="custom-slick-arrow" style="left: 10px">
                  <a-icon type="left-circle" />
                </div>
                <div slot="nextArrow" class="custom-slick-arrow" style="right: 10px">
                  <a-icon type="right-circle" />
                </div>
                <div v-for="data in carouselData" :key="data.imgSrc">
                  <img :src="data.imgSrc" />
                </div>
              </a-carousel>
            </a-col>
            <a-col :span="8">
              <a-card style="height: 500px; overflow: auto">
                <template slot="title">
                  <div style="font-weight: 700">{{ $t('home_detail') }}:</div>
                </template>
                <a-card-grid style="width: 100%; font-weight: 700"
                  >{{ $t('new_dataset') }}：{{
                    /([\w\s%+-]+)(\.png|\.jpg)$/g.exec(carouselData[carouselIndex].imgSrc)[1]
                  }}</a-card-grid
                >
                <a-card-grid style="width: 100%">{{ carouselData[carouselIndex].desc }}</a-card-grid>
              </a-card>
            </a-col>
          </a-row>
        </div>
      </div>
    </div>

    <div class="footer-wrapper">
      <div class="footer">
        <div class="wow animate__animated animate__slideInDown" data-wow-duration="1s" data-wow-delay="0.3s">
          <a-row>
            <a-col :span="6">
              <img src="../assets/images/logoTest1.png" width="100%" />
              <p style="color: #999">{{ $t('home_description') }}</p>
            </a-col>
            <a-col :span="6">
              <h2 class="footer-title">{{ $t('product_services') }}</h2>
              <router-link :to="{ path: '/search' }" style="color: #999">{{ $t('查询服务') }}</router-link
              ><br /><br /><br />
              <router-link :to="{ path: '/datasets' }" style="color: #999">{{ $t('数据集') }}</router-link
              ><br /><br /><br />
              <router-link :to="{ path: '/orderList' }" style="color: #999">{{ $t('订单列表') }}</router-link
              ><br /><br /><br />
              <a target="_blank" href="http://xxx.xxx.xxx.xxx:18066/geois-boot/swagger-ui.html" style="color: #999">Api</a>
              <router-link :to="{ path: '/resources' }" style="color: #999">{{ $t('资源') }}</router-link><br /><br />
            </a-col>
            <a-col :span="6">
              <h2 class="footer-title">{{ $t('About') }}</h2>
              <router-link :to="{ path: '/aboutUs' }" style="color: #999">{{ $t('关于我们') }}</router-link
              ><br /><br /><br />
              <a target="_blank" href="#" style="color: #999">FAQ</a>
            </a-col>
            <a-col :span="6">
              <h2 class="footer-title">{{ $t('links') }}</h2>
              <a target="_blank" href="https://www.whu.edu.cn/" style="color: #999">{{ $t('links_1') }}</a
              ><br /><br /><br />
              <a target="_blank" href="http://rsgis.whu.edu.cn/" style="color: #999">{{ $t('links_2') }}</a
              ><br /><br /><br />
              <a target="_blank" href="https://www.escience.org.cn/" style="color: #999">{{ $t('links_3') }}</a
              ><br /><br /><br />
              <a target="_blank" href="http://www.chinageoss.cn/" style="color: #999">{{ $t('links_4') }}</a>
            </a-col>
          </a-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { WOW } from 'wowjs'
import countTo from 'vue-count-to';
import axios from 'axios'
export default {
  name: 'home',
  data(){
    return {
      carouselData: [
        {
          imgSrc: '/luojiaSet/carousel/AID.png',
          desc: `AID is a new large-scale aerial image dataset, by collecting sample images from Google Earth imagery. Note that although the Google Earth images are post-processed using RGB renderings from the original optical aerial images, it has proven that there is no significant difference between the Google Earth images with the real optical aerial images even in the pixel-level land use/cover mapping. Thus, the Google Earth images can also be used as aerial images for evaluating scene classification algorithms.
The new dataset is made up of the following 30 aerial scene types: Aairport, bare land, baseball field, beach, bridge, center, church, commercial, dense residential, desert, farmland, forest, industrial, meadow, medium residential, mountain, park, parking, playground, pond, port, railway station, resort, river, school, sparse residential, square, stadium, storage tanks and viaduct. All the images are labelled by the specialists in the field of remote sensing image interpretation, and some samples of each class are shown in Fig. In all, the AID dataset has a number of 10000 images within 30 classes.`
        },{
          imgSrc: '/luojiaSet/carousel/RSD46-WHU.png',
          desc: `RSD46-WHU is a larget-scale open dataset for scene classification in remote sensing images. The dataset is collected from Google Earth and Tianditu. The ground resolution of most classes is 0.5m, and the others are about 2m. There are 500-3000 images in each class. The RSD46-WHU dataset has a number of 11,7000 images with 46 classes.`
        },{
          imgSrc: '/luojiaSet/carousel/SAT-4 and SAT-6 airborne datasets.png',
          desc: `Images were extracted from the National Agriculture Imagery Program (NAIP) dataset. The NAIP dataset consists of a total of 330,000 scenes spanning the whole of the Continental United States (CONUS). We used the uncompressed digital Ortho quarter quad tiles (DOQQs) which are GeoTIFF images and the area corresponds to the United States Geological Survey (USGS) topographic quadrangles. The average image tiles are ~6000 pixels in width and ~7000 pixels in height, measuring around 200 megabytes each. The entire NAIP dataset for CONUS is ~65 terabytes. The imagery is acquired at a 1-m ground sample distance (GSD) with a horizontal accuracy that lies within six meters of photo-identifiable ground control points. The images consist of 4 bands - red, green, blue and Near Infrared (NIR). In order to maintain the high variance inherent in the entire NAIP dataset, we sample image patches from a multitude of scenes (a total of 1500 image tiles) covering different landscapes like rural areas, urban areas, densely forested, mountainous terrain, small to large water bodies, agricultural areas, etc. covering the whole state of California. An image labeling tool developed as part of this study was used to manually label uniform image patches belonging to a particular landcover class. Once labeled, 28x28 non-overlapping sliding window blocks were extracted from the uniform image patch and saved to the dataset with the corresponding label. We chose 28x28 as the window size to maintain a significantly bigger context, and at the same time not to make it as big as to drop the relative statistical properties of the target class conditional distributions within the contextual window. Care was taken to avoid interclass overlaps within a selected and labeled image patch.`
        },{
          imgSrc: '/luojiaSet/carousel/AID.png',
          desc: `AID is a new large-scale aerial image dataset, by collecting sample images from Google Earth imagery. Note that although the Google Earth images are post-processed using RGB renderings from the original optical aerial images, it has proven that there is no significant difference between the Google Earth images with the real optical aerial images even in the pixel-level land use/cover mapping. Thus, the Google Earth images can also be used as aerial images for evaluating scene classification algorithms.
The new dataset is made up of the following 30 aerial scene types: Aairport, bare land, baseball field, beach, bridge, center, church, commercial, dense residential, desert, farmland, forest, industrial, meadow, medium residential, mountain, park, parking, playground, pond, port, railway station, resort, river, school, sparse residential, square, stadium, storage tanks and viaduct. All the images are labelled by the specialists in the field of remote sensing image interpretation, and some samples of each class are shown in Fig. In all, the AID dataset has a number of 10000 images within 30 classes.`
        },
      ],
      testShow: false,
      carouselIndex: 0,
      startVal: 0,
      datasetVal: 0,
      sampleVal: 0,
      listData: [
        {
          icon: 'folder-open',
          title: "多任务遥感样本管理",
          description: "在云资源下多任务数据集遥感样本规范组织，持续发布，高可用，跨集合"
        },{
          icon: 'search',
          title: '跨数据集样本查询',
          description: '多维度、多语义跨数据集样本查询，可视化',
        },
        {
          icon: 'cloud-download',
          title: '定制化样本下载',
          description: '根据查询结果生成定制化跨数据集样本订单，管理员审核，数据压缩下载',
        },
        {
          icon: 'api',
          title: '规范样本库API',
          description: '样本信息统一规范组织，完善的类别体系，样本API标准输出',
        },
        {
          icon: 'bar-chart',
          title: '数据集统计分析',
          description: '数据集样本类别，尺寸，数量统计信息展示',
        },
        {
          icon: "highlight",
          title: "样本标注工具",
          description: "数据上传，数据切片，多用户灵活样本标注"
        }
      ],
    }
  },
  components: { countTo },
  methods: {
    afterCarouselChange(index){
      this.carouselIndex = index
    },
    directToSearch() {
      this.$router.push({
                path: '/search',
              })
    },
    getDataNumInfo(){
      var apiUrl = this.$urlFactory.getApiURL('QUERY_DATA_NUM')
      axios.get(apiUrl,{}).then((response) => {
        const { data } = response
        this.datasetVal = data.result.datasetNum
        this.sampleVal = data.result.dataNum
      })
    },

  },
  mounted() {
    new WOW().init(),
    this.getDataNumInfo()
    console.log(this.$i18n.locale)
  }
}
</script>

<style scoped>
.mainBody {
  /* width: 1000px; */
  /* width: 1258px; */
  margin: 0 auto;
}
.banner0 {
  width: 100%;
  height: 100vh;
  position: relative;
  text-align: center;
  border-color: #666;
  background-image: url('https://zos.alipayobjects.com/rmsportal/gGlUMYGEIvjDOOw.jpg');
  background-size: cover;
  background-attachment: fixed;
  background-position: center;
}
.banner0-textWrapper {
  display: inline-block;
  position: absolute;
  top: 30%;
  margin: auto;
  left: 0;
  right: 0;
  font-size: 16px;
  color: #fff;
  width: 550px;
}
.banner0-statistic {
  display: flex;
  position: relative;
  text-align: center;
  top: 75%;
  /* width: 50%; */
  font-size: 16px;
  color: #fff;
  justify-content: center;
  /* bottom:0; */
}
.banner0-Title {
  width: 450px;
  left: 30px;
  min-height: 60px;
  margin: auto;
  display: inline-block;
  font-size: 40px;
  position: relative;
  /*  */
}
.banner0-Content {
  margin-bottom: 45px;
  word-wrap: break-word;
  min-height: 24px;
  color: white;
}

.content1-wrapper {
  /* min-height: 764px; */
  height: 100%;
  position: relative;
  background-color: #ffffff;
}
.content1 {
  max-width: 1400px;
  margin: auto;
  padding: 128px 24px;
}
.titleWrapper {
  margin: 0 auto 64px;
  text-align: center;
}
.content2-wrapper {
  height: 100%;

  position: relative;

  will-change: transform;
  align-content: center;
  background-color: #fafafa;
}
.content2 {
  padding: 128px 24px;
  margin: auto;
  max-width: 1200px;
}
.ant-carousel >>> .custom-slick-arrow {
  width: 25px;
  height: 25px;
  font-size: 25px;
  color: #fff;
  background-color: rgba(31, 45, 61, 0.11);
  opacity: 0.3;
  z-index: 1;
}
.ant-carousel >>> .custom-slick-arrow:before {
  display: none;
}
.ant-carousel >>> .custom-slick-arrow:hover {
  opacity: 0.5;
}
.ant-carousel >>> .slick-slide {
  text-align: center;
  height: 500px;
  /* line-height: 160px; */
  background: #dee1e6;
  overflow: hidden;
  /* 图片居中 */
  display: flex;
  align-items: center;
  justify-content: center;
}
.ant-carousel >>> .slick-slide img {
  border: 5px solid #fff;
  display: block;
  margin: auto;
  max-width: 80%;
}
.footer-wrapper {
  background: #001529;
  overflow: hidden;
  position: relative;
  min-height: 360px;
  height: 100%;
}
.footer {
  max-width: 1200px;
  position: relative;
  margin: auto;
  padding: 64px 24px 80px;
  text-align: center;
  justify-content: center;
  color: white;
}
.footer-title {
  color: #fff;
  margin-bottom: 20px;
}
.title {
  font-weight: 700;
}
</style>
