// Mock数据 - 论坛系统
// 在开发环境下使用，生产环境请移除

// 模拟延迟
const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms))

// Mock分类数据
const mockCategories = [
  {
    categoryId: 1,
    categoryName: '通用社区',
    categoryDesc: '技术讨论和经验分享',
    icon: 'el-icon-cpu',
    sortOrder: 1,
    status: '0',
    postCount: 123,
    createTime: '2024-01-01 10:00:00'
  },
  {
    categoryId: 2,
    categoryName: '产品社区',
    categoryDesc: '产品功能改进建议',
    icon: 'el-icon-s-promotion',
    sortOrder: 2,
    status: '0',
    postCount: 95,
    createTime: '2024-01-01 10:00:00'
  },
  {
    categoryId: 3,
    categoryName: '网络社区',
    categoryDesc: '工作经验和心得体会',
    icon: 'el-icon-document',
    sortOrder: 3,
    status: '0',
    postCount: 75,
    createTime: '2024-01-01 10:00:00'
  },
  {
    categoryId: 4,
    categoryName: '人力社区',
    categoryDesc: '生活趣事和日常分享',
    icon: 'el-icon-sunny',
    sortOrder: 4,
    status: '0',
    postCount: 54,
    createTime: '2024-01-01 10:00:00'
  },
  {
    categoryId: 5,
    categoryName: '财务社区',
    categoryDesc: '技术问题和求助',
    icon: 'el-icon-question',
    sortOrder: 5,
    status: '0',
    postCount: 42,
    createTime: '2024-01-01 10:00:00'
  }
]

// Mock帖子数据
const mockPosts = [
  {
    postId: 1,
    categoryId: 1,
    userId: 1,
    title: 'Vue3 Composition API 深度解析与实践',
    content: '<p>本文深入探讨Vue3 Composition API的设计理念、使用场景和最佳实践,帮助开发者更好地理解和使用Vue3的新特性...</p>',
    summary: '本文深入探讨Vue3 Composition API的设计理念、使用场景和最佳实践,帮助开发者更好地理解和使用Vue3的新特性...',
    viewCount: 1234,
    likeCount: 89,
    commentCount: 12,
    collectCount: 23,
    isTop: '0',
    isEssence: '1',
    status: '0',
    delFlag: '0',
    lastReplyTime: '2024-01-15 14:30:00',
    lastReplyUser: 2,
    categoryName: '技术分享',
    nickName: '张开发',
    avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
    createTime: '2024-01-15 09:00:00',
    isLiked: false,
    isCollected: false
  },
  {
    postId: 2,
    categoryId: 1,
    userId: 2,
    title: '前端性能优化:从理论到实践',
    content: '<p>性能优化是前端开发中永恒的话题。本文从代码层面、构建层面、网络层面等多个维度,系统性地介绍前端性能优化的方法和技巧...</p>',
    summary: '性能优化是前端开发中永恒的话题。本文从代码层面、构建层面、网络层面等多个维度,系统性地介绍前端性能优化的方法和技巧...',
    viewCount: 2156,
    likeCount: 156,
    commentCount: 28,
    collectCount: 45,
    isTop: '0',
    isEssence: '1',
    status: '0',
    delFlag: '0',
    lastReplyTime: '2024-01-14 16:20:00',
    lastReplyUser: 3,
    categoryName: '技术分享',
    nickName: '李前端',
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    createTime: '2024-01-14 11:00:00',
    isLiked: true,
    isCollected: false
  },
  {
    postId: 3,
    categoryId: 1,
    userId: 3,
    title: '微服务架构设计思考与实践',
    content: '<p>微服务架构已经成为现代应用开发的主流选择。本文分享在微服务架构设计过程中的思考和实践经验,包括服务拆分、通信方式、数据一致性等关键问题...</p>',
    summary: '微服务架构已经成为现代应用开发的主流选择。本文分享在微服务架构设计过程中的思考和实践经验,包括服务拆分、通信方式、数据一致性等关键问题...',
    viewCount: 1890,
    likeCount: 134,
    commentCount: 35,
    collectCount: 67,
    isTop: '0',
    isEssence: '1',
    status: '0',
    delFlag: '0',
    lastReplyTime: '2024-01-13 10:15:00',
    lastReplyUser: 1,
    categoryName: '技术分享',
    nickName: '王架构',
    avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
    createTime: '2024-01-13 14:00:00',
    isLiked: false,
    isCollected: true
  },
  {
    postId: 4,
    categoryId: 3,
    userId: 4,
    title: '团队协作中的沟通技巧',
    content: '<p>良好的沟通是团队协作的基础。本文分享在技术团队中如何有效沟通,包括代码审查、技术讨论、问题反馈等方面的实践经验...</p>',
    summary: '良好的沟通是团队协作的基础。本文分享在技术团队中如何有效沟通,包括代码审查、技术讨论、问题反馈等方面的实践经验...',
    viewCount: 987,
    likeCount: 67,
    commentCount: 15,
    collectCount: 23,
    isTop: '0',
    isEssence: '0',
    status: '0',
    delFlag: '0',
    lastReplyTime: '2024-01-12 18:30:00',
    lastReplyUser: 2,
    categoryName: '职场交流',
    nickName: '赵经理',
    avatar: 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a6jpeg.jpeg',
    createTime: '2024-01-12 16:00:00',
    isLiked: false,
    isCollected: false
  },
  {
    postId: 5,
    categoryId: 2,
    userId: 5,
    title: '产品设计中的用户体验思考',
    content: '<p>用户体验是产品成功的关键因素之一。本文从用户研究、交互设计、视觉设计等多个角度,探讨如何提升产品的用户体验...</p>',
    summary: '用户体验是产品成功的关键因素之一。本文从用户研究、交互设计、视觉设计等多个角度,探讨如何提升产品的用户体验...',
    viewCount: 756,
    likeCount: 45,
    commentCount: 18,
    collectCount: 19,
    isTop: '0',
    isEssence: '0',
    status: '0',
    delFlag: '0',
    lastReplyTime: '2024-01-11 09:00:00',
    lastReplyUser: 1,
    categoryName: '产品讨论',
    nickName: '产品设计师',
    avatar: 'https://cube.elemecdn.com/c/9f/3fc305e9ab6d190e9629e185f952dpng.png',
    createTime: '2024-01-11 10:00:00',
    isLiked: false,
    isCollected: false
  }
]

