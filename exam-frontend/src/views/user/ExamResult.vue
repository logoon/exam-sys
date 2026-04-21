<template>
  <div class="content-card">
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="24" style="display: flex; justify-content: space-between; align-items: center">
        <h3>考试结果</h3>
        <el-button @click="router.push('/exam-records')">
          <el-icon><List /></el-icon>
          查看考试记录
        </el-button>
      </el-col>
    </el-row>
    
    <div v-if="loading" class="loading-box">
      <el-icon class="is-loading" size="48"><Loading /></el-icon>
      <p style="margin-top: 20px">加载中...</p>
    </div>
    
    <div v-else>
      <div class="result-card">
        <div :class="['result-status', recordData.isPassed === 1 ? 'passed' : 'failed']">
          {{ recordData.isPassed === 1 ? '恭喜，考试通过！' : '很遗憾，考试未通过' }}
        </div>
        
        <div :class="['result-score', recordData.isPassed === 1 ? 'passed' : 'failed']">
          {{ recordData.totalScore || 0 }}
          <span style="font-size: 24px">分</span>
        </div>
        
        <div class="result-info">
          <div class="result-info-item">
            <div class="result-info-value">{{ paperData.passScore }}</div>
            <div class="result-info-label">及格分</div>
          </div>
          <div class="result-info-item">
            <div class="result-info-value">{{ paperData.totalScore }}</div>
            <div class="result-info-label">试卷总分</div>
          </div>
          <div class="result-info-item">
            <div class="result-info-value">{{ formatDuration(recordData.duration) }}</div>
            <div class="result-info-label">用时</div>
          </div>
          <div class="result-info-item">
            <div class="result-info-value">{{ correctCount }}/{{ totalCount }}</div>
            <div class="result-info-label">正确率</div>
          </div>
        </div>
      </div>
      
      <el-card style="margin-top: 20px">
        <template #header>
          <span>答题详情</span>
        </template>
        
        <div class="question-list">
          <div v-for="(item, index) in questionAnswers" :key="item.questionId" class="question-card">
            <div class="question-header">
              <span class="question-order">{{ index + 1 }}.</span>
              <span class="question-type">{{ item.question?.typeName }}</span>
              <span class="question-score">（{{ item.score }}分）</span>
              <span :class="['result-badge', item.isCorrect === 1 ? 'correct' : item.isCorrect === 0 ? 'wrong' : 'pending']">
                {{ item.isCorrect === 1 ? '正确' : item.isCorrect === 0 ? '错误' : '待批改' }}
              </span>
            </div>
            
            <div class="question-content">{{ item.question?.content }}</div>
            
            <div v-if="item.question?.optionList && item.question.optionList.length > 0" class="options-section">
              <div
                v-for="(option, optIndex) in item.question.optionList"
                :key="optIndex"
                class="option-item"
                :class="{
                  selected: item.userAnswer?.includes(option.label),
                  correct: item.question.answer?.includes(option.label),
                  wrong: item.userAnswer?.includes(option.label) && !item.question.answer?.includes(option.label)
                }"
              >
                <span class="option-label">{{ option.label }}</span>
                <span class="option-content">{{ option.content }}</span>
              </div>
            </div>
            
            <div v-else class="answer-section">
              <div class="answer-item">
                <span class="answer-label">您的答案：</span>
                <span :class="['answer-value', item.isCorrect === 0 ? 'wrong' : '']">{{ item.userAnswer || '未作答' }}</span>
              </div>
              <div class="answer-item">
                <span class="answer-label">正确答案：</span>
                <span class="answer-value correct">{{ item.question?.answer || '无' }}</span>
              </div>
            </div>
            
            <div v-if="item.question?.analysis" class="analysis-section">
              <div class="analysis-title">答案解析：</div>
              <div class="analysis-content">{{ item.question.analysis }}</div>
            </div>
            
            <div class="score-info" v-if="item.isCorrect !== null">
              得分：<span :class="item.isCorrect === 1 ? 'correct' : 'wrong'">{{ item.score }}</span> 分
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getExamDetail, getExamAnswers, getUserPaperById } from '@/api/user'

