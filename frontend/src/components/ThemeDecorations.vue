<template>
  <div class="theme-decorations" :class="`theme-${currentTheme}`">
    <!-- 青花瓷主题 - 丰富的藏青色纹样 -->
    <template v-if="currentTheme === 'porcelain'">
      <div class="porcelain-border top"></div>
      <div class="porcelain-border bottom"></div>
      <div class="porcelain-border left"></div>
      <div class="porcelain-border right"></div>
      <div class="porcelain-corner top-left"></div>
      <div class="porcelain-corner top-right"></div>
      <div class="porcelain-corner bottom-left"></div>
      <div class="porcelain-corner bottom-right"></div>
      <div class="porcelain-pattern"></div>
      <div class="porcelain-flower flower-1"></div>
      <div class="porcelain-flower flower-2"></div>
      <div class="porcelain-flower flower-3"></div>
      <div class="porcelain-cloud cloud-1"></div>
      <div class="porcelain-cloud cloud-2"></div>
    </template>
    
    <!-- 动漫主题 - 樱花飘落 -->
    <template v-if="currentTheme === 'anime'">
      <div class="sakura-container">
        <div v-for="n in 15" :key="n" class="sakura" :style="getSakuraStyle(n)">🌸</div>
      </div>
      <div class="sparkle-container">
        <div v-for="n in 8" :key="n" class="sparkle" :style="getSparkleStyle(n)">✨</div>
      </div>
    </template>
    
    <!-- 赛博朋克主题 - 霓虹效果 -->
    <template v-if="currentTheme === 'cyberpunk'">
      <div class="cyber-scanline"></div>
      <div class="cyber-glow top"></div>
      <div class="cyber-glow bottom"></div>
      <div class="cyber-corner top-left"></div>
      <div class="cyber-corner top-right"></div>
      <div class="cyber-corner bottom-left"></div>
      <div class="cyber-corner bottom-right"></div>
      <div class="cyber-grid"></div>
      <div class="cyber-flicker"></div>
    </template>
    
    <!-- 神仙姐姐主题 - 仙气飘飘 -->
    <template v-if="currentTheme === 'liuyifei'">
      <div class="petal-container">
        <div v-for="n in 12" :key="n" class="petal" :style="getPetalStyle(n)"></div>
      </div>
      <div class="ethereal-glow"></div>
      <div class="fairy-dust">
        <div v-for="n in 20" :key="n" class="dust" :style="getDustStyle(n)"></div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
const currentTheme = computed(() => themeStore.currentTheme)

const getSakuraStyle = (n) => {
  const left = Math.random() * 100
  const delay = Math.random() * 8
  const duration = 8 + Math.random() * 6
  const size = 12 + Math.random() * 12
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    fontSize: `${size}px`,
    opacity: 0.6 + Math.random() * 0.4
  }
}

const getSparkleStyle = (n) => {
  const left = 10 + Math.random() * 80
  const top = 10 + Math.random() * 80
  const delay = Math.random() * 4
  const size = 10 + Math.random() * 8
  return {
    left: `${left}%`,
    top: `${top}%`,
    animationDelay: `${delay}s`,
    fontSize: `${size}px`
  }
}

const getPetalStyle = (n) => {
  const left = Math.random() * 100
  const delay = Math.random() * 10
  const duration = 10 + Math.random() * 8
  const size = 15 + Math.random() * 15
  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    width: `${size}px`,
    height: `${size * 0.8}px`,
    opacity: 0.4 + Math.random() * 0.4
  }
}

const getDustStyle = (n) => {
  const left = Math.random() * 100
  const top = Math.random() * 100
  const delay = Math.random() * 5
  const size = 2 + Math.random() * 4
  return {
    left: `${left}%`,
    top: `${top}%`,
    animationDelay: `${delay}s`,
    width: `${size}px`,
    height: `${size}px`
  }
}
</script>

