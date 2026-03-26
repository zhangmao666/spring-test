import request from './request'

export function getHotNews(params) {
  return request({
    url: '/news/hot',
    method: 'get',
    params
  })
}
