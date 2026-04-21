<template>
  <div class="content-card">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="24" style="display: flex; justify-content: space-between; align-items: center">
        <h3>题目管理</h3>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增题目
        </el-button>
      </el-col>
    </el-row>
    
    <el-form :inline="true" :model="searchForm" class="search-form">
      <el-form-item label="科目">
        <el-select v-model="searchForm.subjectId" placeholder="请选择科目" clearable style="width: 150px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="题型">
        <el-select v-model="searchForm.typeId" placeholder="请选择题型" clearable style="width: 150px">
          <el-option v-for="t in questionTypes" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="难度">
        <el-select v-model="searchForm.difficulty" placeholder="请选择难度" clearable style="width: 120px">
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    
    <el-table :data="tableData" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="subjectName" label="科目" width="120" />
      <el-table-column prop="typeName" label="题型" width="100" />
      <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip />
      <el-table-column prop="score" label="分值" width="80" />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <span :style="{ color: getDifficultyColor(row.difficulty) }">
            {{ getDifficultyText(row.difficulty) }}
          </span>
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
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link @click="handleView(row)">查看</el-button>
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
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属科目" prop="subjectId">
              <el-select v-model="form.subjectId" placeholder="请选择科目" style="width: 100%">
                <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题目类型" prop="typeId">
              <el-select v-model="form.typeId" placeholder="请选择题型" style="width: 100%" @change="handleTypeChange">
                <el-option v-for="t in questionTypes" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分值">
              <el-input-number v-model="form.score" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" style="width: 100%">
                <el-option label="简单" :value="1" />
                <el-option label="中等" :value="2" />
                <el-option label="困难" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        
        <el-form-item label="选项" v-if="isOptionType">
          <div v-for="(option, index) in form.optionList" :key="index" style="margin-bottom: 10px; display: flex; align-items: center">
            <span style="width: 30px; font-weight: bold">{{ String.fromCharCode(65 + index) }}.</span>
            <el-input v-model="option.content" placeholder="选项内容" style="flex: 1" />
            <el-checkbox v-model="option.isSelected" :label="String.fromCharCode(65 + index)" style="margin-left: 10px" @change="(val) => handleOptionCheck(index, val)">
              正确答案
            </el-checkbox>
            <el-button v-if="form.optionList.length > 2" type="danger" link style="margin-left: 10px" @click="handleRemoveOption(index)">
              删除
            </el-button>
          </div>
          <el-button type="primary" link @click="handleAddOption">+ 添加选项</el-button>
        </el-form-item>
        
        <el-form-item label="参考答案" prop="answer">
          <el-input v-model="form.answer" type="textarea" :rows="2" :placeholder="answerPlaceholder" />
        </el-form-item>
        
        <el-form-item label="答案解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="请输入答案解析（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <el-dialog v-model="viewDialogVisible" title="题目详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="科目">{{ viewData.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="题型">{{ viewData.typeName }}</el-descriptions-item>
        <el-descriptions-item label="分值">{{ viewData.score }}分</el-descriptions-item>
        <el-descriptions-item label="难度">{{ getDifficultyText(viewData.difficulty) }}</el-descriptions-item>
        <el-descriptions-item label="题目内容" :span="2">
          {{ viewData.content }}
        </el-descriptions-item>
        <el-descriptions-item label="选项" v-if="viewData.optionList && viewData.optionList.length > 0" :span="2">
          <div v-for="(opt, idx) in viewData.optionList" :key="idx" style="margin-bottom: 5px">
            {{ String.fromCharCode(65 + idx) }}. {{ opt.content }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="参考答案" :span="2">
          {{ viewData.answer }}
        </el-descriptions-item>
        <el-descriptions-item label="答案解析" :span="2" v-if="viewData.analysis">
          {{ viewData.analysis }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestionPage, getQuestionTypes, getSubjectList, createQuestion, updateQuestion, deleteQuestion, getQuestionById } from '@/api/admin'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const subjects = ref([])
const questionTypes = ref([])
const viewData = ref({})

const searchForm = reactive({
  subjectId: null,
  typeId: null,
  difficulty: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  subjectId: null,
  typeId: null,
  content: '',
  optionList: [{ label: 'A', content: '', isSelected: false }, { label: 'B', content: '', isSelected: false }],
  answer: '',
  analysis: '',
  score: 1,
  difficulty: 1
})

const rules = {
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  typeId: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入参考答案', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑题目' : '新增题目')

const isOptionType = computed(() => {
  const type = questionTypes.value.find(t => t.id === form.typeId)
  return type && (type.code === 'SINGLE' || type.code === 'MULTIPLE')
})

const answerPlaceholder = computed(() => {
  const type = questionTypes.value.find(t => t.id === form.typeId)
  if (!type) return '请输入参考答案'
  switch (type.code) {
    case 'SINGLE': return '请输入正确选项（如：A）'
    case 'MULTIPLE': return '请输入正确选项（如：ABC）'
    case 'FILL': return '请输入填空答案'
    case 'ESSAY': return '请输入参考答案（仅供参考）'
    default: return '请输入参考答案'
  }
})

const getDifficultyText = (d) => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[d] || '未知'
}

