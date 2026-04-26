/**
 * 聚合实名 + 匿名未读数（用于顶栏红点）
 */
import { getUnreadCount } from '@/api/bbs/notification'
import { getAnonymousKey, hashAnonymousKeyForUser } from '@/utils/anonymousKey'

function safeUnread(response) {
  if (response && response.code === 200) {
    const n = response.data
    return typeof n === 'number' ? n : parseInt(n, 10) || 0
  }
  return 0
}

/**
 * @param {string|number} userId - 当前登录用户 id（vuex user.id）
 * @returns {Promise<number>}
 */
export function fetchLayoutUnreadTotal(userId) {
  const realPromise = getUnreadCount()
    .then(safeUnread)
    .catch(() => 0)

  const key = getAnonymousKey()
  if (!key || userId === undefined || userId === null || String(userId).trim() === '') {
    return realPromise
  }

  const anonPromise = hashAnonymousKeyForUser(key, userId)
    .then((hash) => getUnreadCount({ code: hash }))
    .then(safeUnread)
    .catch(() => 0)

  return Promise.all([realPromise, anonPromise]).then(([a, b]) => a + b)
}
