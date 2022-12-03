<template>
  <div class="main-container">
    <div class="anchor-list">
    <a-anchor :offsetTop="200" @click="handleClick">
        <a-anchor-link href="#anchor-1" :title="$t('data_introduction')"/>
        <a-anchor-link href="#anchor-2" :title="$t('staff')"/>
        <a-anchor-link href="#anchor-3" :title="$t('terms_for_usage')"/>
        <a-anchor-link href="#anchor-4" :title="$t('contact_us')"/>
        <a-anchor-link href="#anchor-5" :title="$t('friendly_links')"/>
    </a-anchor>
    </div>
    <div class="main-body">
    <!-- 数据介绍 -->
    <div class="introduction-title" id="anchor-1">
        <span class="introduction-span">
            <h2 class="introduction-h2">{{$t('data_introduction')}}</h2>
            </span>
    </div>
    <div class="content-box">
    <b>LuoJiaSET</b>
    {{ $t('data_introduction_info')}}
    </div>

    <!-- 人员队伍 -->

    <div class="introduction-title" id="anchor-2">
        <span class="introduction-span">
            <h2 class="introduction-h2">{{ $t('staff') }}</h2>
            </span>
    </div>
    <div>
        <div class="content-box">
       {{ $t('staff_info_content')}}
        </div><br/><br/>
        <h2 style="text-align:center;"><b>{{ $t('experts') }}</b></h2>
        <a-list :grid="{ gutter:128, column:3}" :data-source="data1">
        <a-list-item slot="renderItem" slot-scope="item">
            <a :href="item.detail" target="_blank">
            <a-card hoverable style="height: 400px;text-align:center" :bordered="false">
                <img slot="cover" alt="example" :src="item.path" style="height: 300px;border-radius: 50%;object-fit: cover; object-position: center;"/>
                <a-card-meta >
                <template slot="title">
                      {{item.title}}
                    </template>
                    <template slot="description">
                       {{ item.description }}
                    </template>
                </a-card-meta>
            </a-card>
            </a>
        </a-list-item>
        </a-list><br/><br/>
        <h2 style="text-align:center;"><b>{{$t('staff_research')}}</b></h2>
        <a-list :grid="{ gutter:64, column:4}" :data-source="data2" >
        <a-list-item slot="renderItem" slot-scope="item">
           <a :href="item.detail" target="_blank">
            <a-card  hoverable style="height: 300px;;text-align:center" :bordered="false">
                <img slot="cover" alt="example" :src="item.path" style="height: 210px;border-radius: 50%;object-fit: cover; object-position: center;"/>
                <a-card-meta >
                    <template slot="title">
                      <a :href="item.detail" target="_blank">{{item.title}}</a>
                    </template>
                    <template slot="description">
                       {{ item.description }}
                    </template>
                </a-card-meta>
            </a-card>
           </a>
        </a-list-item>
        </a-list><br/><br/>
<!--        <h2 style="text-align:center;"><b>{{ $t('data_contributor') }}</b></h2>-->
<!--        <a-list :grid="{ gutter:128, column:3}" :data-source="data3" style="width: 100%">-->
<!--        <a-list-item slot="renderItem" slot-scope="item">-->
<!--            <a-card hoverable style="height: 400px;">-->
<!--                <img slot="cover" alt="example" :src="item.path" style="height: 300px"/>-->
<!--                <a-card-meta :title="item.title">-->
<!--                    <template slot="description">-->
<!--                       {{ item.description }}-->
<!--                    </template>-->
<!--                </a-card-meta>-->
<!--            </a-card>-->
<!--        </a-list-item>-->
<!--        </a-list>-->
    </div>


    <!-- 使用条款 -->
    <div class="introduction-title" id="anchor-3">
        <span class="introduction-span">
            <h2 class="introduction-h2">{{ $t('terms_for_usage') }}</h2>
            </span>
    </div>
    <div class="organization-box">
        <p class="organization-box-fl">
          {{ $t('clause one') }}
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
        </p>
    </div>

    <!-- 联系我们 -->
    <div class="introduction-title" id="anchor-4">
        <span class="introduction-span">
            <h2 class="introduction-h2">{{ $t('contact_us') }}</h2>
            </span>
    </div>
    <div class="organization-box">
        <div class="organization-box-fl">
            <ul>
                <li class="content-list">
                    <strong>武汉大学LuojiaSet样本库数据</strong>
                </li>
                <li class="content-list">
                  {{ $t('address_value') }}
                </li>
                <li class="content-list">
                  {{ $t('phone') }}：027-68778546
                </li>
                <li class="content-list">
                  {{ $t('fax') }}：027-68778086
                </li>
                <li class="content-list">
                  {{ $t('email') }}：luojiaset@163.com
                </li>
            </ul>
        </div>
        <div class="organization-box-fr">
            <img src="../../assets/images/WHU.jpg" height="300" width="300">
        </div>

    </div>



    <!-- 友情链接 -->
    <div class="introduction-title" id="anchor-5">
        <span class="introduction-span">
            <h2 class="introduction-h2">{{  $t('friendly_links') }}</h2>
            </span>
    </div>
    <div class="content-box">
      <p>
        <a target="_blank" href="http://www.escience.org.cn/">中国科技资源共享网</a><span>|</span>
        <a target="_blank" href="http://www.chinageoss.cn/">国家对地观测科学数据中心</a>
      </p>
    </div>
  </div>
  </div>
