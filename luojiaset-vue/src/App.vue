<template>
  <a-config-provider :locale="locale">
    <div id="app">
      <router-view/>
    </div>
  </a-config-provider>
</template>
<script>
  import zhCN from 'ant-design-vue/lib/locale-provider/zh_CN'
  import enquireScreen from '@/utils/device'
  import moment from 'moment'
  import 'moment/locale/zh-cn'
  import {DEFAULT_LANG, setup} from './locales'
  import {mapState, mapGetters} from 'vuex'

  moment.locale('zh-cn')
  export default {
    data() {
      return {
        locale: zhCN
      }
    },
    computed: {
      ...mapState([
        'localeValue',
      ]),
      ...mapGetters([
        'localeValue',
      ])
    },
    watch: {
      localeValue(localeValue) {
        this.changeLocale(localeValue)
      }
    },
    methods: {
      moment,
      changeLocale(localeValue) {
        if (localeValue === DEFAULT_LANG) {
          this.locale = zhCN
          moment.locale('zh-cn')
        } else {
          this.locale = null
          moment.locale('en')
        }
      }
    },
    created() {
      let that = this
      // 初始化语言
      that.$store.dispatch('GetLocaleValue').then(info => {
        setup(this.localeValue)
      })
      // 初始化布局
      enquireScreen(deviceType => {
        // tablet
        if (deviceType === 0) {
          that.$store.commit('TOGGLE_DEVICE', 'mobile')
          that.$store.dispatch('setSidebar', false)
        }
        // mobile
        else if (deviceType === 1) {
          that.$store.commit('TOGGLE_DEVICE', 'mobile')
          that.$store.dispatch('setSidebar', false)
        }
        else {
          that.$store.commit('TOGGLE_DEVICE', 'desktop')
          that.$store.dispatch('setSidebar', true)
        }
      })
    }
  }
</script>
<style>
  #app {
    height: 100%;
  }
</style>