<template>
  <div class="content-card">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="24" style="display: flex; justify-content: space-between; align-items: center">
        <h3>考试记录</h3>
      </el-col>
    </el-row>
    
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="考试状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px">
          <el-option label="进行中" :value="0" />
          <el-option label="已完成" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="tableData" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="paperName" label="考卷名称" min-width="200" />
      <el-table-column prop="totalScore" label="得分" width="100">
        <template #default="{ row }">
          <span :class="row.status === 1 && row.isPassed === 1 ? 'passed' : row.status === 1 ? 'failed' : ''">
            {{ row.status === 1 ? (row.totalScore || 0) : '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="isPassed" label="是否通过" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" :type="row.isPassed === 1 ? 'success' : 'danger'" size="small">
            {{ row.isPassed === 1 ? '通过' : '未通过' }}
          </el-tag>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
            {{ row.status === 1 ? '已完成' : '进行中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180">
        <template #default="{ row }">
          {{ row.endTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="用时" width="100">
        <template #default="{ row }">
          {{ formatDuration(row.duration) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" link @click="handleContinueExam(row)">
            继续考试
          </el-button>
          <el-button v-else type="info" link @click="handleViewResult(row)">
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :page-sizes="[10, 20, 50]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 20px; justify-content: flex-end"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExamRecords } from '@/api/user'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)

const searchForm = reactive({
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const formatDuration = (minutes) => {
  if (!minutes) return '-'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h > 0) {
    return `${h}时${m}分`
  }
  return `${m}分钟`
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const res = await getExamRecords(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    ElMessage.error('加载考试记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.status = null
  pagination.current = 1
  fetchData()
}

const handleContinueExam = (row) => {
  router.push(`/exam/${row.id}`)
}

const handleViewResult = (row) => {
  router.push(`/exam-result/${row.id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.passed {
  color: #67c23a;
  font-weight: bold;
}

.failed {
  color: #f56c6c;
  font-weight: bold;
}
</style>
