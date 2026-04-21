<template>
  <div class="exam-container">
    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading" size="48"><Loading /></el-icon>
      <p style="margin-top: 20px">加载中...</p>
    </div>
    
    <div v-else class="exam-card">
      <div class="exam-header">
        <div>
          <span class="exam-title">{{ paperData.name }}</span>
          <span style="margin-left: 20px; color: #909399">总分：{{ paperData.totalScore }}分 | 及格分：{{ paperData.passScore }}分</span>
        </div>
        <div class="timer-box">
          <el-icon><Timer /></el-icon>
          <span>{{ formatTime(remainingTime) }}</span>
        </div>
      </div>
      
      <el-row :gutter="20">
        <el-col :span="18">
          <div class="question-list">
            <div v-for="(item, index) in questions" :key="item.questionId" class="question-card" :id="'question-' + index">
              <div class="question-header">
                <span class="question-order">{{ index + 1 }}.</span>
                <span class="question-type">{{ item.question.typeName }}</span>
                <span class="question-score">（{{ item.score }}分）</span>
                <span v-if="userAnswers[item.questionId] !== undefined && userAnswers[item.questionId] !== ''" class="answered-badge">已答</span>
              </div>
              
              <div class="question-content">{{ item.question.content }}</div>
              
              <div v-if="item.question.typeCode === 'SINGLE'" class="options-section">
                <div
                  v-for="(option, optIndex) in item.question.optionList"
                  :key="optIndex"
                  class="option-item"
                  :class="{ selected: userAnswers[item.questionId] === option.label }"
                  @click="selectSingleOption(item, option.label)"
                >
                  <span class="option-label">{{ option.label }}</span>
                  <span class="option-content">{{ option.content }}</span>
                </div>
              </div>
              
              <div v-else-if="item.question.typeCode === 'MULTIPLE'" class="options-section">
                <div
                  v-for="(option, optIndex) in item.question.optionList"
                  :key="optIndex"
                  class="option-item"
                  :class="{ selected: isMultiSelected(item.questionId, option.label) }"
                  @click="selectMultiOption(item, option.label)"
                >
                  <span class="option-label">{{ option.label }}</span>
                  <span class="option-content">{{ option.content }}</span>
                </div>
              </div>
              
              <div v-else-if="item.question.typeCode === 'FILL'" class="fill-section">
                <el-input
                  v-model="userAnswers[item.questionId]"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入答案"
                  @blur="saveAnswer(item.questionId)"
                />
              </div>
              
              <div v-else-if="item.question.typeCode === 'ESSAY'" class="essay-section">
                <el-input
                  v-model="userAnswers[item.questionId]"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入答案"
                  @blur="saveAnswer(item.questionId)"
                />
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="question-nav">
            <div class="nav-title">题目导航</div>
            <div class="nav-content">
              <div v-for="(group, typeName) in questionGroups" :key="typeName" class="question-group">
                <div class="group-title">{{ typeName }}（{{ group.length }}题）</div>
                <div class="group-items">
                  <div
                    v-for="(index, gIdx) in group"
                    :key="gIdx"
                    class="nav-item"
                    :class="{
                      current: currentQuestionIndex === index,
                      answered: userAnswers[questions[index].questionId] !== undefined && userAnswers[questions[index].questionId] !== ''
                    }"
                    @click="scrollToQuestion(index)"
                  >
                    {{ index + 1 }}
                  </div>
                </div>
              </div>
            </div>
            <div class="nav-footer">
              <div class="nav-info">
                已答：{{ answeredCount }} / {{ questions.length }} 题
              </div>
              <el-button type="primary" style="width: 100%" @click="handleSubmit">
                提交考试
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamQuestions, submitAnswer, finishExam } from '@/api/user'

const route = useRoute()
const router = useRouter()
const recordId = route.params.recordId

const loading = ref(true)
const paperData = ref({})
const questions = ref([])
const currentQuestionIndex = ref(0)
const userAnswers = reactive({})
const remainingTime = ref(0)
let timer = null

const questionGroups = computed(() => {
  const groups = {}
  questions.value.forEach((item, index) => {
    const typeName = item.question.typeName
    if (!groups[typeName]) {
      groups[typeName] = []
    }
    groups[typeName].push(index)
  })
  return groups
})

