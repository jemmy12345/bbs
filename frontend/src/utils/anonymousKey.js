/**
 * Anonymous posting: local key management and hashing.
 * Key is stored in localStorage; only the hash is sent to the server.
 */

const STORAGE_KEY = 'anonymous_key'
const KEY_EVER_SET_FLAG = 'anonymous_key_ever_set'

const ALPHANUMERIC = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
const KEY_LENGTH = 6

// Only kept in memory for current page/session.
// If user doesn't want to "remember in this browser", we store key here instead of localStorage.
let sessionAnonymousKey = null

/**
 * Get the stored anonymous key, or null if not set.
 * @returns {string|null}
 */
export function getAnonymousKeyFromLocalStorage() {
  try {
    const key = localStorage.getItem(STORAGE_KEY)
    return key && key.length > 0 ? key : null
  } catch (e) {
    return null
  }
}

/**
 * Get anonymous key from localStorage, fallback to current session.
 * @returns {string|null}
 */
export function getAnonymousKey() {
  const localKey = getAnonymousKeyFromLocalStorage()
  return localKey || sessionAnonymousKey
}

/**
 * Check if an anonymous key exists in storage.
 * @returns {boolean}
 */
export function hasAnonymousKey() {
  return !!getAnonymousKey()
}

/**
 * Check if an anonymous key exists in localStorage only.
 * (No session fallback)
 * @returns {boolean}
 */
export function hasAnonymousKeyInLocalStorage() {
  return !!getAnonymousKeyFromLocalStorage()
}

/**
 * Save the anonymous key to localStorage and set the "ever set" flag.
 * @param {string} key - Raw key (6-char alphanumeric recommended).
 */
/**
 * 设置匿名密钥的函数
 * @param {string} key - 要设置的匿名密钥
 * @param {object} options
 * @param {boolean} options.rememberInBrowser - Save key into localStorage (and set ever-set flag).
 *   When false, key is stored only in memory for current session.
 */
export function setAnonymousKey(key, options = {}) {
  // 检查输入是否为非空字符串
  if (typeof key !== 'string' || key.length === 0) return
  const rememberInBrowser = options.rememberInBrowser !== false
  const normalizedKey = key.trim()

  // Always keep a session copy so hashing can proceed even when user doesn't want browser persistence.
  sessionAnonymousKey = normalizedKey

  try {
    if (rememberInBrowser) {
      // 将密钥去除前后空格后存入localStorage
      localStorage.setItem(STORAGE_KEY, normalizedKey)
      // 设置一个标志位，表示密钥曾经被设置过（用于提示“曾经设置过但现在没找到”）
      localStorage.setItem(KEY_EVER_SET_FLAG, '1')
    } else {
      // 删除浏览器侧秘钥相关记录
      localStorage.removeItem(STORAGE_KEY)
      localStorage.removeItem(KEY_EVER_SET_FLAG)
    }
  } catch (e) {
    // 如果存储过程中出现错误，打印警告信息
    console.warn('anonymousKey: setAnonymousKey failed', e)
  }
}

/**
 * Check if user has ever set a key (used to show "key not found" hint when key is missing).
 * @returns {boolean}
 */
export function hasEverSetAnonymousKey() {
  try {
    return localStorage.getItem(KEY_EVER_SET_FLAG) === '1'
  } catch (e) {
    return false
  }
}

/**
 * Generate a 6-character alphanumeric key (e.g. A1b2C3).
 * @returns {string}
 */
export function generateRandomKey() {
  let result = ''
  for (let i = 0; i < KEY_LENGTH; i++) {
    result += ALPHANUMERIC.charAt(Math.floor(Math.random() * ALPHANUMERIC.length))
  }
  return result
}


/**
 * Hash (anonymous key + logged-in user id) with SHA-256.
 * Used for backend to distinguish same manual key across different accounts.
 * @param {string} key - Raw anonymous key.
 * @param {string|number} userId - Current user's userId.
 * @returns {Promise<string>} Hex-encoded SHA-256 hash (sent as request field `userid`).
 */
export function hashAnonymousKeyForUser(key, userId) {
  if (typeof key !== 'string' || key.length === 0) {
    return Promise.reject(new Error('anonymousKey: key is required'))
  }
  const uid =
    userId !== undefined && userId !== null && String(userId).trim() !== ''
      ? String(userId).trim()
      : ''
  if (!uid) {
    return Promise.reject(new Error('anonymousKey: userId is required'))
  }
  const material = key.trim() + uid
  const encoder = new TextEncoder()
  const data = encoder.encode(material)
  return crypto.subtle.digest('SHA-256', data).then((buffer) => {
    const bytes = new Uint8Array(buffer)
    return Array.from(bytes)
      .map((b) => b.toString(16).padStart(2, '0'))
      .join('')
  })
}

/**
 * 校验秘钥格式：6个字符，只能包含字母和数字
 * @param {string} key
 * @returns {boolean}
 */
export function isValidKeyFormat(key) {
  if (typeof key !== 'string') return false
  const trimmed = key.trim()
  if (trimmed.length !== KEY_LENGTH) return false
  return /^[A-Za-z0-9]+$/.test(trimmed)
}
