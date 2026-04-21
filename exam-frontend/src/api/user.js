import request from './index'

export function getSubjectList() {
  return request.get('/user/subjects/list')
}

export function getUserPaperList(params) {
  return request.get('/user/papers/list', { params })
}

export function getUserPaperPage(params) {
  return request.get('/user/papers/page', { params })
}

export function getUserPaperById(id) {
  return request.get(`/user/papers/${id}`)
}

export function getUserPaperWithQuestions(id) {
  return request.get(`/user/papers/${id}/questions`)
}

export function startExam(paperId) {
  return request.post(`/user/exam/start/${paperId}`)
}

export function getExamQuestions(recordId) {
  return request.get(`/user/exam/${recordId}/questions`)
}

export function submitAnswer(recordId, data) {
  return request.post(`/user/exam/${recordId}/answer`, data)
}

export function finishExam(recordId) {
  return request.post(`/user/exam/${recordId}/finish`)
}

export function getExamRecords(params) {
  return request.get('/user/exam/records', { params })
}

export function getExamDetail(recordId) {
  return request.get(`/user/exam/records/${recordId}`)
}

export function getExamAnswers(recordId) {
  return request.get(`/user/exam/records/${recordId}/answers`)
}
