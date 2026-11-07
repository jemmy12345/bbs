// 自动合并 modules 目录下各页面的中英文文案。
// 每个模块文件导出 { zh: {...}, en: {...} }，顶层 key 作为命名空间（如 qa、idea），不同模块的命名空间不要重复。
const ctx = require.context('./', false, /\.js$/)

const zh = {}
const en = {}
ctx.keys().forEach(key => {
  if (key === './index.js') return
  const mod = ctx(key).default || ctx(key)
  Object.assign(zh, mod.zh || {})
  Object.assign(en, mod.en || {})
})

export const zhModules = zh
export const enModules = en
