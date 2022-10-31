import { createRouter, createWebHistory } from 'vue-router'
import PkIndexView from '@/views/pk/PkIndexView'
import RanklistIndexView from '@/views/ranklist/RanklistIndexView'
import RecordIndexView from '@/views/record/RecordIndexView'
import UserBotIndexView from '@/views/user/bot/UserBotIndexView'
import NotFoundView from '@/views/error/NotFoundView'

const routes = [
  // 网址重定向
  {
    path: '/',
    name: "home",
    redirect: '/pk/',
  },
  {
    path: "/pk/",
    name: "pk_index",
    component: PkIndexView,
  },
  {
    path: "/ranklist/",
    name: "ranklist_index",
    component: RanklistIndexView,
  },
  {
    path: "/record/",
    name: "record_index",
    component: RecordIndexView,
  },
  {
    path: "/user/bot/",
    name: "user_bot_index",
    component: UserBotIndexView,
  },
  {
    path: "/404/",
    name: "404",
    component: NotFoundView,
  },
  // 上述匹配都失败，重定向到404页面
  {
    path: "/:catchAll(.*)",
    redirect: "/404/"
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
