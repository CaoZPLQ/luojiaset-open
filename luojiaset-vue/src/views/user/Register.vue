<template>
  <div class="main user-layout-register">
    <h3><span>{{ $t('login_register') }}</span></h3>
    <a-form ref="formRegister" :autoFormCreate="(form)=>{this.form = form}" id="formRegister">
      <a-form-item
        fieldDecoratorId="username"
        :fieldDecoratorOptions="{rules: [{ required: true, message: this.$t('usernameNullMessage')}, { validator: this.checkUsername }], validateTrigger: ['change', 'blur']}">
        <a-input size="large" type="text" autocomplete="false" :placeholder="$t('usernamePlaceholder')"></a-input>
      </a-form-item>

      <a-popover placement="rightTop" trigger="click" :visible="state.passwordLevelChecked">
        <template slot="content">
          <div :style="{ width: '240px' }">
            <div :class="['user-register', passwordLevelClass]">{{ $t('passwordStrength') }}：<span>{{ passwordLevelName }}</span></div>
            <a-progress :percent="state.percent" :showInfo="false" :strokeColor=" passwordLevelColor "/>
            <div style="margin-top: 10px;">
              <span>{{ $t('passwordTips') }}</span>
            </div>
          </div>
        </template>
        <a-form-item
          fieldDecoratorId="password"
          :fieldDecoratorOptions="{rules: [{ required: false}, { validator: this.handlePasswordLevel }], validateTrigger: ['change', 'blur']}">
          <a-input size="large" type="password" @click="handlePasswordInputClick" autocomplete="false" :placeholder="$t('passwordPlaceholder')"></a-input>
        </a-form-item>
      </a-popover>

      <a-form-item
        fieldDecoratorId="password2"
        :fieldDecoratorOptions="{rules: [{ required: true, message: this.$t('passwordMessage') }, { validator: this.handlePasswordCheck }], validateTrigger: ['change', 'blur']}">

        <a-input size="large" type="password" autocomplete="false" :placeholder="$t('confirmPwdPlaceholder')"></a-input>
      </a-form-item>
      <a-form-item
        fieldDecoratorId="email"
        :fieldDecoratorOptions="{rules: [{ required: true, type: 'email', message: $t('emailMessage') }, { validator: this.handleEmailCheck }], validateTrigger: ['change', 'blur']}">
        <a-input size="large" type="text" :placeholder="$t('email')"></a-input>
      </a-form-item>

      <a-form-item fieldDecoratorId="country" mode="default"
                   :fieldDecoratorOptions="{rules: [{ required: true, message: $t('countryPlaceholder') }], validateTrigger: ['change', 'blur']}" >
        <a-select size="large" showSearch :show-arrow="false" :filter-option="filterOption" :placeholder="$t('countryPlaceholder')">
          <a-select-option v-for="c in countryNames" :key="c.code">{{ c.value }}</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item
        fieldDecoratorId="workplace"
        :fieldDecoratorOptions="{rules: [{ required: true, message: $t('workplaceMessage') }], validateTrigger: ['change', 'blur']}">
        <a-input size="large" type="text" :placeholder="$t('workplacePlaceholder')"></a-input>
      </a-form-item>



      <a-form-item
        fieldDecoratorId="mobile"
        :fieldDecoratorOptions="{rules: [{ required: true, pattern: /^1[3456789]\d{9}$/, message: this.$t('phoneMessage') }, { validator: this.handlePhoneCheck } ], validateTrigger: ['change', 'blur'] }">
        <a-input size="large" :placeholder="$t('phonePlaceholder')">
          <a-select slot="addonBefore" size="large" defaultValue="+86">
            <a-select-option value="+86">+86</a-select-option>
            <a-select-option value="+87">+87</a-select-option>
          </a-select>
        </a-input>
      </a-form-item>
      <!--<a-input-group size="large" compact>
            <a-select style="width: 20%" size="large" defaultValue="+86">
              <a-select-option value="+86">+86</a-select-option>
              <a-select-option value="+87">+87</a-select-option>
            </a-select>
            <a-input style="width: 80%" size="large" placeholder="11 位手机号"></a-input>
          </a-input-group>-->

      <a-row :gutter="16">
        <a-col class="gutter-row" :span="16">
          <a-form-item
            fieldDecoratorId="captcha"
            :fieldDecoratorOptions="{rules: [{ required: true, message: this.$t('captchaEnter') }], validateTrigger: 'blur'}">
            <a-input size="large" type="text" :placeholder="$t('Captcha')">
              <a-icon slot="prefix" type="mail" :style="{ color: 'rgba(0,0,0,.25)' }"/>
            </a-input>
          </a-form-item>
        </a-col>
        <a-col class="gutter-row" :span="8">
          <a-button
            class="getCaptcha"
            size="large"
            :disabled="state.smsSendBtn"
            @click.stop.prevent="getCaptcha"
            v-text="!state.smsSendBtn && this.$t('getCaptcha')||(state.time+' s')"></a-button>
        </a-col>
      </a-row>

      <a-form-item>
        <a-button
          size="large"
          type="primary"
          htmlType="submit"
          class="register-button"
          :loading="registerBtn"
          @click.stop.prevent="handleSubmit"
          :disabled="registerBtn">{{ $t('register') }}
        </a-button>
        <router-link class="login" :to="{ name: 'login' }">{{ $t('loginWithUserExisted') }}</router-link>
      </a-form-item>

    </a-form>
  </div>
