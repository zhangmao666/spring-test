import request from './request'

export function getDictList(params) {
  return request({
    url: '/dicts',
    method: 'get',
    params
  })
}

export function getDictById(id) {
  return request({
    url: `/dicts/${id}`,
    method: 'get'
  })
}

export function createDict(data) {
  return request({
    url: '/dicts',
    method: 'post',
    data
  })
}

export function updateDict(id, data) {
  return request({
    url: `/dicts/${id}`,
    method: 'put',
    data
  })
}

export function deleteDict(id) {
  return request({
    url: `/dicts/${id}`,
    method: 'delete'
  })
}

export function getDictItems(dictId) {
  return request({
    url: `/dicts/${dictId}/items`,
    method: 'get'
  })
}

export function createDictItem(data) {
  return request({
    url: '/dicts/items',
    method: 'post',
    data
  })
}

export function updateDictItem(id, data) {
  return request({
    url: `/dicts/items/${id}`,
    method: 'put',
    data
  })
}

export function deleteDictItem(id) {
  return request({
    url: `/dicts/items/${id}`,
    method: 'delete'
  })
}