// Mock评论数据
const mockComments = [
  {
    commentId: 1,
    postId: 1,
    userId: 2,
    parentId: 0,
    replyUserId: null,
    content: '非常好的分享，学到了很多！',
    likeCount: 5,
    status: '0',
    delFlag: '0',
    nickName: '产品经理',
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    replyNickName: null,
    createTime: '2024-01-10 10:30:00',
    isLiked: false,
    children: [
      {
        commentId: 2,
        postId: 1,
        userId: 1,
        parentId: 1,
        replyUserId: 2,
        content: '谢谢支持！',
        likeCount: 2,
        status: '0',
        delFlag: '0',
        nickName: '技术达人',
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        replyNickName: '产品经理',
        createTime: '2024-01-10 10:35:00',
        isLiked: false
      }
    ]
  },
  {
    commentId: 3,
    postId: 1,
    userId: 3,
    parentId: 0,
    replyUserId: null,
    content: '请问关于缓存的使用，有什么具体的建议吗？',
    likeCount: 3,
    status: '0',
    delFlag: '0',
    nickName: '团队Leader',
    avatar: 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png',
    replyNickName: null,
    createTime: '2024-01-10 11:00:00',
    isLiked: true,
    children: []
  }
]

// Mock标签数据
const mockTags = [
  { tagId: 1, tagName: 'Spring Boot', tagColor: 'primary', useCount: 15 },
  { tagId: 2, tagName: 'Vue', tagColor: 'success', useCount: 12 },
  { tagId: 3, tagName: 'Java', tagColor: 'warning', useCount: 20 },
  { tagId: 4, tagName: '前端', tagColor: 'danger', useCount: 18 },
  { tagId: 5, tagName: '后端', tagColor: 'info', useCount: 16 }
]

// Mock敏感词列表（参考小红书、抖音等平台的敏感词标准）
const mockSensitiveWords = [
  '政治敏感', '色情', '暴力', '赌博', '诈骗', '毒品', '传销',
  '反动', '分裂', '恐怖', '极端', '邪教', '迷信', '谣言',
  '广告', '刷单', '代购', '微商', '引流', '推广', '营销',
  '低俗', '恶俗', '辱骂', '人身攻击', '歧视', '仇恨',
  '自杀', '自残', '血腥', '恶心', '变态', '性暗示',
  '联系方式', '微信号', 'QQ号', '手机号', '链接', '二维码',
  '违禁品', '管制刀具', '仿真枪', '假货', '山寨', '盗版'
]

