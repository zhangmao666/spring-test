<template>
  <div ref="hostRef" class="sparkline"></div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  data: { type: Array, default: () => [] },
  color: { type: String, default: 'var(--accent)' },
  smooth: { type: Boolean, default: true },
  showArea: { type: Boolean, default: true }
})

const hostRef = ref(null)
let chart = null
let resizeObs = null

const resolveColor = (raw) => {
  if (!raw) return '#2f5bea'
  if (!raw.startsWith('var(')) return raw
  const varName = raw.slice(4, -1).trim()
  const val = getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
  return val || '#2f5bea'
}

const render = () => {
  if (!chart) return
  const seriesColor = resolveColor(props.color)
  chart.setOption({
    grid: { left: 0, right: 0, top: 2, bottom: 2 },
    xAxis: { type: 'category', show: false, boundaryGap: false, data: props.data.map((_, i) => i) },
    yAxis: { type: 'value', show: false, scale: true },
    tooltip: { show: false },
    series: [
      {
        type: 'line',
        smooth: props.smooth,
        showSymbol: false,
        lineStyle: { color: seriesColor, width: 2 },
        areaStyle: props.showArea
          ? {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: seriesColor },
                { offset: 1, color: 'rgba(255,255,255,0)' }
              ]),
              opacity: 0.35
            }
          : undefined,
        data: props.data
      }
    ]
  })
}

onMounted(async () => {
  await nextTick()
  if (!hostRef.value) return
  chart = echarts.init(hostRef.value)
  render()
  resizeObs = new ResizeObserver(() => chart && chart.resize())
  resizeObs.observe(hostRef.value)
})

watch(() => props.data, render, { deep: true })
watch(() => props.color, render)

onBeforeUnmount(() => {
  if (resizeObs) resizeObs.disconnect()
  if (chart) chart.dispose()
})
</script>

<style lang="scss" scoped>
.sparkline {
  width: 100%;
  height: 100%;
  min-height: 28px;
}
</style>
