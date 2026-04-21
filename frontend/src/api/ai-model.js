import request from './request'

export function getAiModelList(params) {
  return request({
    url: '/ai/models',
    method: 'get',
    params
  })
}

export function getAiCapabilities() {
  return request({
    url: '/ai/capabilities',
    method: 'get'
  })
}

export function testAiModel(data) {
  return request({
    url: '/ai/models/test',
    method: 'post',
    timeout: 30000,
    data
  })
}

export function createAiModel(data) {
  return request({
    url: '/ai/models',
    method: 'post',
    data
  })
}

export function updateAiModel(id, data) {
  return request({
    url: `/ai/models/${id}`,
    method: 'put',
    data
  })
}

export function setDefaultAiModel(id) {
  return request({
    url: `/ai/models/${id}/default`,
    method: 'put'
  })
}

export function updateAiModelStatus(id, enabled) {
  return request({
    url: `/ai/models/${id}/status`,
    method: 'put',
    params: { enabled }
  })
}

export function deleteAiModel(id) {
  return request({
    url: `/ai/models/${id}`,
    method: 'delete'
  })
}