const getDifficultyColor = (d) => {
  const map = { 1: '#67c23a', 2: '#e6a23c', 3: '#f56c6c' }
  return map[d] || '#606266'
}

const fetchData = async () => {
  const params = {
    current: pagination.current,
    size: pagination.size,
    ...searchForm
  }
  const res = await getQuestionPage(params)
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

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  searchForm.subjectId = null
  searchForm.typeId = null
  searchForm.difficulty = null
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.subjectId = null
  form.typeId = null
  form.content = ''
  form.optionList = [{ label: 'A', content: '', isSelected: false }, { label: 'B', content: '', isSelected: false }]
  form.answer = ''
  form.analysis = ''
  form.score = 1
  form.difficulty = 1
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  const res = await getQuestionById(row.id)
  const data = res.data
  form.id = data.id
  form.subjectId = data.subjectId
  form.typeId = data.typeId
  form.content = data.content
  form.answer = data.answer
  form.analysis = data.analysis
  form.score = data.score
  form.difficulty = data.difficulty
  
  if (data.optionList && data.optionList.length > 0) {
    form.optionList = data.optionList.map(opt => ({
      ...opt,
      isSelected: data.answer && data.answer.includes(opt.label)
    }))
  } else {
    form.optionList = [{ label: 'A', content: '', isSelected: false }, { label: 'B', content: '', isSelected: false }]
  }
  
  dialogVisible.value = true
}

const handleView = async (row) => {
  const res = await getQuestionById(row.id)
  viewData.value = res.data
  viewDialogVisible.value = true
}

const handleTypeChange = () => {
  form.answer = ''
  if (isOptionType.value) {
    if (!form.optionList || form.optionList.length < 2) {
      form.optionList = [{ label: 'A', content: '', isSelected: false }, { label: 'B', content: '', isSelected: false }]
    }
  }
}

const handleAddOption = () => {
  const index = form.optionList.length
  if (index < 8) {
    form.optionList.push({ label: String.fromCharCode(65 + index), content: '', isSelected: false })
  } else {
    ElMessage.warning('最多支持8个选项')
  }
}

const handleRemoveOption = (index) => {
  form.optionList.splice(index, 1)
  form.optionList.forEach((opt, idx) => {
    opt.label = String.fromCharCode(65 + idx)
  })
}

const handleOptionCheck = (index, checked) => {
  const type = questionTypes.value.find(t => t.id === form.typeId)
  if (type && type.code === 'SINGLE' && checked) {
    form.optionList.forEach((opt, idx) => {
      if (idx !== index) {
        opt.isSelected = false
      }
    })
  }
  form.answer = form.optionList.filter(opt => opt.isSelected).map(opt => opt.label).join('')
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (isOptionType.value) {
    const filledOptions = form.optionList.filter(opt => opt.content && opt.content.trim())
    if (filledOptions.length < 2) {
      ElMessage.warning('请至少填写2个有效选项')
      return
    }
    form.optionList = form.optionList.filter(opt => opt.content && opt.content.trim())
    if (isEdit.value) {
      form.answer = form.optionList.filter(opt => opt.isSelected).map(opt => opt.label).join('')
    }
  }
  
  loading.value = true
  try {
    if (isEdit.value) {
      await updateQuestion(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createQuestion(form)
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
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteQuestion(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchSubjects()
  fetchQuestionTypes()
  fetchData()
})
</script>