</template>

<script>
  import {mixinDevice} from '@/utils/mixin.js'
  import {getSmsCaptcha} from '@/api/login'
  import {getAction, postAction} from '@/api/manage'
  import {checkOnlyUser} from '@/api/api'
  import axios from "axios";
  import {mapGetters, mapState} from "vuex";

  const levelNames = {
    0: '低',
    1: '低',
    2: '中',
    3: '强'
  }
  const levelClass = {
    0: 'error',
    1: 'error',
    2: 'warning',
    3: 'success'
  }
  const levelColor = {
    0: '#ff0000',
    1: '#ff0000',
    2: '#ff7e05',
    3: '#52c41a',
  }
  export default {
    name: "Register",
    components: {},
    mixins: [mixinDevice],
    data() {
      return {
        form: null,

        state: {
          time: 60,
          smsSendBtn: false,
          passwordLevel: 0,
          passwordLevelChecked: false,
          percent: 10,
          progressColor: '#FF0000'
        },
        registerBtn: false,
        countryNames: []
      }
    },
    computed: {
      passwordLevelClass() {
        return levelClass[this.state.passwordLevel]
      },
      passwordLevelName() {
        return levelNames[this.state.passwordLevel]
      },
      passwordLevelColor() {
        return levelColor[this.state.passwordLevel]
      },
      ...mapState([        'localeValue',      ]),      ...mapGetters([        'localeValue',      ]),
    },
    methods: {
      checkUsername(rule, value, callback) {
        var params = {
          username: value,
        };
        checkOnlyUser(params).then((res) => {
          if (res.success) {
            callback()
          } else {
            callback(this.$t('usernameExistMessage'))
          }
        })
      },
      handleEmailCheck(rule, value, callback) {
        var params = {
          email: value,
        };
        checkOnlyUser(params).then((res) => {
          if (res.success) {
            callback()
          } else {
            callback(this.$t('emailExist'))
          }
        })
      },
      handlePasswordLevel(rule, value, callback) {

        let level = 0
        let reg = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/;
        if (!reg.test(value)) {
          callback(new Error(this.$t('passwordWarn')))
        }
        // 判断这个字符串中有没有数字
        if (/[0-9]/.test(value)) {
          level++
        }
        // 判断字符串中有没有字母
        if (/[a-zA-Z]/.test(value)) {
          level++
        }
        // 判断字符串中有没有特殊符号
        if (/[^0-9a-zA-Z_]/.test(value)) {
          level++
        }
        this.state.passwordLevel = level
        this.state.percent = level * 30
        if (level >= 2) {
          if (level >= 3) {
            this.state.percent = 100
          }
          callback()
        } else {
          if (level === 0) {
            this.state.percent = 10
          }
          callback(new Error(this.$t('passwordStrengthWarn')))
        }
      },

      handlePasswordCheck(rule, value, callback) {
        let password = this.form.getFieldValue('password')
        //console.log('value', value)
        if (value === undefined) {
          callback(new Error(this.$t('passwordEnter')))
        }
        if (value && password && value.trim() !== password.trim()) {
          callback(new Error(this.$t('passwordConsistency')))
        }
        callback()
      },

      handlePhoneCheck(rule, value, callback) {
        var params = {
          phone: value,
        };
        checkOnlyUser(params).then((res) => {
          if (res.success) {
            callback()
          } else {
            callback(this.$t('phoneWarn'))
          }
        })
      },

      handlePasswordInputClick() {
        if (!this.isMobile()) {
          this.state.passwordLevelChecked = true
          return;
        }
        this.state.passwordLevelChecked = false
      },

      handleSubmit() {
        this.form.validateFields((err, values) => {
          if (!err) {
            var register = {
              username: values.username,
              password: values.password,
              email: values.email,
              phone: values.mobile,
              smscode: values.captcha,
              countryCode: values.countryCode,
              workplace: values.workplace
            };
            console.log(values)

            postAction("/sys/user/register", register).then((res) => {
              if (!res.success) {
                this.registerFailed(res.message)
              } else {
                this.$router.push({name: 'registerResult', params: {...values}})
              }
            })
          }
        })
      },

      getCaptcha(e) {
        e.preventDefault()
        let that = this
        this.form.validateFields(['email'], {force: true}, (err, values) => {
          // console.log(values)
            if (!err) {
              this.state.smsSendBtn = true;
              let interval = window.setInterval(() => {
                if (that.state.time-- <= 0) {
                  that.state.time = 60;
                  that.state.smsSendBtn = false;
                  window.clearInterval(interval);
                }
              }, 1000);
              const hide = this.$message.loading(this.$t('captchaSending'), 0);
              const params = {
                username: this.form.getFieldValue("username"),
                email: this.form.getFieldValue("email"),
                mobile: values.mobile,
                smsmode: "1"
              };
              postAction("/sys/sms", params).then((res) => {
                console.log(params)
                if (!res.success) {
                  this.registerFailed(res.message);
                  setTimeout(hide, 0);
                }
                setTimeout(hide, 500);
              }).catch(err => {
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
      registerFailed(message) {
        this.$notification['error']({
          message: this.$t('registerFail'),
          description: message,
          duration: 2,
        });

      },
      requestFailed(err) {
        this.$notification['error']({
          message: '错误',
          description: ((err.response || {}).data || {}).message || "请求出现错误，请稍后再试",
          duration: 4,
        });
        this.registerBtn = false;
      },
      queryCountries(){
        var params = {
          // locale:'en',
        }

        var apiUrl = "http://localhost:18066/geois-boot/system/sysCountryList/queryCountryList?locale=" + this.localeValue
        getAction(apiUrl,{params}).then((response) => {
          this.countryNames = response.result
        })
      },
      filterOption(input, option) {
        return (
          option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
        );
      }
    },

    mounted() {
      this.queryCountries(this.localeValue)
    },

    watch: {
      'state.passwordLevel'(val) {
        console.log(val)
      },

      localeValue(val) {
        this.localeValue = val
      }


    }
  }
</script>
<style lang="scss">
  .user-register {

    &.error {
      color: #ff0000;
    }

    &.warning {
      color: #ff7e05;
    }

    &.success {
      color: #52c41a;
    }

  }

  .user-layout-register {
    .ant-input-group-addon:first-child {
      background-color: #fff;
    }
  }
</style>
<style lang="scss" scoped>
  .user-layout-register {

    & > h3 {
      font-size: 16px;
      margin-bottom: 20px;
    }

    .getCaptcha {
      display: block;
      width: 100%;
      height: 40px;
    }

    .register-button {
      width: 50%;
    }

    .login {
      float: right;
      line-height: 40px;
    }
  }
</style>