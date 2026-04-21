<template>
  <div class="content-card">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="24" style="display: flex; justify-content: space-between; align-items: center">
        <h3>考卷管理</h3>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增考卷
        </el-button>
      </el-col>
    </el-row>
    
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="科目">
        <el-select v-model="searchForm.subjectId" placeholder="请选择科目" clearable style="width: 150px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="tableData" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="考卷名称" min-width="200" />
      <el-table-column prop="subjectName" label="科目" width="120" />
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column prop="passScore" label="及格分" width="80" />
      <el-table-column prop="duration" label="考试时长" width="100">
        <template #default="{ row }">
          {{ row.duration }}分钟
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link @click="handleView(row)">详情</el-button>
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 20px; justify-content: flex-end"
    />
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考卷名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入考卷名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属科目" prop="subjectId">
              <el-select v-model="form.subjectId" placeholder="请选择科目" style="width: 100%">
                <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number v-model="form.totalScore" :min="1" :max="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number v-model="form.passScore" :min="1" :max="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number v-model="form.duration" :min="1" :max="300" style="width: 100%">
                <template #suffix>分钟</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入考卷描述（可选）" />
        </el-form-item>
        
        <el-divider content-position="left">题目列表</el-divider>
        
        <div style="margin-bottom: 15px">
          <el-button type="primary" link @click="handleSelectQuestions">
            <el-icon><Plus /></el-icon>
            添加题目
          </el-button>
        </div>
        
        <el-table :data="form.questions" stripe style="width: 100%" max-height="300">
          <el-table-column label="序号" type="index" width="60" />
          <el-table-column prop="question.typeName" label="题型" width="100" />
          <el-table-column prop="question.content" label="题目内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="score" label="分值" width="80">
            <template #default="{ row, $index }">
              <el-input-number v-model="form.questions[$index].score" :min="1" :max="100" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link @click="handleRemoveQuestion($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <el-dialog v-model="selectDialogVisible" title="选择题目" width="900px">
      <el-form :inline="true" :model="questionSearchForm" style="margin-bottom: 15px">
        <el-form-item label="科目">
          <el-select v-model="questionSearchForm.subjectId" placeholder="请选择科目" clearable style="width: 150px">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="questionSearchForm.typeId" placeholder="请选择题型" clearable style="width: 150px">
            <el-option v-for="t in questionTypes" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchQuestions">搜索</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="questionTableData" stripe style="width: 100%" @selection-change="handleQuestionSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="typeName" label="题型" width="100" />
        <el-table-column prop="subjectName" label="科目" width="120" />
        <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="score" label="默认分值" width="80" />
      </el-table>
      
      <el-pagination
        v-model:current-page="questionPagination.current"
        v-model:page-size="questionPagination.size"
        :page-sizes="[10, 20, 50]"
        :total="questionPagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchQuestions"
        @current-change="fetchQuestions"
        style="margin-top: 15px; justify-content: flex-end"
      />
      
      <template #footer>
        <el-button @click="selectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSelectQuestions">确定添加</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="viewDialogVisible" title="考卷详情" width="800px">
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="考卷名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ viewData.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ viewData.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="及格分">{{ viewData.passScore }}分</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ viewData.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">
            {{ viewData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ viewData.description || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">题目列表</el-divider>
      
      <el-table :data="viewData.questions" stripe style="width: 100%">
        <el-table-column label="序号" type="index" width="60" />
        <el-table-column prop="question.typeName" label="题型" width="100" />
        <el-table-column prop="question.content" label="题目内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="80" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPaperPage, getPaperById, createPaper, updatePaper, deletePaper, updatePaperStatus, getSubjectList, getQuestionPage, getQuestionTypes } from '@/api/admin'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const subjects = ref([])
const questionTypes = ref([])
const viewData = ref({})
const selectedQuestions = ref([])

const searchForm = reactive({
  subjectId: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  name: '',
  subjectId: null,
  totalScore: 100,
  passScore: 60,
  duration: 60,
  description: '',
  questions: []
})

const rules = {
  name: [{ required: true, message: '请输入考卷名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  totalScore: [{ required: true, message: '请输入总分', trigger: 'blur' }],
  passScore: [{ required: true, message: '请输入及格分', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'blur' }]
}

const questionSearchForm = reactive({
  subjectId: null,
  typeId: null
})

const questionTableData = ref([])
const questionPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isEdit.value ? '编辑考卷' : '新增考卷')

const fetchData = async () => {
  const params = {
    current: pagination.current,
    size: pagination.size,
    ...searchForm
  }
  const res = await getPaperPage(params)
  tableData.value = res.data.records
  pagination.total = res.data.total
}

const fetchSubjects = async () => {
  const res = await getSubjectList()
  subjects.value = res.data
}

const fetchQuestionTypes = async () => {
  const res = await getQuestionTypes()
  questionTypes.value = res.data
}

const fetchQuestions = async () => {
  const params = {
    current: questionPagination.current,
    size: questionPagination.size,
    ...questionSearchForm,
    status: 1
  }
  const res = await getQuestionPage(params)
  questionTableData.value = res.data.records
  questionPagination.total = res.data.total
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.subjectId = null
  searchForm.status = null
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.subjectId = null
  form.totalScore = 100
  form.passScore = 60
  form.duration = 60
  form.description = ''
  form.questions = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  const res = await getPaperById(row.id, true)
  const data = res.data
  form.id = data.id
  form.name = data.name
  form.subjectId = data.subjectId
  form.totalScore = data.totalScore
  form.passScore = data.passScore
  form.duration = data.duration
  form.description = data.description
  form.questions = data.questions || []
  dialogVisible.value = true
}

const handleView = async (row) => {
  const res = await getPaperById(row.id, true)
  viewData.value = res.data
  viewDialogVisible.value = true
}

const handleSelectQuestions = () => {
  questionSearchForm.subjectId = form.subjectId
  selectedQuestions.value = []
  selectDialogVisible.value = true
  fetchQuestions()
}

const handleQuestionSelectionChange = (selection) => {
  selectedQuestions.value = selection
}

const handleConfirmSelectQuestions = () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  
  const existingIds = form.questions.map(q => q.questionId)
  selectedQuestions.value.forEach(question => {
    if (!existingIds.includes(question.id)) {
      form.questions.push({
        questionId: question.id,
        question: question,
        score: question.score || 1
      })
    }
  })
  
  selectDialogVisible.value = false
}

const handleRemoveQuestion = (index) => {
  form.questions.splice(index, 1)
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const submitData = { ...form }
    
    if (isEdit.value) {
      await updatePaper(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createPaper(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该考卷吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deletePaper(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleToggleStatus = (row) => {
  const status = row.status === 1 ? 0 : 1
  const action = status === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}该考卷吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await updatePaperStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchSubjects()
  fetchQuestionTypes()
  fetchData()
})
</script>
