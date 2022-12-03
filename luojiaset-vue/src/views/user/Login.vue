<template>
  <div class="main">
    <a-form :form="form" class="user-layout-login" ref="formLogin" id="formLogin">
      <a-tabs
        :activeKey="customActiveKey"
        :tabBarStyle="{ textAlign: 'center', borderBottom: 'unset' }"
        @change="handleTabClick">
        <a-tab-pane key="tab1" :tab="$t('login_tab_username')">
          <a-form-item>
            <a-input
              size="large"
              v-decorator="['username',validatorRules.username,{ validator: this.handleUsernameOrEmail }]"
              type="text"
              :placeholder="$t('login_placeholder_username')">
              <a-icon slot="prefix" type="user" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>

          <a-form-item>
            <a-input
              v-decorator="['password',validatorRules.password]"
              size="large"
              type="password"
              autocomplete="false"
              :placeholder="$t('login_placeholder_password')">
              <a-icon slot="prefix" type="lock" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>




          <a-row :gutter="0">
            <a-col :span="14">
              <a-form-item>
                <a-input
                  v-decorator="['inputCode',validatorRules.inputCode]"
                  size="large"
                  type="text"
                  @change="inputCodeChange"
                  :placeholder="$t('login_placeholder_code')">
                  <a-icon slot="prefix" v-if=" inputCodeContent==verifiedCode " type="smile"
                          :style="{ color: 'rgba(0,0,0,.25)' }"/>
                  <a-icon slot="prefix" v-else type="frown" :style="{ color: 'rgba(0,0,0,.25)' }"/>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col :span="10">
              <j-graphic-code @success="generateCode" style="float: right"></j-graphic-code>
            </a-col>
          </a-row>


        </a-tab-pane>

        <a-tab-pane key="tab2" :tab="$t('login_tab_phone')">
          <a-form-item>
            <a-input
              v-decorator="['mobile',validatorRules.mobile]"
              size="large"
              type="text"
              :placeholder="$t('login_placeholder_phone')">
              <a-icon slot="prefix" type="mobile" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>

          <a-row :gutter="16">
            <a-col class="gutter-row" :span="16">
              <a-form-item>
                <a-input
                  v-decorator="['captcha',validatorRules.captcha]"
                  size="large"
                  type="text"
                  :placeholder="$t('login_placeholder_code')">
                  <a-icon slot="prefix" type="mail" :style="{ color: 'rgba(0,0,0,.25)' }"/>
                </a-input>
              </a-form-item>
            </a-col>
            <a-col class="gutter-row" :span="8">
              <a-button
                class="getCaptcha"
                tabindex="-1"
                :disabled="state.smsSendBtn"
                @click.stop.prevent="getCaptcha"
                v-text="!state.smsSendBtn && $t('login_get_code') || (state.time+' s')"></a-button>
            </a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>

      <a-form-item>
        <a-checkbox v-model="formLogin.rememberMe">{{ $t('login_remember_me') }}</a-checkbox>
        <router-link :to="{ name: 'alteration'}" class="forge-password" style="float: right;">
          {{ $t('login_forget_password') }}
        </router-link>
        <router-link :to="{ name: 'register'}" class="forge-password" style="float: right;margin-right: 10px" >
          {{ $t('login_register') }}
        </router-link>
      </a-form-item>

      <a-form-item style="margin-top:24px">

        <a-button
          size="large"
          type="primary"
          htmlType="submit"
          class="login-button"
          :loading="loginBtn"
          @click.stop.prevent="handleSubmit"
          :disabled="loginBtn">{{ $t('login_ok') }}
        </a-button>

      </a-form-item>
      <a-button
          size="large"
          type="primary"
          htmlType="submit"
          class="login-button"
          :loading="loginBtn"
          @click.stop.prevent="handleGuestSubmit"
          :disabled="loginBtn">{{ $t('login_message_guest_login')}}
        </a-button>
      <div class="user-login-other">
        <!--<span>其他登陆方式</span>-->
        <!--<a><a-icon class="item-icon" type="alipay-circle"></a-icon></a>-->
        <!--<a><a-icon class="item-icon" type="taobao-circle"></a-icon></a>-->
        <!--<a><a-icon class="item-icon" type="weibo-circle"></a-icon></a>-->
        <!--<router-link class="register" :to="{ name: 'register' }">-->
          <!--{{ $t('login_register') }}-->
        <!--</router-link>-->
        <a-radio-group :defaultValue=localeValue @change="changeLocale">
          <a-radio-button key="cn" :value="DEFAULT_LANG">中文</a-radio-button>
          <a-radio-button key="en" :value="EN_LANG">English</a-radio-button>
        </a-radio-group>
      </div>

    </a-form>

    <two-step-captcha
      v-if="requiredTwoStepCaptcha"
      :visible="stepCaptchaVisible"
      @success="stepCaptchaSuccess"
      @cancel="stepCaptchaCancel"></two-step-captcha>

    <a-modal
      :title="$t('login_modal_title_department')"
      :width="450"
      :visible="departVisible"
      :closable="false"
      :maskClosable="false">

      <template slot="footer">
        <a-button type="primary" @click="departOk">{{ $t('login_ok') }}</a-button>
      </template>

      <a-form>
        <a-form-item
          :labelCol="{span:4}"
          :wrapperCol="{span:20}"
          style="margin-bottom:10px"
          :validate-status="validate_status">
          <a-tooltip placement="topLeft">
            <template slot="title">
              <span>{{ $t('login_modal_info_choose_department') }}</span>
            </template>
            <a-avatar style="backgroundColor:#87d068" icon="gold"/>
          </a-tooltip>
          <a-select @change="departChange" :class="{'valid-error':validate_status=='error'}"
                    :placeholder="$t('login_placeholder_department')" style="margin-left:10px;width: 80%">
            <a-icon slot="suffixIcon" type="gold"/>
            <a-select-option
              v-for="d in departList"
              :key="d.id"
              :value="d.orgCode">
              {{ d.departName }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>


    </a-modal>

  </div>
</template>

<script>
  //import md5 from "md5"
  import api from '@/api'
  import TwoStepCaptcha from '@/components/tools/TwoStepCaptcha'
  import { mapActions } from "vuex"
  import { timeFix } from "@/utils/util"
  import Vue from 'vue'
  import { ACCESS_TOKEN } from "@/store/mutation-types"
  import JGraphicCode from '@/components/jeecg/JGraphicCode'
  import { putAction } from '@/api/manage'
  import { postAction } from '@/api/manage'
  import { getAction} from '@/api/manage'
  import { encryption } from '@/utils/encryption/aesEncrypt'
  import store from '@/store/'
  import { USER_INFO } from "@/store/mutation-types"
  import { DEFAULT_LANG, EN_LANG, setup } from "@/locales"

  export default {
    components: {
      TwoStepCaptcha,
      JGraphicCode
    },
    data() {
      return {
        DEFAULT_LANG,
        EN_LANG,
        customActiveKey: "tab1",
        loginBtn: false,
        // login type: 0 email, 1 username, 2 telephone
        loginType: 0,
        requiredTwoStepCaptcha: false,
        stepCaptchaVisible: false,
        form: this.$form.createForm(this),
        state: {
          time: 60,
          smsSendBtn: false,
        },
        formLogin: {
          username: "",
          password: "",
          captcha: "",
          mobile: "",
          rememberMe: true
        },
        validatorRules: {
          username: {rules: [{required: true, message: this.$t('login_message_input_username'), validator: 'click'}]},
          password: {rules: [{required: true, message: this.$t('login_message_input_password'), validator: 'click'}]},
          mobile: {rules: [{validator: this.validateMobile}]},
          captcha: {rule: [{required: true, message: this.$t('login_message_input_code')}]},
          inputCode: {
            rules: [{
              required: true,
              message: this.$t('login_message_input_code')
            }, {validator: this.validateInputCode}]
          }
        },
        verifiedCode: "",
        inputCodeContent: "",
        inputCodeNull: true,

        departList: [],
        departVisible: false,
        departSelected: "",
        currentUsername: "",
        validate_status: ""
      }
    },
    computed: {
      localeValue() {
        return this.$store.getters.localeValue
      }
    },
    created() {
      Vue.ls.remove(ACCESS_TOKEN)
      this.getRouterData();
      // update-begin- --- author:scott ------ date:20190805 ---- for:密码加密逻辑暂时注释掉，有点问题
      //this.getEncrypte();
      // update-end- --- author:scott ------ date:20190805 ---- for:密码加密逻辑暂时注释掉，有点问题

    },
    methods: {
      ...mapActions([ "Login", "Logout","PhoneLogin" ]),
      // handler
      handleUsernameOrEmail(rule, value, callback) {
        const regex = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\.[a-zA-Z0-9_-]{2,3}){1,2})$/
        if (regex.test(value)) {
          this.loginType = 0
        } else {
          this.loginType = 1
        }
        callback()
      },
      handleTabClick(key) {
        this.customActiveKey = key
        // this.form.resetFields()
      },
      handleSubmit() {
        let that = this
        let loginParams = {
          remember_me: that.formLogin
        }

        // 使用账户密码登陆
        if (that.customActiveKey === 'tab1') {
          that.form.validateFields(['username', 'password', 'inputCode'], {force: true}, (err, values) => {
            if (!err) {
              getAction("/sys/getEncryptedString",{}).then((res)=>{
                loginParams.username = values.username
                //loginParams.password = md5(values.password)
                //loginParams.password = encryption(values.password,that.encryptedString.key,that.encryptedString.iv)
                loginParams.password = values.password
                // update-begin- --- author:scott ------ date:20190805 ---- for:密码加密逻辑暂时注释掉，有点问题
                that.Login(loginParams).then((res) => {
                  this.departConfirm(res)
                }).catch((err) => {
                  that.requestFailed(err);
                })
              }).catch((err) => {
                that.requestFailed(err);
              });
            }
          })
          // 使用手机号登陆
        } else {
          that.form.validateFields([ 'mobile', 'captcha' ], { force: true }, (err, values) => {
            if (!err) {
              loginParams.mobile = values.mobile
              loginParams.captcha = values.captcha
              that.PhoneLogin(loginParams).then((res) => {
                console.log(res.result);
                this.departConfirm(res)
              }).catch((err) => {
                that.requestFailed(err);
              })

            }
          })
        }
      },
      handleGuestSubmit(){
        let that = this
        let loginParams = {
          remember_me: that.formLogin
        }

        // 游客登陆


              getAction("/sys/getEncryptedString",{}).then((res)=>{
                loginParams.username = "guest1"
                //loginParams.password = md5(values.password)
                //loginParams.password = encryption(values.password,that.encryptedString.key,that.encryptedString.iv)
                loginParams.password = "WHUgis12345678."
                // update-begin- --- author:scott ------ date:20190805 ---- for:密码加密逻辑暂时注释掉，有点问题
                that.Login(loginParams).then((res) => {
                  this.departConfirm(res)
                }).catch((err) => {
                  that.requestFailed(err);
                })
              }).catch((err) => {
                that.requestFailed(err);
              });


      },
      getCaptcha (e) {
        e.preventDefault();
        let that = this;
        this.form.validateFields([ 'mobile' ], { force: true },(err,values) => {
            if(!values.mobile){
              that.cmsFailed(this.$t('login_placeholder_phone'));
            }else if (!err) {
              this.state.smsSendBtn = true;
              let interval = window.setInterval(() => {
                if (that.state.time-- <= 0) {
                  that.state.time = 60;
                  that.state.smsSendBtn = false;
                  window.clearInterval(interval);
                }
              }, 1000);

              const hide = this.$message.loading(this.$t('login_message_code_loading'), 0);
              let smsParams = {};
              smsParams.mobile=values.mobile;
              smsParams.smsmode="0";
              postAction("/sys/sms",smsParams)
                .then(res => {
                  if(!res.success){
                    setTimeout(hide, 0);
                    this.cmsFailed(res.message);
                  }
                  console.log(res);
                  setTimeout(hide, 500);
                })
                .catch(err => {
                  setTimeout(hide, 1);
                  clearInterval(interval);
                  that.state.time = 60;
                  that.state.smsSendBtn = false;
                  this.requestFailed(err);
                });
            }
          }
        );
      },
      getCaptcha(e) {
        e.preventDefault()
        let that = this

        this.form.validateFields(['mobile'], {force: true},
          (err) => {
            if (!err) {
              this.state.smsSendBtn = true

              let interval = window.setInterval(() => {
                if (that.state.time-- <= 0) {
                  that.state.time = 60
                  that.state.smsSendBtn = false
                  window.clearInterval(interval)
                }
              }, 1000)

              const hide = this.$message.loading(this.$t('login_message_code_loading'), 0)
              this.$http.post(api.SendSms, {mobile: that.formLogin.mobile})
                .then(res => {
                  setTimeout(hide, 2500)
                  this.$notification['success']({
                    message: this.$t('message_info'),
                    description: this.$t('login_message_code_info') + res.result.captcha,
                    duration: 8
                  })
                })
                .catch(err => {
                  setTimeout(hide, 1)
                  clearInterval(interval)
                  that.state.time = 60
                  that.state.smsSendBtn = false
                  this.requestFailed(err)
                })
            }
          }
        )
      },
      stepCaptchaSuccess() {
        this.loginSuccess()
      },
      stepCaptchaCancel() {
        this.Logout().then(() => {
          this.loginBtn = false
          this.stepCaptchaVisible = false
        })
      },
      loginSuccess() {
        // update-begin- author:sunjianlei --- date:20190812 --- for: 登录成功后不解除禁用按钮，防止多次点击
        // this.loginBtn = false
        // update-end- author:sunjianlei --- date:20190812 --- for: 登录成功后不解除禁用按钮，防止多次点击
        this.$router.push({name: "dashboard"})
        this.$notification.success({
          message: this.$t('message_welcome'),
          description: `${timeFix()}，` + this.$t('login_welcome_back'),
        })
      },
      cmsFailed(err){
        this.$notification[ 'error' ]({
          message: this.$t('login_message_fail'),
          description:err,
          duration: 4,
        });
      },
      requestFailed(err) {
        this.$notification['error']({
          message: this.$t('login_message_fail'),
          description: ((err.response || {}).data || {}).message || err.message || this.$t('login_message_fail_info'),
          duration: 4,
        })
        this.loginBtn = false
      },
      validateMobile(rule, value, callback) {
        if (!value || new RegExp(/^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\d{8}$/).test(value)) {
          callback()
        } else {
          callback(this.$t('login_message_phone_error'))
        }

      },
      validateInputCode(rule, value, callback) {
        if (!value || this.verifiedCode == this.inputCodeContent) {
          callback()
        } else {
          callback(this.$t('login_message_code_error'))
        }
      },

      generateCode(value) {
        this.verifiedCode = value.toLowerCase()

      },
      inputCodeChange(e) {
        this.inputCodeContent = e.target.value
        if (!e.target.value || 0 == e.target.value) {
          this.inputCodeNull = true
        } else {
          this.inputCodeContent = this.inputCodeContent.toLowerCase()
          this.inputCodeNull = false
        }
      },
      departConfirm(res) {
        if (res.success) {
          let multi_depart = res.result.multi_depart
          //0:无部门 1:一个部门 2:多个部门
          if (multi_depart == 0) {
            this.loginSuccess()
            // this.$notification.warn({
            //   message: this.$t('message_info'),
            //   description: this.$t('login_modal_info_no_department'),
            //   duration: 3
            // })
          } else if (multi_depart == 2) {
            this.departVisible = true
            this.currentUsername = this.form.getFieldValue("username")
            this.departList = res.result.departs
          } else {
            this.loginSuccess()
          }
        } else {
          this.requestFailed(res)
          this.Logout()
        }
      },
      departOk() {
        if (!this.departSelected) {
          this.validate_status = 'error'
          return false
        }
        let obj = {
          orgCode: this.departSelected,
          username: this.form.getFieldValue("username")
        }
        putAction("/sys/selectDepart", obj).then(res => {
          if (res.success) {
            const userInfo = res.result.userInfo;
            Vue.ls.set(USER_INFO, userInfo, 7 * 24 * 60 * 60 * 1000);
            store.commit('SET_INFO', userInfo);
            //console.log("---切换组织机构---userInfo-------",store.getters.userInfo.orgCode);
            this.departClear()
            this.loginSuccess()
          } else {
            this.requestFailed(res)
            this.Logout().then(() => {
              this.departClear()
            })
          }
        })
      },
      departClear() {
        this.departList = []
        this.departSelected = ""
        this.currentUsername = ""
        this.departVisible = false
        this.validate_status = ''
      },
      departChange(value) {
        this.validate_status = 'success'
        this.departSelected = value
      },
      getRouterData(){
        this.$nextTick(() => {
          this.form.setFieldsValue({
            'username': this.$route.params.username
          });
        })
      },
      changeLocale(e) {
        setup(e.target.value)
        this.$store.dispatch('SetLocaleValue', e.target.value)
      }
    }
  }
</script>

<style lang="scss" scoped>

  .user-layout-login {
    label {
      font-size: 14px;
    }

    .getCaptcha {
      display: block;
      width: 100%;
      height: 40px;
    }

    .forge-password {
      font-size: 14px;
    }

    button.login-button {
      padding: 0 15px;
      font-size: 16px;
      height: 40px;
      width: 100%;
    }

    .user-login-other {
      text-align: left;
      margin-top: 24px;
      line-height: 22px;

      .item-icon {
        font-size: 24px;
        color: rgba(0, 0, 0, .2);
        margin-left: 16px;
        vertical-align: middle;
        cursor: pointer;
        transition: color .3s;

        &:hover {
          color: #1890ff;
        }
      }

      .register {
        float: right;
      }
    }
  }

</style>
<style>
  .valid-error .ant-select-selection__placeholder {
    color: #f5222d;
  }
</style>