<style lang="scss" scoped>
.theme-decorations {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

// 青花瓷主题装饰 - 藏青色
.theme-porcelain {
  // 边框装饰
  .porcelain-border {
    position: absolute;
    background: linear-gradient(90deg, transparent, rgba(26, 54, 93, 0.3), transparent);
    
    &.top {
      top: 64px;
      left: 240px;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, 
        transparent 0%, 
        rgba(26, 54, 93, 0.5) 20%, 
        rgba(26, 54, 93, 0.8) 50%, 
        rgba(26, 54, 93, 0.5) 80%, 
        transparent 100%);
    }
    &.bottom {
      bottom: 0;
      left: 240px;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, 
        transparent 0%, 
        rgba(26, 54, 93, 0.5) 20%, 
        rgba(26, 54, 93, 0.8) 50%, 
        rgba(26, 54, 93, 0.5) 80%, 
        transparent 100%);
    }
    &.left {
      top: 64px;
      left: 240px;
      bottom: 0;
      width: 2px;
      background: linear-gradient(180deg, 
        rgba(26, 54, 93, 0.6) 0%, 
        rgba(26, 54, 93, 0.3) 50%, 
        rgba(26, 54, 93, 0.6) 100%);
    }
    &.right {
      top: 64px;
      right: 0;
      bottom: 0;
      width: 2px;
      background: linear-gradient(180deg, 
        rgba(26, 54, 93, 0.6) 0%, 
        rgba(26, 54, 93, 0.3) 50%, 
        rgba(26, 54, 93, 0.6) 100%);
    }
  }
  
  // 角落装饰 - 回纹
  .porcelain-corner {
    position: absolute;
    width: 80px;
    height: 80px;
    opacity: 0.4;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Cpath d='M10,10 L10,40 L40,40 L40,25 L25,25 L25,10 Z' fill='none' stroke='%231a365d' stroke-width='3'/%3E%3Cpath d='M50,10 L50,30 L70,30 L70,10' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Cpath d='M10,50 L30,50 L30,70 L10,70' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Ccircle cx='60' cy='60' r='15' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Ccircle cx='60' cy='60' r='8' fill='none' stroke='%231a365d' stroke-width='1.5'/%3E%3C/svg%3E");
    background-size: contain;
    
    &.top-left { top: 70px; left: 250px; }
    &.top-right { top: 70px; right: 10px; transform: scaleX(-1); }
    &.bottom-left { bottom: 10px; left: 250px; transform: scaleY(-1); }
    &.bottom-right { bottom: 10px; right: 10px; transform: scale(-1); }
  }
  
  // 青花纹样背景
  .porcelain-pattern {
    position: absolute;
    top: 64px;
    left: 240px;
    right: 0;
    bottom: 0;
    opacity: 0.04;
    background-image: 
      url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='80' height='80'%3E%3Ccircle cx='40' cy='40' r='20' fill='none' stroke='%231a365d' stroke-width='1'/%3E%3Ccircle cx='40' cy='40' r='12' fill='none' stroke='%231a365d' stroke-width='0.8'/%3E%3Cpath d='M20,40 Q30,30 40,40 T60,40' fill='none' stroke='%231a365d' stroke-width='0.8'/%3E%3Cpath d='M40,20 Q30,30 40,40 T40,60' fill='none' stroke='%231a365d' stroke-width='0.8'/%3E%3C/svg%3E");
  }
  
  // 青花花朵装饰
  .porcelain-flower {
    position: absolute;
    width: 120px;
    height: 120px;
    opacity: 0.12;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='50' r='8' fill='%231a365d'/%3E%3Cellipse cx='50' cy='25' rx='12' ry='20' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Cellipse cx='50' cy='75' rx='12' ry='20' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Cellipse cx='25' cy='50' rx='20' ry='12' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Cellipse cx='75' cy='50' rx='20' ry='12' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Cellipse cx='30' cy='30' rx='10' ry='15' transform='rotate(-45 30 30)' fill='none' stroke='%231a365d' stroke-width='1.5'/%3E%3Cellipse cx='70' cy='30' rx='10' ry='15' transform='rotate(45 70 30)' fill='none' stroke='%231a365d' stroke-width='1.5'/%3E%3Cellipse cx='30' cy='70' rx='10' ry='15' transform='rotate(45 30 70)' fill='none' stroke='%231a365d' stroke-width='1.5'/%3E%3Cellipse cx='70' cy='70' rx='10' ry='15' transform='rotate(-45 70 70)' fill='none' stroke='%231a365d' stroke-width='1.5'/%3E%3C/svg%3E");
    background-size: contain;
    
    &.flower-1 { top: 120px; right: 80px; }
    &.flower-2 { bottom: 100px; left: 320px; width: 100px; height: 100px; }
    &.flower-3 { top: 50%; right: 40%; width: 80px; height: 80px; opacity: 0.06; }
  }
  
  // 祥云装饰
  .porcelain-cloud {
    position: absolute;
    width: 150px;
    height: 60px;
    opacity: 0.1;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 150 60'%3E%3Cpath d='M20,50 Q10,50 10,40 Q10,30 25,30 Q20,20 35,15 Q50,10 65,20 Q75,10 90,15 Q105,10 115,20 Q130,15 140,30 Q150,30 150,40 Q150,50 140,50 Z' fill='none' stroke='%231a365d' stroke-width='2'/%3E%3Cpath d='M30,45 Q25,40 35,35 Q45,30 55,38' fill='none' stroke='%231a365d' stroke-width='1'/%3E%3Cpath d='M80,42 Q75,38 85,33 Q95,28 105,36' fill='none' stroke='%231a365d' stroke-width='1'/%3E%3C/svg%3E");
    background-size: contain;
    background-repeat: no-repeat;
    
    &.cloud-1 { top: 100px; left: 45%; }
    &.cloud-2 { bottom: 80px; right: 15%; transform: scaleX(-1); }
  }
}

