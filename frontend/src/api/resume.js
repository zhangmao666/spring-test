import request from './request'

/**
 * AI 简历优化
 * @param {Object} data - ResumeOptimizeRequest
 */
export function optimizeResume(data) {
  return request({
    url: '/ai/resume/optimize',
    method: 'post',
    timeout: 190000,
    data
  })
}

/**
 * AI 简历生成
 * @param {Object} data - ResumeGenerateRequest
 */
export function generateResume(data) {
  return request({
    url: '/ai/resume/generate',
    method: 'post',
    timeout: 190000,
    data
  })
}
