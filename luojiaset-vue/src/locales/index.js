import Vue from 'vue'

import VueI18n from 'vue-i18n'
import en_us from './locale/en-US'
import zh_cn from './locale/zh-CN'

Vue.use(VueI18n)

export const DEFAULT_LANG = 'zh'
export const EN_LANG = 'en'

const locales = {
  zh: zh_cn,
  en: en_us,
}

const i18n = new VueI18n({
  locale: DEFAULT_LANG, // 语言标识
  messages: locales
})

export const setup = lang => {
  Object.keys(locales).forEach(lang => {
    document.body.classList.remove(`lang-${lang}`)
  })
  document.body.classList.add(`lang-${lang}`)
  document.body.setAttribute('lang', lang)

  Vue.config.lang = lang
  i18n.locale = lang
}

window.i18n = i18n

export default i18n