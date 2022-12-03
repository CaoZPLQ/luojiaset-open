import Vue from 'vue'
import Router from 'vue-router'
import { constantRouterMap } from '@/config/router.config'

Vue.use(Router)

//
const originPush = Router.prototype.push

Router.prototype.push = function(location){
  return originPush.call(this, location).catch(err=>err)
}

//

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})