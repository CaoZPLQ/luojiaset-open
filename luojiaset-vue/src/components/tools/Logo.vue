<template>
  <div class="logo">
    <router-link :to="{name:'home'}">

      <!-- update-begin- author:sunjianlei --- date:20190814 --- for: logo颜色根据主题颜色变化 -->
      <img v-if="navTheme === 'dark'" src="~@/assets/logo-white.png" alt="logo">
      <!-- <img v-else src="~@/assets/logo.svg" alt="logo"> -->
      <img v-else src="~@/assets/logo.png" alt="logo">

      <!-- update-begin- author:sunjianlei --- date:20190814 --- for: logo颜色根据主题颜色变化 -->

      <!-- <h1  v-if="showTitle">{{ title }}</h1> -->
    </router-link>
  </div>
</template>

<script>
  import { mixin } from '@/utils/mixin.js'

  export default {
    name: 'Logo',
    mixins: [mixin],
    props: {
      title: {
        type: String,
        default: 'LuoJiaSET',
        required: false
      },
      showTitle: {
        type: Boolean,
        default: true,
        required: false
      }
    }
  }
</script>
<style lang="scss" scoped>
  /*缩小首页布 局顶部的高度*/
  $height: 59px;
  
  // .title{
  //   font-size: 20px;
  //   font-family: "Chinese Quote", -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
  //   font-weight: 600;
  //   margin-left: 10px;
  //   color: rgba(0, 0, 0, 0.85);
  // }

  .sider {
    box-shadow: none !important;
    .logo {
      height: $height !important;
      line-height: $height !important;
      box-shadow: none !important;
      transition: background 300ms;

      a {
        color: white;
        &:hover {
          color: rgba(255, 255, 255, 0.8);
        }
      }
    }

    &.light .logo {
      background-color: #1890ff;
    }
  }
  .layout .top-nav-header-index .header-index-wide .header-index-left .logo.top-nav-header img {
    vertical-align: center;
    margin-top: 0px;    
    height: 90%;
    width: 150px;
  }
</style>