/**
 * 设备检测工具
 * 用于判断是否为移动设备
 */

/**
 * 检测是否为移动设备
 * @returns {boolean}
 */
export function isMobileDevice() {
  // 通过 User Agent 检测
  const ua = navigator.userAgent || navigator.vendor || window.opera
  const mobileRegex = /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i
  const isMobileUA = mobileRegex.test(ua.toLowerCase())
  
  // 通过屏幕宽度检测（移动端通常小于768px）
  const isMobileScreen = window.innerWidth < 768
  
  // 通过触摸支持检测
  const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0
  
  // 综合判断：如果满足移动端UA或（小屏幕且支持触摸），则认为是移动设备
  return isMobileUA || (isMobileScreen && isTouchDevice)
}

/**
 * 检测是否为平板设备
 * @returns {boolean}
 */
export function isTabletDevice() {
  const ua = navigator.userAgent || navigator.vendor || window.opera
  const tabletRegex = /ipad|android(?!.*mobile)|tablet/i
  return tabletRegex.test(ua.toLowerCase()) && window.innerWidth >= 768 && window.innerWidth < 1024
}

/**
 * 获取设备类型
 * @returns {'mobile' | 'tablet' | 'desktop'}
 */
export function getDeviceType() {
  if (isMobileDevice() && !isTabletDevice()) {
    return 'mobile'
  } else if (isTabletDevice()) {
    return 'tablet'
  } else {
    return 'desktop'
  }
}
