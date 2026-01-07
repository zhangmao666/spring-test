import request from './request'

// ==================== 登录日志 ====================

/**
 * 获取登录日志列表
 */
export function getLoginLogList(params) {
  return request({
    url: '/log/login/list',
    method: 'get',
    params
  })
}

/**
 * 删除登录日志
 */
export function deleteLoginLog(id) {
  return request({
    url: `/log/login/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除登录日志
 */
export function batchDeleteLoginLog(ids) {
  return request({
    url: '/log/login/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 清空登录日志
 */
export function clearLoginLog() {
  return request({
    url: '/log/login/clear',
    method: 'delete'
  })
}

// ==================== 操作日志 ====================

/**
 * 获取操作日志列表
 */
export function getOperationLogList(params) {
  return request({
    url: '/log/operation/list',
    method: 'get',
    params
  })
}

/**
 * 获取操作日志详情
 */
export function getOperationLogDetail(id) {
  return request({
    url: `/log/operation/${id}`,
    method: 'get'
  })
}

/**
 * 删除操作日志
 */
export function deleteOperationLog(id) {
  return request({
    url: `/log/operation/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除操作日志
 */
export function batchDeleteOperationLog(ids) {
  return request({
    url: '/log/operation/batch',
    method: 'delete',
    data: ids
  })
}

/**
 * 清空操作日志
 */
export function clearOperationLog() {
  return request({
    url: '/log/operation/clear',
    method: 'delete'
  })
}