// 动漫主题装饰
.theme-anime {
  .sakura-container {
    position: absolute;
    top: 0;
    left: 240px;
    right: 0;
    height: 100%;
  }
  
  .sakura {
    position: absolute;
    top: -30px;
    animation: sakura-fall linear infinite;
    filter: drop-shadow(0 0 3px rgba(236, 72, 153, 0.5));
  }
  
  @keyframes sakura-fall {
    0% {
      transform: translateY(-10px) rotate(0deg) translateX(0);
      opacity: 0;
    }
    10% {
      opacity: 1;
    }
    90% {
      opacity: 1;
    }
    100% {
      transform: translateY(100vh) rotate(360deg) translateX(50px);
      opacity: 0;
    }
  }
  
  .sparkle-container {
    position: absolute;
    top: 0;
    left: 240px;
    right: 0;
    bottom: 0;
  }
  
  .sparkle {
    position: absolute;
    animation: sparkle-blink 2s ease-in-out infinite;
  }
  
  @keyframes sparkle-blink {
    0%, 100% { opacity: 0.3; transform: scale(0.8); }
    50% { opacity: 1; transform: scale(1.2); }
  }
}

// 赛博朋克主题装饰
.theme-cyberpunk {
  // 扫描线效果
  .cyber-scanline {
    position: absolute;
    top: 64px;
    left: 240px;
    right: 0;
    bottom: 0;
    background: repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(0, 245, 255, 0.03) 2px,
      rgba(0, 245, 255, 0.03) 4px
    );
    animation: scanline-move 8s linear infinite;
  }
  
  @keyframes scanline-move {
    0% { background-position: 0 0; }
    100% { background-position: 0 100px; }
  }
  
  // 顶部/底部霓虹光晕
  .cyber-glow {
    position: absolute;
    left: 240px;
    right: 0;
    height: 100px;
    
    &.top {
      top: 64px;
      background: linear-gradient(180deg, 
        rgba(255, 0, 255, 0.15) 0%, 
        rgba(0, 245, 255, 0.05) 30%,
        transparent 100%);
    }
    &.bottom {
      bottom: 0;
      background: linear-gradient(0deg, 
        rgba(0, 245, 255, 0.15) 0%, 
        rgba(255, 0, 255, 0.05) 30%,
        transparent 100%);
    }
  }
  
  // 霓虹边角
  .cyber-corner {
    position: absolute;
    width: 60px;
    height: 60px;
    
    &.top-left {
      top: 70px;
      left: 250px;
      border-left: 2px solid #00f5ff;
      border-top: 2px solid #00f5ff;
      box-shadow: -5px -5px 20px rgba(0, 245, 255, 0.5);
    }
    &.top-right {
      top: 70px;
      right: 10px;
      border-right: 2px solid #ff00ff;
      border-top: 2px solid #ff00ff;
      box-shadow: 5px -5px 20px rgba(255, 0, 255, 0.5);
    }
    &.bottom-left {
      bottom: 10px;
      left: 250px;
      border-left: 2px solid #ff00ff;
      border-bottom: 2px solid #ff00ff;
      box-shadow: -5px 5px 20px rgba(255, 0, 255, 0.5);
    }
    &.bottom-right {
      bottom: 10px;
      right: 10px;
      border-right: 2px solid #00f5ff;
      border-bottom: 2px solid #00f5ff;
      box-shadow: 5px 5px 20px rgba(0, 245, 255, 0.5);
    }
  }
  
  // 网格背景
  .cyber-grid {
    position: absolute;
    top: 64px;
    left: 240px;
    right: 0;
    bottom: 0;
    opacity: 0.15;
    background-image: 
      linear-gradient(rgba(0, 245, 255, 0.1) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 0, 255, 0.1) 1px, transparent 1px);
    background-size: 60px 60px;
    perspective: 500px;
    transform: rotateX(60deg);
    transform-origin: center top;
  }
  
  // 闪烁效果
  .cyber-flicker {
    position: absolute;
    top: 64px;
    left: 240px;
    right: 0;
    bottom: 0;
    background: transparent;
    animation: cyber-flicker 0.15s infinite;
    pointer-events: none;
  }
  
  @keyframes cyber-flicker {
    0%, 100% { opacity: 0; }
    50% { opacity: 0.02; background: rgba(0, 245, 255, 0.1); }
  }
}

