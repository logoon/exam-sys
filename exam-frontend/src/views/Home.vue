<template>
  <div class="content-card">
    <el-row :gutter="20">
      <el-col :span="24">
        <h2 style="margin-bottom: 20px">欢迎使用模拟考试系统</h2>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <el-col :span="8" v-if="userStore.isAdmin">
        <el-card shadow="hover" style="cursor: pointer" @click="router.push('/subjects')">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>科目管理</span>
              <el-icon size="24" color="#409eff"><Collection /></el-icon>
            </div>
          </template>
          <div>管理考试科目</div>
        </el-card>
      </el-col>
      
      <el-col :span="8" v-if="userStore.isAdmin">
        <el-card shadow="hover" style="cursor: pointer" @click="router.push('/questions')">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>题目管理</span>
              <el-icon size="24" color="#67c23a"><Document /></el-icon>
            </div>
          </template>
          <div>管理考试题目（单选、多选、填空、简答）</div>
        </el-card>
      </el-col>
      
      <el-col :span="8" v-if="userStore.isAdmin">
        <el-card shadow="hover" style="cursor: pointer" @click="router.push('/papers-manage')">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>考卷管理</span>
              <el-icon size="24" color="#e6a23c"><CopyDocument /></el-icon>
            </div>
          </template>
          <div>创建和管理考卷</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" style="cursor: pointer" @click="router.push('/papers')">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>考卷列表</span>
              <el-icon size="24" color="#409eff"><Reading /></el-icon>
            </div>
          </template>
          <div>浏览并参加考试</div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover" style="cursor: pointer" @click="router.push('/exam-records')">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>考试记录</span>
              <el-icon size="24" color="#67c23a"><List /></el-icon>
            </div>
          </template>
          <div>查看历史考试记录</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 30px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>系统说明</span>
          </template>
          <div>
            <p><strong>题目类型：</strong></p>
            <ul>
              <li><strong>单选题：</strong>从选项中选择一个正确答案</li>
              <li><strong>多选题：</strong>从选项中选择多个正确答案</li>
              <li><strong>填空题：</strong>填写正确的答案文字</li>
              <li><strong>简答题：</strong>需要人工评分的主观题</li>
            </ul>
            <p style="margin-top: 15px"><strong>用户角色：</strong></p>
            <ul>
              <li><strong>管理员：</strong>可管理科目、题目、考卷</li>
              <li><strong>普通用户：</strong>可参加模拟考试、查看考试记录</li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
</script>
