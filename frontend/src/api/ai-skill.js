import request from './request'

export function listSkills(params) {
  return request({
    url: '/ai/skills',
    method: 'get',
    params
  })
}

export function getSkill(id) {
  return request({
    url: `/ai/skills/${id}`,
    method: 'get'
  })
}

export function createSkill(data) {
  return request({
    url: '/ai/skills',
    method: 'post',
    data
  })
}

export function updateSkill(id, data) {
  return request({
    url: `/ai/skills/${id}`,
    method: 'put',
    data
  })
}

export function updateSkillStatus(id, enabled) {
  return request({
    url: `/ai/skills/${id}/status`,
    method: 'put',
    params: { enabled }
  })
}

export function deleteSkill(id) {
  return request({
    url: `/ai/skills/${id}`,
    method: 'delete'
  })
}

export function syncLocalSkills() {
  return request({
    url: '/ai/skills/sync-local',
    method: 'post'
  })
}

export function importSkillZip(file) {
  const data = new FormData()
  data.append('file', file)
  return request({
    url: '/ai/skills/import-zip',
    method: 'post',
    timeout: 60000,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function getConversationSkills(conversationId) {
  return request({
    url: `/ai/history/conversations/${conversationId}/skills`,
    method: 'get'
  })
}

export function saveConversationSkills(conversationId, skillIds) {
  return request({
    url: `/ai/history/conversations/${conversationId}/skills`,
    method: 'put',
    data: { skillIds }
  })
}

export function testSkill(id, data) {
  return request({
    url: `/ai/skills/${id}/test`,
    method: 'post',
    timeout: 35000,
    data
  })
}
