<template>
  <div class="main-container">
    <div class="anchor-list">

      <a-anchor :offsetTop="200" @click="handleClick"  >
        <a-anchor-link href="#components-anchor-1" title="LuojiaSet Statistics" />
        <a-anchor-link href="#components-anchor-2" title="LuojiaSet API" />
        <a-anchor-link href="#components-anchor-3" title="LuojiaSet Juypter NoteBook"/>
      </a-anchor>


    </div>
    <div class="main-body">
      <div class="statistics" id="components-anchor-1">
<!--      <span class="statistics-span">-->
          <h2 class="statistics-h2">LuojiaSet Statistics</h2>
<!--          </span>-->
        <div class="content">{{ $t('luojiaSet_statistics') }}<br>
          <a-button class="button"><router-link :to="{ path: '/ClassResources' }">{{ $t('goto_resources') }}<a-icon style="margin-left: 20px" type="arrow-right"/></router-link></a-button>
        </div>

      </div>
      <div class="statistics" id="components-anchor-2">
<!--      <span class="statistics-span">-->
          <h2 class="statistics-h2">LuojiaSet API</h2>
<!--          </span>-->
        <div class="content">{{$t('luojiaSet_API')}}<br>
<!--          <a-button class="button"><a href="http://xxx.xxx.xxx.xxx:18066/geois-boot/swagger-ui.html">进入资源<a-icon style="margin-left: 20px" type="arrow-right"/></a> </a-button>-->
          <a-button class="button"><router-link :to="{path: '/10ec87389f11e21ee8f6b9cd1d6a44fa'}">{{ $t('goto_resources') }}<a-icon style="margin-left: 20px" type="arrow-right"/></router-link> </a-button>
        </div>

      </div>
      <div class="statistics" id="components-anchor-3">
<!--      <span class="statistics-span">-->
          <h2 class="statistics-h2">LuojiaSet Jupyter NoteBook</h2>
<!--          </span>-->
        <div class="content">
          <div>
            {{ $t('luojiaSet_jupyter_notebook') }}<br>
          <a-button class="button" @click="jupyterShow()">{{ $t('goto_resources') }}<a-icon style="margin-left: 20px" type="arrow-right"/></a-button>
        </div>
        </div>

      </div>
    </div>




  </div>



</template>

<script>
export default {
  name: "Resources",
  data() {
    return {
      url: 'http://xxx.xxx.xxx.xxx:8094'
  }
  },
  methods: {
    jupyterShow() {
      window.location.href=this.url
    }
  }
}
</script>

<style scoped>
.anchor-list {
  width: 200px;
  margin-left: 100px;
  /* padding-top: 150px; */
}

.main-container {
  /* width: 1258px; */
  margin:  auto;
  padding: 30px 110px;
  background-color: #fff;
  display: flex;
  flex-direction: row;
}
.main-body {
  /* width: 1080px; */
  width: 1258px;
  margin: auto;
  padding: 30px 110px;
  background-color: #fff;
}

.statistics {
  margin-top: 20px;
}

.statistics-h2 {
  /*text-align: center;*/
  font-size: 25px;
}

.statistics-span {
  display: inline-block;
  padding: 0 50px;
  margin-bottom: -3px;
  border-bottom: 4px solid #00ac9c;
  background-color: #fff;
}
.content {
  /*border: 1px solid #4CAF50;*/
  margin-top: 10px;
  padding: 10px;
  background-color: #efefef;
  line-height: 32px;
  /* color: #666; */
  text-align: justify;
  font-size: 16px;
  text-indent: 30px;
  border-radius: 10px;

}
.button {
  margin-top: 20px;
  margin-bottom: 10px;
  margin-left: 10px;
  border: 1px solid #FFFFFF;
  background-color: #1890ff;
  padding-top: 10px;
  padding-bottom: 10px;
  text-align: justify;
  font-size: 14px;
  letter-spacing: 3px;
  height: 40px;
  color: #FFFFFF;



}

</style>