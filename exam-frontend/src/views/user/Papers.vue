<template>
  <div class="content-card">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="24" style="display: flex; justify-content: space-between; align-items: center">
        <h3>考卷列表</h3>
      </el-col>
    </el-row>
    
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="科目">
        <el-select v-model="searchForm.subjectId" placeholder="请选择科目" clearable style="width: 150px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="tableData" stripe style="width: 100%">
      <el-table-column prop="name" label="考卷名称" min-width="200" />
      <el-table-column prop="subjectName" label="科目" width="120" />
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column prop="passScore" label="及格分" width="80" />
      <el-table-column prop="duration" label="考试时长" width="100">
        <template #default="{ row }">
          {{ row.duration }}分钟
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
          <el-button type="success" link @click="handleStartExam(row)">开始考试</el-button>
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
    
    <el-dialog v-model="detailDialogVisible" title="考卷详情" width="800px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="考卷名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ detailData.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ detailData.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="及格分">{{ detailData.passScore }}分</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ detailData.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ detailData.questions?.length || 0 }}道</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detailData.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">题目列表</el-divider>
      
      <el-table :data="detailData.questions || []" stripe style="width: 100%" max-height="400">
        <el-table-column label="序号" type="index" width="60" />
        <el-table-column prop="question.typeName" label="题型" width="100" />
        <el-table-column prop="question.content" label="题目内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="80" />
      </el-table>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleStartExam(detailData)">开始考试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPaperPage, getUserPaperWithQuestions, startExam, getSubjectList } from '@/api/user'

const router = useRouter()
const tableData = ref([])
const subjects = ref([])
const detailDialogVisible = ref(false)
const detailData = ref({})

const searchForm = reactive({
  subjectId: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const fetchData = async () => {
  const params = {
    current: pagination.current,
    size: pagination.size,
    ...searchForm
  }
  const res = await getUserPaperPage(params)
  tableData.value = res.data.records
  pagination.total = res.data.total
}

const fetchSubjects = async () => {
  const res = await getSubjectList()
  subjects.value = res.data
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.subjectId = null
  pagination.current = 1
  fetchData()
}

const handleViewDetail = async (row) => {
  const res = await getUserPaperWithQuestions(row.id)
  detailData.value = res.data
  detailDialogVisible.value = true
}

const handleStartExam = async (row) => {
  ElMessageBox.confirm(
    `确定要开始考试【${row.name}】吗？\n考试时长：${row.duration}分钟\n总分：${row.totalScore}分`,
    '确认开始考试',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    detailDialogVisible.value = false
    try {
      const res = await startExam(row.id)
      router.push(`/exam/${res.data.id}`)
    } catch (e) {
      ElMessage.error('开始考试失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchSubjects()
  fetchData()
})
</script>