// 神仙姐姐主题装饰
.theme-liuyifei {
  // 飘落的花瓣
  .petal-container {
    position: absolute;
    top: 0;
    left: 240px;
    right: 0;
    height: 100%;
  }

  .petal {
    position: absolute;
    top: -40px;
    background: linear-gradient(135deg, #fff 0%, #f3e6ff 100%);
    border-radius: 50% 0 50% 50%;
    animation: petal-fall linear infinite;
    box-shadow: 0 2px 5px rgba(159, 122, 234, 0.1);
  }

  @keyframes petal-fall {
    0% {
      transform: translateY(-20px) rotate(0deg) translateX(0) scale(0.5);
      opacity: 0;
    }
    10% { opacity: 0.8; }
    90% { opacity: 0.8; }
    100% {
      transform: translateY(100vh) rotate(720deg) translateX(100px) scale(1.2);
      opacity: 0;
    }
  }

  // 仙气光晕
  .ethereal-glow {
    position: absolute;
    top: 0;
    left: 240px;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 50% 50%, rgba(159, 122, 234, 0.05) 0%, transparent 70%);
    animation: glow-pulse 8s ease-in-out infinite;
  }

  @keyframes glow-pulse {
    0%, 100% { opacity: 0.3; transform: scale(1); }
    50% { opacity: 0.7; transform: scale(1.1); }
  }

  // 仙尘/光点
  .fairy-dust {
    position: absolute;
    top: 0;
    left: 240px;
    right: 0;
    bottom: 0;
  }

  .dust {
    position: absolute;
    background: #fff;
    border-radius: 50%;
    box-shadow: 0 0 10px #fff, 0 0 20px #b794f4;
    animation: dust-blink 3s ease-in-out infinite;
  }

  @keyframes dust-blink {
    0%, 100% { opacity: 0.2; transform: scale(0.5); }
    50% { opacity: 1; transform: scale(1.5); }
  }
}
</style>