// 导出Mock API函数
export const mockBbsAPI = {
  // 获取分类列表
  getCategoryList: async () => {
    await delay()
    return {
      code: 200,
      msg: '操作成功',
      data: mockCategories
    }
  },

  // 获取帖子列表
  getPostList: async (params) => {
    await delay()
    let posts = [...mockPosts]
    
    // 按分类筛选
    if (params.categoryId) {
      posts = posts.filter(p => p.categoryId === params.categoryId)
    }
    
    // 按标题搜索
    if (params.title) {
      posts = posts.filter(p => p.title.includes(params.title))
    }
    
    // 排序
    if (params.sortType === 'hot') {
      posts.sort((a, b) => (b.likeCount + b.commentCount) - (a.likeCount + a.commentCount))
    } else if (params.sortType === 'essence') {
      posts = posts.filter(p => p.isEssence === '1')
    }
    
    // 分页
    const pageNum = params.pageNum || 1
    const pageSize = params.pageSize || 10
    const start = (pageNum - 1) * pageSize
    const end = start + pageSize
    const rows = posts.slice(start, end)
    
    return {
      code: 200,
      msg: '操作成功',
      rows: rows,
      total: posts.length
    }
  },

  // 获取帖子详情
  getPostDetail: async (postId) => {
    await delay()
    const post = mockPosts.find(p => p.postId === parseInt(postId))
    if (post) {
      return {
        code: 200,
        msg: '操作成功',
        data: { ...post, viewCount: post.viewCount + 1 }
      }
    }
    return {
      code: 500,
      msg: '帖子不存在'
    }
  },

  // 发布帖子
  addPost: async (data) => {
    await delay()
    const newPost = {
      postId: mockPosts.length + 1,
      ...data,
      viewCount: 0,
      likeCount: 0,
      commentCount: 0,
      collectCount: 0,
      isTop: '0',
      isEssence: '0',
      status: '0',
      delFlag: '0',
      createTime: new Date().toLocaleString('zh-CN'),
      categoryName: mockCategories.find(c => c.categoryId === data.categoryId)?.categoryName || '',
      nickName: '当前用户',
      avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      isLiked: false,
      isCollected: false
    }
    mockPosts.unshift(newPost)
    return {
      code: 200,
      msg: '发布成功',
      data: newPost
    }
  },

  // 点赞/取消点赞
  toggleLike: async (postId) => {
    await delay()
    const post = mockPosts.find(p => p.postId === parseInt(postId))
    if (post) {
      post.isLiked = !post.isLiked
      post.likeCount += post.isLiked ? 1 : -1
      return {
        code: 200,
        msg: post.isLiked ? '点赞成功' : '取消点赞'
      }
    }
    return {
      code: 500,
      msg: '操作失败'
    }
  },

  // 收藏/取消收藏
  toggleCollect: async (postId) => {
    await delay()
    const post = mockPosts.find(p => p.postId === parseInt(postId))
    if (post) {
      post.isCollected = !post.isCollected
      post.collectCount += post.isCollected ? 1 : -1
      return {
        code: 200,
        msg: post.isCollected ? '收藏成功' : '取消收藏'
      }
    }
    return {
      code: 500,
      msg: '操作失败'
    }
  },

  // 获取评论列表
  getCommentList: async (params) => {
    await delay()
    let comments = mockComments.filter(c => c.postId === parseInt(params.postId))
    return {
      code: 200,
      msg: '操作成功',
      data: comments
    }
  },

  // 管理端：评论分页列表（扁平化树形评论，含帖子标题）
  getCommentAdminList: async (params) => {
    await delay()
    const postTitleMap = {}
    mockPosts.forEach(p => {
      postTitleMap[p.postId] = p.title
    })
    const flat = []
    function walk(nodes) {
      if (!nodes) return
      for (const c of nodes) {
        flat.push({
          ...c,
          postTitle: postTitleMap[c.postId] || '',
          children: undefined
        })
        if (c.children && c.children.length) walk(c.children)
      }
    }
    walk(mockComments)
    let rows = flat
    if (params.title) {
      const kw = String(params.title).trim()
      if (kw) rows = rows.filter(r => (r.postTitle || '').includes(kw))
    }
    if (params.commentContent) {
      const kw = String(params.commentContent).trim()
      if (kw) rows = rows.filter(r => (r.content || '').includes(kw))
    }
    const total = rows.length
    const pageNum = parseInt(params.pageNum, 10) || 1
    const pageSize = parseInt(params.pageSize, 10) || 10
    const start = (pageNum - 1) * pageSize
    rows = rows.slice(start, start + pageSize)
    return {
      code: 200,
      msg: '操作成功',
      rows,
      total
    }
  },

  restoreComment: async (commentId) => {
    await delay()
    const id = parseInt(commentId, 10)
    function findAndRestore(nodes) {
      if (!nodes) return false
      for (const c of nodes) {
        if (c.commentId === id) {
          c.delFlag = '0'
          return true
        }
        if (c.children && findAndRestore(c.children)) return true
      }
      return false
    }
    if (findAndRestore(mockComments)) {
      return { code: 200, msg: '恢复成功' }
    }
    return Promise.reject({ code: 500, msg: '评论不存在' })
  },

  setCommentDelFlag: async ({ commentId, commentDelFlag }) => {
    await delay()
    const id = parseInt(commentId, 10)
    const flag = String(commentDelFlag)
    function findAndSet(nodes) {
      if (!nodes) return false
      for (const c of nodes) {
        if (c.commentId === id) {
          c.delFlag = flag
          return true
        }
        if (c.children && findAndSet(c.children)) return true
      }
      return false
    }
    if (findAndSet(mockComments)) {
      return { code: 200, msg: '操作成功' }
    }
    return Promise.reject({ code: 500, msg: '评论不存在' })
  },

  // 发表评论
  addComment: async (data) => {
    await delay()
    const newComment = {
      commentId: mockComments.length + 1,
      ...data,
      likeCount: 0,
      status: '0',
      delFlag: '0',
      nickName: '当前用户',
      avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      replyNickName: data.replyUserId ? '被回复用户' : null,
      createTime: new Date().toLocaleString('zh-CN'),
      isLiked: false,
      children: []
    }
    if (newComment.parentId === 0) {
      mockComments.push(newComment)
    } else {
      const parent = mockComments.find(c => c.commentId === newComment.parentId)
      if (parent) {
        parent.children.push(newComment)
      }
    }
    // 更新帖子评论数
    const post = mockPosts.find(p => p.postId === parseInt(data.postId))
    if (post) {
      post.commentCount += 1
    }
    return {
      code: 200,
      msg: '评论成功',
      data: newComment
    }
  },

  // 点赞评论
  toggleCommentLike: async (commentId) => {
    await delay()
    const comment = mockComments.find(c => c.commentId === parseInt(commentId))
    if (comment) {
      comment.isLiked = !comment.isLiked
      comment.likeCount += comment.isLiked ? 1 : -1
      return {
        code: 200,
        msg: comment.isLiked ? '点赞成功' : '取消点赞'
      }
    }
    return {
      code: 500,
      msg: '操作失败'
    }
  },

  // 点踩评论
  toggleCommentDislike: async (commentId) => {
    await delay()
    const comment = mockComments.find(c => c.commentId === parseInt(commentId))
    if (comment) {
      comment.isDisliked = !comment.isDisliked
      const currentCount = Number(comment.dislikeCount || 0)
      comment.dislikeCount = comment.isDisliked ? currentCount + 1 : Math.max(0, currentCount - 1)
      return {
        code: 200,
        msg: comment.isDisliked ? '点踩成功' : '取消点踩'
      }
    }
    return {
      code: 500,
      msg: '操作失败'
    }
  },

  // 获取热门标签
  getHotTags: async () => {
    await delay()
    return {
      code: 200,
      msg: '操作成功',
      data: mockTags.sort((a, b) => b.useCount - a.useCount).slice(0, 10)
    }
  },

  // 获取热门文章
  getHotPosts: async (limit = 5) => {
    await delay()
    const hotPosts = [...mockPosts]
      .sort((a, b) => (b.viewCount + b.likeCount * 2) - (a.viewCount + a.likeCount * 2))
      .slice(0, limit)
    return {
      code: 200,
      msg: '操作成功',
      data: hotPosts
    }
  },

  // 检测敏感词
  checkSensitiveWords: async (text) => {
    await delay(300) // 模拟检测延迟
    
    if (!text || typeof text !== 'string') {
      return {
        code: 200,
        msg: '检测通过',
        data: { hasSensitive: false, words: [] }
      }
    }

    // 去除HTML标签，只检测纯文本
    const plainText = text.replace(/<[^>]+>/g, '').toLowerCase()
    
    // 检测是否包含敏感词
    const foundWords = []
    for (const word of mockSensitiveWords) {
      if (plainText.includes(word.toLowerCase())) {
        foundWords.push(word)
      }
    }

    if (foundWords.length > 0) {
      // 返回错误，表示检测到敏感词
      return Promise.reject({
        code: 500,
        msg: `内容包含敏感词：${foundWords.join('、')}，请修改后重新发布`,
        data: { hasSensitive: true, words: foundWords }
      })
    }

    // 检测通过
    return {
      code: 200,
      msg: '检测通过',
      data: { hasSensitive: false, words: [] }
    }
  }
}
