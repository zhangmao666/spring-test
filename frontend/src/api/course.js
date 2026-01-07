import request from './request'

export function getCourseList() {
  return request({
    url: '/courses/all',
    method: 'get'
  })
}

export function getPublishedCourses() {
  return request({
    url: '/courses',
    method: 'get'
  })
}

export function getCourseById(id) {
  return request({
    url: `/courses/${id}`,
    method: 'get'
  })
}

export function createCourse(data) {
  return request({
    url: '/courses',
    method: 'post',
    data
  })
}

export function updateCourse(id, data) {
  return request({
    url: `/courses/${id}`,
    method: 'put',
    data
  })
}

export function deleteCourse(id) {
  return request({
    url: `/courses/${id}`,
    method: 'delete'
  })
}

export function publishCourse(id) {
  return request({
    url: `/courses/${id}/publish`,
    method: 'post'
  })
}

export function unpublishCourse(id) {
  return request({
    url: `/courses/${id}/unpublish`,
    method: 'post'
  })
}

export function searchCourses(keyword) {
  return request({
    url: '/courses/search',
    method: 'get',
    params: { keyword }
  })
}