const route = useRoute()
const router = useRouter()
const recordId = route.params.recordId

const loading = ref(true)
const recordData = ref({})
const paperData = ref({})
const questionAnswers = ref([])

const totalCount = computed(() => questionAnswers.value.length)
const correctCount = computed(() => {
  return questionAnswers.value.filter(item => item.isCorrect === 1).length
})

const formatDuration = (minutes) => {
  if (!minutes) return '0分钟'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h > 0) {
    return `${h}小时${m}分钟`
  }
  return `${m}分钟`
}

const loadData = async () => {
  loading.value = true
  try {
    const [recordRes, answersRes] = await Promise.all([
      getExamDetail(recordId),
      getExamAnswers(recordId)
    ])
    
    recordData.value = recordRes.data
    questionAnswers.value = answersRes.data || []
    
    if (recordData.value.paperId) {
      const paperRes = await getUserPaperById(recordData.value.paperId)
      paperData.value = paperRes.data
    }
  } catch (e) {
    console.error('加载考试结果失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.loading-box {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
}

.result-card {
  text-align: center;
  padding: 40px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 12px;
}

.result-status {
  font-size: 28px;
  margin-bottom: 20px;
  font-weight: 600;
}

.result-status.passed {
  color: #67c23a;
}

.result-status.failed {
  color: #f56c6c;
}

.result-score {
  font-size: 72px;
  font-weight: bold;
  margin: 30px 0;
}

.result-score.passed {
  color: #67c23a;
}

.result-score.failed {
  color: #f56c6c;
}

.result-info {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin-top: 40px;
  flex-wrap: wrap;
}

.result-info-item {
  text-align: center;
}

.result-info-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.result-info-label {
  color: #909399;
  margin-top: 8px;
  font-size: 14px;
}

.question-list {
  margin-top: 10px;
}

.question-card {
  margin-bottom: 25px;
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
  margin-right: auto;
}

.result-badge {
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.result-badge.correct {
  background: #f0f9eb;
  color: #67c23a;
}

.result-badge.wrong {
  background: #fef0f0;
  color: #f56c6c;
}

.result-badge.pending {
  background: #fdf6ec;
  color: #e6a23c;
}

.question-content {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 15px;
}

.options-section {
  margin-top: 15px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.2s;
}

.option-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.option-item.correct {
  border-color: #67c23a;
  background: #f0f9eb;
}

.option-item.wrong {
  border-color: #f56c6c;
  background: #fef0f0;
}

.option-label {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 12px;
  font-weight: bold;
  color: #606266;
}

.option-item.selected .option-label {
  background: #409eff;
  color: white;
}

.option-item.correct .option-label {
  background: #67c23a;
  color: white;
}

.option-item.wrong .option-label {
  background: #f56c6c;
  color: white;
}

.option-content {
  flex: 1;
  line-height: 1.6;
}

.answer-section {
  margin-top: 15px;
}

.answer-item {
  display: flex;
  align-items: baseline;
  margin-bottom: 10px;
}

.answer-label {
  color: #606266;
  margin-right: 10px;
  min-width: 70px;
}

.answer-value {
  font-size: 15px;
}

.answer-value.correct {
  color: #67c23a;
}

.answer-value.wrong {
  color: #f56c6c;
  text-decoration: line-through;
}

.analysis-section {
  margin-top: 15px;
  padding: 12px 15px;
  background: #fdf6ec;
  border-radius: 4px;
}

.analysis-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #e6a23c;
}

.analysis-content {
  color: #606266;
  line-height: 1.6;
}

.score-info {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 14px;
}

.score-info .correct {
  color: #67c23a;
  font-weight: bold;
}

.score-info .wrong {
  color: #f56c6c;
  font-weight: bold;
}
</style>
