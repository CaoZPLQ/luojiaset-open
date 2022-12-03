<template>
  <div class="user-wrapper" :class="theme">
    <span>
      <!--&lt;!&ndash;<a class="logout_title" target="_blank" href="http://jeecg-boot.mydoc.io">&ndash;&gt;-->
        <!--&lt;!&ndash;<a-icon type="question-circle-o"></a-icon>&ndash;&gt;-->
      <!--&lt;!&ndash;</a>&ndash;&gt;-->
      <a-radio-group :defaultValue=localeValue @change="changeLocale">
        <a-radio-button key="cn" :value=DEFAULT_LANG>中文</a-radio-button>
        <a-radio-button key="en" :value=EN_LANG>English</a-radio-button>
      </a-radio-group>
    </span>
    <header-notice class="action"/>
    <a-dropdown>
      <span class="action action-full ant-dropdown-link user-dropdown-menu">
        <a-avatar class="avatar" size="small" :src="getAvatar()"/>
        <span v-if="isDesktop()">{{$t('user_menu_welcome')}}{{ nickname() }}</span>
      </span>
      <a-menu slot="overlay" class="user-dropdown-menu-wrapper">
        <a-menu-item key="0">
          <router-link :to="{ name: 'account-center' }">
            <a-icon type="user"/>
            <span>{{$t('user_menu_account_center')}}</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="1">
          <router-link :to="{ name: 'account-settings-base' }">
            <a-icon type="setting"/>
            <span>{{$t('user_menu_account_setting')}}</span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="2"  @click="systemSetting">
          <a-icon type="tool"/>
          <span>{{$t('user_menu_system_setting')}}</span>
        </a-menu-item>
        <a-menu-item key="3" @click="updatePassword">
          <a-icon type="setting"/>
          <span>{{$t('user_menu_password_setting')}}</span>
        </a-menu-item>
        <a-menu-item key="4" @click="updateCurrentDepart">
          <a-icon type="cluster"/>
          <span>{{$t('user_menu_depart_setting')}}</span>
        </a-menu-item>
        <a-menu-divider/>
        <a-menu-item key="5" @click="handleLogout">
          <a-icon type="logout"/>
          <span>{{$t('user_menu_logout')}}</span>
        </a-menu-item>
      </a-menu>
    </a-dropdown>
    <!--<span class="action">-->
      <!--<a class="logout_title" href="javascript:;" @click="handleLogout">-->
        <!--<a-icon type="logout"/>-->
        <!--<span v-if="isDesktop()">&nbsp;{{$t('user_menu_logout')}}</span>-->
      <!--</a>-->
    <!--</span>-->
    <user-password ref="userPassword"></user-password>
    <depart-select ref="departSelect" :closable="true" title="部门切换"></depart-select>
    <setting-drawer ref="settingDrawer" :closable="true" title="系统设置"></setting-drawer>
  </div>
</template>

<script>
  import HeaderNotice from './HeaderNotice'
  import UserPassword from './UserPassword'
  import SettingDrawer from "@/components/setting/SettingDrawer";
  import DepartSelect from './DepartSelect'
  import { mapActions, mapGetters } from 'vuex'
  import { mixinDevice } from '@/utils/mixin.js'
  import { DEFAULT_LANG, EN_LANG, setup} from "@/locales"

  export default {
    name: "UserMenu",
    mixins: [mixinDevice],
    components: {
      HeaderNotice,
      UserPassword,
      DepartSelect,
      SettingDrawer
    },
    props: {
      theme: {
        type: String,
        required: false,
        default: 'dark'
      }
    },
    data() {
      return {
        DEFAULT_LANG,
        EN_LANG,
      }
    },
    computed: {
      localeValue() {
        return this.$store.getters.localeValue
      }
    },
    methods: {
      ...mapActions(["Logout"]),
      ...mapGetters(["nickname", "avatar","userInfo"]),
      getAvatar(){
        return window._CONFIG['imgDomainURL']+"/"+this.avatar()
      },
      handleLogout() {
        const that = this

        this.$confirm({
          title: this.$t('message_warning'),
          content: this.$t('user_menu_warning_logout'),
          onOk() {
            return that.Logout({}).then(() => {
                window.location.href="/luojiaSet";
              //window.location.reload()
            }).catch(err => {
              that.$message.error({
                title: this.$t('message_error'),
                description: err.message
              })
            })
          },
          onCancel() {
          },
        });
      },
      updatePassword(){
        let username = this.userInfo().username;
        this.$refs.userPassword.show(username)
      },
      updateCurrentDepart(){
        this.$refs.departSelect.show()
      },
      systemSetting(){
        this.$refs.settingDrawer.showDrawer()
      },
      changeLocale(e) {
        setup(e.target.value)
        this.$store.dispatch('SetLocaleValue', e.target.value)
      }
    }
  }
</script>

<style scoped>
  .logout_title {
    color: inherit;
    text-decoration: none;
  }
</style>