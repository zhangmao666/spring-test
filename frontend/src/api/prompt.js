import request from './request'

export function getPromptTemplateList(params) {
  return request({
    url: '/prompt-templates',
    method: 'get',
    params
  })
}

export function getPromptTemplateById(id) {
  return request({
    url: `/prompt-templates/${id}`,
    method: 'get'
  })
}

export function createPromptTemplate(data) {
  return request({
    url: '/prompt-templates',
    method: 'post',
    data
  })
}

export function updatePromptTemplate(id, data) {
  return request({
    url: `/prompt-templates/${id}`,
    method: 'put',
    data
  })
}

export function deletePromptTemplate(id) {
  return request({
    url: `/prompt-templates/${id}`,
    method: 'delete'
  })
}

export function renderPromptTemplate(data) {
  return request({
    url: '/prompt-templates/render',
    method: 'post',
    data
  })
}
