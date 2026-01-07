import request from './request'

export function getTaskList(params) {
  return request({
    url: '/tasks',
    method: 'get',
    params
  })
}

export function getMyCreatedTasks(params) {
  return request({
    url: '/tasks/created',
    method: 'get',
    params
  })
}

export function getMyPendingTasks(params) {
  return request({
    url: '/tasks/pending',
    method: 'get',
    params
  })
}

export function getTaskById(id) {
  return request({
    url: `/tasks/${id}`,
    method: 'get'
  })
}

export function createTask(data) {
  return request({
    url: '/tasks',
    method: 'post',
    data
  })
}

export function updateTask(id, data) {
  return request({
    url: `/tasks/${id}`,
    method: 'put',
    data
  })
}

export function deleteTask(id) {
  return request({
    url: `/tasks/${id}`,
    method: 'delete'
  })
}

export function submitTask(id) {
  return request({
    url: `/tasks/${id}/submit`,
    method: 'post'
  })
}

export function withdrawTask(id, reason) {
  return request({
    url: `/tasks/${id}/withdraw`,
    method: 'post',
    data: { reason }
  })
}

export function completeTask(id) {
  return request({
    url: `/tasks/${id}/complete`,
    method: 'post'
  })
}
