# 热门分类和热门文章后端接口说明

## 新增接口

### 1. 热门文章接口

#### Controller层
**文件**: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/bbs/BbsPostController.java`

**接口**: `GET /bbs/post/hot`

**参数**:
- `limit` (Integer, 可选): 返回数量限制，默认5

**返回**: 热门文章列表

**示例**:
```java
@GetMapping("/hot")
public AjaxResult getHotPosts(Integer limit)
```

#### Service层
**文件**: `ruoyi-system/src/main/java/com/ruoyi/system/service/IBbsPostService.java`

**方法**: `selectHotPostList(Integer limit)`

#### Mapper层
**文件**: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/BbsPostMapper.java`

**方法**: `selectHotPostList(Integer limit)`

#### SQL实现
**文件**: `ruoyi-system/src/main/resources/mapper/system/BbsPostMapper.xml`

**排序规则**: 
- 按热度排序：`(view_count + like_count * 2 + comment_count) DESC`
- 相同热度按时间排序：`create_time DESC`

**SQL示例**:
```xml
<select id="selectHotPostList" parameterType="Integer" resultMap="BbsPostResult">
    <include refid="selectBbsPostVo"/>
    where p.del_flag = '0' and p.status = '0'
    order by (p.view_count + p.like_count * 2 + p.comment_count) desc, p.create_time desc
    limit #{limit}
</select>
```

### 2. 热门分类接口

#### Controller层
**文件**: `ruoyi-admin/src/main/java/com/ruoyi/web/controller/bbs/BbsCategoryController.java`

**接口**: `GET /bbs/category/hot`

**返回**: 热门分类列表（按帖子数排序）

**示例**:
```java
@GetMapping("/hot")
public AjaxResult getHotCategories()
```

#### Service层
**文件**: `ruoyi-system/src/main/java/com/ruoyi/system/service/IBbsCategoryService.java`

**方法**: `selectHotCategoryList()`

#### Mapper层
**文件**: `ruoyi-system/src/main/java/com/ruoyi/system/mapper/BbsCategoryMapper.java`

**方法**: `selectHotCategoryList()`

#### SQL实现
**文件**: `ruoyi-system/src/main/resources/mapper/system/BbsCategoryMapper.xml`

**排序规则**: 
- 按帖子数降序：`post_count DESC`
- 相同帖子数按排序字段：`sort_order ASC`

**SQL示例**:
```xml
<select id="selectHotCategoryList" resultMap="BbsCategoryResult">
    <include refid="selectBbsCategoryVo"/>
    where status = '0'
    order by post_count desc, sort_order
</select>
```

## 热度计算规则

### 热门文章热度计算公式
```
热度 = 浏览量 + 点赞数 × 2 + 评论数
```

**权重说明**:
- 浏览量：权重 1
- 点赞数：权重 2（更重要的互动指标）
- 评论数：权重 1

### 热门分类排序规则
- 按分类下的帖子总数降序排列
- 帖子数相同的按排序字段升序排列

## API调用示例

### 获取热门文章
```javascript
// 前端调用
import { getHotPosts } from "@/api/bbs/post"

// 获取前5篇热门文章
getHotPosts(5).then(response => {
  console.log(response.data)
})

// 后端接口
GET /bbs/post/hot?limit=5
```

### 获取热门分类
```javascript
// 前端调用
import { getHotCategories } from "@/api/bbs/category"

// 获取热门分类
getHotCategories().then(response => {
  console.log(response.data)
})

// 后端接口
GET /bbs/category/hot
```

## 数据库优化建议

### 索引优化
为了提升查询性能，建议在以下字段上创建索引：

```sql
-- 帖子表索引
CREATE INDEX idx_post_hot ON bbs_post(del_flag, status, view_count, like_count, comment_count, create_time);
CREATE INDEX idx_post_category ON bbs_post(category_id, del_flag, status);

-- 分类表索引
CREATE INDEX idx_category_hot ON bbs_category(status, post_count);
```

### 缓存建议
- 热门文章列表：建议缓存5-10分钟
- 热门分类列表：建议缓存30分钟
- 使用Redis缓存，key格式：`bbs:hot:posts:limit:{limit}` 和 `bbs:hot:categories`

## 注意事项

1. **数据一致性**
   - 分类的`post_count`字段需要与实际的帖子数量保持一致
   - 建议使用定时任务定期同步统计

2. **性能优化**
   - 热门文章查询涉及多表关联，注意SQL性能
   - 可以考虑使用视图或物化视图

3. **扩展性**
   - 热度计算公式可以根据业务需求调整权重
   - 可以添加时间衰减因子，让新文章更容易上热门

## 测试建议

### 单元测试
```java
@Test
public void testSelectHotPostList() {
    List<BbsPost> hotPosts = bbsPostService.selectHotPostList(5);
    assertNotNull(hotPosts);
    assertTrue(hotPosts.size() <= 5);
}

@Test
public void testSelectHotCategoryList() {
    List<BbsCategory> hotCategories = bbsCategoryService.selectHotCategoryList();
    assertNotNull(hotCategories);
}
```

### 接口测试
```bash
# 测试热门文章接口
curl -X GET "http://localhost:8080/bbs/post/hot?limit=5"

# 测试热门分类接口
curl -X GET "http://localhost:8080/bbs/category/hot"
```
