<template>
  <div :id="random">
    <div id="slider" v-show="sliderVisible"></div>
  </div>
</template>

<script>
//cesium 不支持import, 改用require
let Cesium = require('cesium/Cesium');
require('cesium/Widgets/widgets.css')
import imageryViewModels from './modules/ProviderViewModels'
import { tianDTAnno, tainDTVectorAnno, tianDTAnno_EN } from './modules/AnnotationLayerProviders'

export default {
  name: 'CesiumViewer',
  data() {
    return {
      sliderVisible: false,
      viewer: null,
      random: Math.random().toString(36).substring(2)
    }
  },
  computed:{
    Anno: function(){return this.$t('Anno_layer')}
  },
  watch: {
    Anno: function(n){
      if(n == 'eia_w'){
        this.viewer.imageryLayers.remove(this.viewer.imageryLayers.get(1), false)
        this.viewer.imageryLayers.addImageryProvider(tianDTAnno_EN)
      }
      else{
        this.viewer.imageryLayers.remove(this.viewer.imageryLayers.get(1), false)
        this.viewer.imageryLayers.addImageryProvider(tianDTAnno)
      }
    }
  },
  mounted() {
    this.initCesiumViewer()
  },
  methods: {
    //返回当前viewer
    getViewer() {
      return this.viewer
    },
    initCesiumViewer(){
      this.$nextTick(()=>{
        //设置静态资源目录
        // Cesium.buildModuleUrl.setBaseUrl('/static/Cesium-1.71/')
        //Initialize the viewer widget with several custom options and mixins.
        Cesium.Ion.defaultAccessToken =
          'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJmMWM1ZjExZi00Yzg5LTRlOTMtODViYS0yOTZmZmI0OGU2NjQiLCJpZCI6MTg5MDIsInNjb3BlcyI6WyJhc3IiLCJnYyJdLCJpYXQiOjE1NzQ2NjU2ODJ9.bPY0jro1abLzWRoT8Mj4CtH7e0B_dogToc2f5JDm-w0'
        this.viewer = new Cesium.Viewer(this.random, {
          sceneMode: Cesium.SceneMode.SCENE3D,
          baseLayerPicker: true,
          animation: false,
          infoBox: true,
          timeline: false,
          imageryProvider: false,
          // Show Columbus View map with Web Mercator projection
          mapProjection: new Cesium.WebMercatorProjection(),
        })

        //Add basic drag and drop functionality
        this.viewer.extend(Cesium.viewerDragDropMixin)

        //取消左下侧cesium ion logo
        this.viewer.cesiumWidget.creditContainer.style.display = 'none'

        //设置image和地形图层
        this.viewer.baseLayerPicker.viewModel.imageryProviderViewModels = imageryViewModels
        this.viewer.baseLayerPicker.viewModel.selectedImagery = this.viewer.baseLayerPicker.viewModel.imageryProviderViewModels[1]

        //是否叠加注记功能
        if(this.$t('Anno_layer') == 'cia_w'){
          this.viewer.imageryLayers.addImageryProvider(tianDTAnno)
        }else{
          this.viewer.imageryLayers.addImageryProvider(tianDTAnno_EN)
        }

        //设置初始视角
        this.viewer.camera.setView({
          // fromDegrees()方法，将经纬度和高程转换为世界坐标
          // destination: Cesium.Rectangle.fromDegrees(113.0149404672,30.0734572226,113.9181165740,30.9597805439),//west, south, east, north
          destination: Cesium.Cartesian3.fromDegrees(108, 35, 20000000), //west, south, east, north
          orientation:{
            // 指向
            heading:Cesium.Math.toRadians(0,0),
            // 视角
            pitch:Cesium.Math.toRadians(-90),
            roll:0.0
          }
        });

        window.viewer = this.viewer;
      })
    }
  },
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
#slider {
        position: absolute;
        left: 50%;
        top: 0;
        background-color: #d3d3d3;
        width: 5px;
        height: 100%;
        z-index: 999;
      }

#slider:hover {
        cursor: ew-resize;
      }
</style>