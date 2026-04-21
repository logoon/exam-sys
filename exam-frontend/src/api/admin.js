import request from './index'

export function getSubjectPage(params) {
  return request.get('/admin/subjects/page', { params })
}

export function getSubjectList() {
  return request.get('/admin/subjects/list')
}

export function getSubjectById(id) {
  return request.get(`/admin/subjects/${id}`)
}

export function createSubject(data) {
  return request.post('/admin/subjects', data)
}

export function updateSubject(id, data) {
  return request.put(`/admin/subjects/${id}`, data)
}

export function deleteSubject(id) {
  return request.delete(`/admin/subjects/${id}`)
}

export function updateSubjectStatus(id, status) {
  return request.put(`/admin/subjects/${id}/status?status=${status}`)
}

export function getQuestionTypes() {
  return request.get('/admin/questions/types')
}

export function getQuestionPage(params) {
  return request.get('/admin/questions/page', { params })
}

export function getQuestionById(id) {
  return request.get(`/admin/questions/${id}`)
}

export function createQuestion(data) {
  return request.post('/admin/questions', data)
}

export function updateQuestion(id, data) {
  return request.put(`/admin/questions/${id}`, data)
}

export function deleteQuestion(id) {
  return request.delete(`/admin/questions/${id}`)
}

export function updateQuestionStatus(id, status) {
  return request.put(`/admin/questions/${id}/status?status=${status}`)
}

export function getPaperPage(params) {
  return request.get('/admin/papers/page', { params })
}

export function getPaperById(id, includeQuestions = true) {
  return request.get(`/admin/papers/${id}`, { params: { includeQuestions } })
}

export function createPaper(data) {
  return request.post('/admin/papers', data)
}

export function updatePaper(id, data) {
  return request.put(`/admin/papers/${id}`, data)
}

export function deletePaper(id) {
  return request.delete(`/admin/papers/${id}`)
}

export function updatePaperStatus(id, status) {
  return request.put(`/admin/papers/${id}/status?status=${status}`)
}

export function addQuestionsToPaper(paperId, questions) {
  return request.post(`/admin/papers/${paperId}/questions`, questions)
}

export function removeQuestionFromPaper(paperId, questionId) {
  return request.delete(`/admin/papers/${paperId}/questions/${questionId}`)
}
