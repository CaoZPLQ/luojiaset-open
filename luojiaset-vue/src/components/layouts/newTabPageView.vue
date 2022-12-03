<template>

</template>

<script>

  export default {
    name: "newTabPageView",
    data () {
      return {
        url: "",
        id:"",
      }
    },
    created () {
      this.goUrl()
    },
    updated () {
      this.goUrl()
    },
    watch: {
      $route(to, from) {
        this.goUrl();
      }
    },
    methods: {
      goUrl () {
        let url = this.$route.meta.url
        let id = this.$route.path
        this.id = id
        console.log("------url------"+url)
        if (url !== null && url !== undefined) {
          this.url = url;
          window.open(url,'_blank') // 新窗口打开外链接
        }
      }
    }
  }
</script>