</template>

<script>
import img1 from '../../assets/images/members/s1l1.jpg'
import img2 from '../../assets/images/members/s1l2.jpg'
import img3 from '../../assets/images/members/s1l3.jpg'
import img4 from '../../assets/images/members/s2l2.jpg'
import img5 from '../../assets/images/members/s2l1.jpg'
import img6 from '../../assets/images/members/s2l3.jpg'
import img7 from '../../assets/images/members/s2l5.jpg'
import img8 from '../../assets/images/members/s2l6.png'
import img9 from '../../assets/images/members/s2l7.jpg'
import img10 from '../../assets/images/members/s2l8.jpg'
import img11 from '../../assets/images/members/s2l9.jpg'



export default {
  // name: 'home',
  data() {
    return {
      activeIndex: -1,
      anchorList: [
        { id: 1, name: '数据介绍' },
        { id: 2, name: '人员队伍' },
        { id: 3, name: '使用条款' },
        { id: 4, name: '联系我们' },
        { id: 5, name: '友情链接' },
      ],
      data1: [
        {
          title: "龚健雅",
          path: img1,
          description: "中国科学院院士，教授，博士生导师，武汉大学",
          detail: "http://www.jygong.com/"
        },
        {
          title: "乐鹏",
          path: img2,
          description: "武汉大学遥感信息工程学院副院长，教授，博士生导师",
          detail: "http://jszy.whu.edu.cn/pyue/zh_CN/index.htm"
        },
        {
          title: "胡翔云",
          path: img3,
          description: "工学博士，教授，博导，武汉大学",
          detail: "http://jszy.whu.edu.cn/huxiangyun/zh_CN/index.htm"
        },
      ],
      data2: [
        {
          title: "姜良存",
          path: img5,
          description: "硕士生导师，武汉大学",
          detail: 'http://jszy.whu.edu.cn/jiang/'
        },
        {
          title: "曹志鹏",
          path: img4,
          description: this.$t('whu_doctor'),
        },
        {
          title: "谭浩锋",
          path: img6,
          description: this.$t('whu_master'),
        },
        {
          title: "刘畅",
          path: img11,
          description: this.$t('whu_master'),
        },
        {
          title: "刘帅旗",
          path: img7,
          description: this.$t('whu_master'),
        },
        {
          title: "黄展飞",
          path: img8,
          description: this.$t('whu_master'),
        },
        {
          title: "喻顺河",
          path: img10,
          description: this.$t('whu_master'),
        },
        {
          title: "陈国栋",
          path: img9,
          description: this.$t('whu_master'),
        },
      ],
      data3: [
        {
          title: "龚健雅",
          path: img1,
          description: "中国科学院院士，教授，博士生导师，武汉大学",
        },
        {
          title: "乐鹏",
          path: img2,
          description: "武汉大学遥感信息工程学院副院长，教授，博士生导师",
        },
        {
          title: "胡翔云",
          path: img3,
          description: "工学博士，教授，博导，武汉大学",
        },
      ],
    }
  },
  methods: {
    // changeBgColor(index) {
    //   this.activeIndex = index
    // },
    handleClick(e, link) {
      e.preventDefault()
      console.log(link)
    },
    directOut(){
      console.log("test")
    }
  },
  mounted() {
    console.log(img1)
  }
}
</script>

<style scoped>
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

.anchor-list {
  width: 180px;
  margin-left: 90px;





  /* padding-top: 150px; */
}

a-anchor-link {
  width:200px
}

/* .colorChange {
  background-color: #0060be;
} */

.content-box {
  line-height: 32px;
  /* color: #666; */
  text-align: justify;
  font-size: 16px;
  text-indent: 30px;
}
.personal-box {
  margin: 15px;
}
.personal-box-info {
  display: flex;
  -webkit-box-flex: 1;
  flex: 1;
  margin-top: 20px;
  align-items: center;
  align-content: center;
}
.personal-name {
  color: #0060be;
  font-size: 23px;
  text-align: center;
  font-weight: 700;
  padding: 10px 0;
  display: block;
}
.content-list {
  list-style-type: none;
}
.personal-list {
  border: 1px solid #eaeaea;
  text-align: center;
  width: 20%;
  padding: 15px 0;
  margin-right: 10px;
  list-style-type: none;
  float: left;
}

.personal-info {
  font-size: 14px;
  text-align: center;
  line-height: 22px;
}

.organization-box {
  display: flex;
  background-color: #f5f5f5;
}

.organization-box-fl {
  line-height: 32px;
  /* color: #666; */
  text-align: justify;
  font-size: 16px;
  text-indent: 30px;
  margin-left: 20px;
  -webkit-box-flex: 1;
  flex: 1;
}

.organization-box-fr {
  vertical-align: middle;
  border: none;
}

.introduction-title {
  text-align: center;
  margin-bottom: 50px;
  border-bottom: 2px solid #eaeaea;
}

.introduction-span {
  display: inline-block;
  padding: 0 50px;
  margin-bottom: -3px;
  border-bottom: 4px solid #0060be;
  background-color: #fff;
}


.introduction-h2 {
  font-size: 28px;
  color: #0060be;
  position: relative;
  top: 35px;
  padding: 0 15px;
  /* z-index: 1000; */
  background-color: #fff;
}
</style>