const answeredCount = computed(() => {
  return questions.value.filter(item => 
    userAnswers[item.questionId] !== undefined && userAnswers[item.questionId] !== ''
  ).length
})

const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const isMultiSelected = (questionId, label) => {
  const answer = userAnswers[questionId]
  return answer && answer.includes(label)
}

const selectSingleOption = (item, label) => {
  userAnswers[item.questionId] = label
  saveAnswer(item.questionId)
}

const selectMultiOption = (item, label) => {
  let answer = userAnswers[item.questionId] || ''
  if (answer.includes(label)) {
    answer = answer.replace(label, '')
  } else {
    answer = answer.split('').sort().join('') + label
  }
  userAnswers[item.questionId] = answer.split('').sort().join('')
  saveAnswer(item.questionId)
}

const saveAnswer = (questionId) => {
  const answer = userAnswers[questionId]
  submitAnswer(recordId, {
    questionId: questionId,
    userAnswer: answer || ''
  }).catch(() => {})
}

const scrollToQuestion = (index) => {
  currentQuestionIndex.value = index
  const element = document.getElementById(`question-${index}`)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const handleSubmit = () => {
  ElMessageBox.confirm(
    `已完成 ${answeredCount.value}/${questions.value.length} 题，确定要提交考试吗？`,
    '确认提交',
    {
      confirmButtonText: '确定提交',
      cancelButtonText: '继续答题',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await finishExam(recordId)
      ElMessage.success('考试提交成功！')
      router.push(`/exam-result/${recordId}`)
    } catch (e) {
      ElMessage.error('提交失败')
    }
  }).catch(() => {})
}

const loadExamData = async () => {
  loading.value = true
  try {
    const res = await getExamQuestions(recordId)
    paperData.value = res.data
    questions.value = res.data.questions || []
    remainingTime.value = res.data.duration * 60
    
    timer = setInterval(() => {
      remainingTime.value--
      if (remainingTime.value <= 0) {
        clearInterval(timer)
        ElMessage.warning('考试时间已到，正在自动提交...')
        finishExam(recordId).then(() => {
          router.push(`/exam-result/${recordId}`)
        })
      }
    }, 1000)
  } catch (e) {
    ElMessage.error('加载考试数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadExamData()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.exam-container {
  height: 100%;
  overflow-y: auto;
}

.loading-box {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.exam-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  min-height: calc(100% - 40px);
  margin: 20px;
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  background: white;
  z-index: 100;
}

.exam-title {
  font-size: 22px;
  font-weight: 600;
}

.timer-box {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 8px;
}

.question-list {
  padding: 20px;
}

.question-card {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.question-header {
  display: flex;
  align-items: baseline;
  margin-bottom: 15px;
}

.question-order {
  font-weight: bold;
  color: #409eff;
  margin-right: 10px;
  font-size: 18px;
}

.question-type {
  padding: 2px 8px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 10px;
}

.question-score {
  color: #909399;
  font-size: 14px;
}

.answered-badge {
  margin-left: 10px;
  padding: 2px 8px;
  background: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
  font-size: 12px;
}

.question-content {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 20px;
}

.options-section {
  margin-top: 15px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 12px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
}

.option-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.option-label {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
  font-weight: bold;
  color: #606266;
}

.option-item.selected .option-label {
  background: #409eff;
  color: white;
}

.option-content {
  flex: 1;
  line-height: 1.6;
}

.fill-section,
.essay-section {
  margin-top: 15px;
}

.question-nav {
  position: sticky;
  top: 20px;
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin: 20px 20px 20px 0;
}

.nav-title {
  padding: 15px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
  font-size: 16px;
}

.nav-content {
  padding: 15px;
  max-height: 500px;
  overflow-y: auto;
}

.question-group {
  margin-bottom: 15px;
}

.group-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.group-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.nav-item {
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.nav-item:hover {
  border-color: #409eff;
  color: #409eff;
}

.nav-item.current {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.nav-item.answered {
  background: #f0f9eb;
  border-color: #67c23a;
  color: #67c23a;
}

.nav-item.answered.current {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.nav-footer {
  padding: 15px;
  border-top: 1px solid #eee;
}

.nav-info {
  text-align: center;
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
}
</style>
