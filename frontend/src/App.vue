<template>
  <!--
    Element Plus 改为按需引入后，不再有 app.use(ElementPlus, { locale })。
    这里用 ElConfigProvider 提供中文语言包：它会同时写入 Element Plus 的模块级
    globalConfig，因此 ElMessage / ElMessageBox 这类命令式组件也能取到 locale。
  -->
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<style>
html, body, #app {
  height: 100%;
}
</style>
