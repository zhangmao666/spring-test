import request from './request'

export function getWeatherByCity(cityName) {
  return request({
    url: `/weather/city/${encodeURIComponent(cityName)}`,
    method: 'get'
  })
}

export function getWeather(city, countryCode = 'CN') {
  return request({
    url: '/weather',
    method: 'get',
    params: { city, countryCode }
  })
}

export function getSupportedCities() {
  return request({
    url: '/weather/cities',
    method: 'get'
  })
}
