<template>
  <el-container class="main-layout">
    <el-aside width="220px">
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/home">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-sub-menu v-if="userStore.isAdmin" index="admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/subjects">
            <el-icon><Collection /></el-icon>
            <span>科目管理</span>
          </el-menu-item>
          <el-menu-item index="/questions">
            <el-icon><Document /></el-icon>
            <span>题目管理</span>
          </el-menu-item>
          <el-menu-item index="/papers-manage">
            <el-icon><CopyDocument /></el-icon>
            <span>考卷管理</span>
          </el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/papers">
          <el-icon><Reading /></el-icon>
          <span>考卷列表</span>
        </el-menu-item>
        <el-menu-item index="/exam-records">
          <el-icon><List /></el-icon>
          <span>考试记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-left">
          模拟考试系统
        </div>
        <div class="header-right">
          <div class="user-info">
            <el-avatar :size="32" icon="UserFilled" />
            <span>{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
            <el-badge :value="userStore.isAdmin ? '管理员' : '用户'" :type="userStore.isAdmin ? 'primary' : 'info'" />
          </div>
          <el-button type="text" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-header>
      
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>
