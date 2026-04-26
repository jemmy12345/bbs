package com.ruoyi.web.controller.bbs;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsComment;
import com.ruoyi.system.domain.BbsPost;
import com.ruoyi.system.domain.BbsDeptContact;
import com.ruoyi.system.service.IBbsCommentService;
import com.ruoyi.system.service.IBbsPostService;
import com.ruoyi.system.service.IBbsDeptContactService;

/**
 * 评论 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/bbs/comment")
@Api(tags = "评论")
public class BbsCommentController extends BaseController
{

    @Autowired
    private IBbsCommentService bbsCommentService;

    @Autowired
    private IBbsPostService bbsPostService;

    @Autowired
    private IBbsDeptContactService bbsDeptContactService;

    /**
     * 查询评论列表
     */
    @ApiOperation("查询评论列表")
    @GetMapping("/list")
    public AjaxResult list(BbsComment bbsComment)
    {
        List<BbsComment> list = bbsCommentService.selectBbsCommentList(bbsComment);
        return success(list);
    }

    /**
     * 查询个人匿名评论列表
     */
    @ApiOperation("查询个人匿名评论列表")
    @GetMapping("/listAnonymousByKey/{hashCode}")
    public AjaxResult listCommentAnonymousByKey(@PathVariable String hashCode)
    {
        List<BbsComment> list = bbsCommentService.listCommentAnonymousByKey(hashCode);
        return success(list);
    }

    /**
     * 新增评论
     */
    @ApiOperation("新增评论")
    @PostMapping
    public AjaxResult add(@RequestBody BbsComment bbsComment)
    {
        // 验证评论内容或图片至少有一个
        if ((bbsComment.getContent() == null || bbsComment.getContent().trim().isEmpty()) 
            && (bbsComment.getImages() == null || bbsComment.getImages().trim().isEmpty()))
        {
            return error("评论内容或图片至少需要填写一项");
        }
        if (bbsComment.getPostId() == null)
        {
            return error("帖子ID不能为空");
        }

        

        bbsComment.setStatus("0");
        bbsComment.setDelFlag("0");
        bbsComment.setLikeCount(0);
//        bbsComment.setNickName(SecurityUtils.getLoginUser().getUser().getNickName());
        bbsComment.setAvatar(SecurityUtils.getLoginUser().getUser().getAvatar());
        bbsComment.setNickName(SecurityUtils.getLoginUser().getUser().getNickName());

        BbsPost post = bbsPostService.selectBbsPostById(bbsComment.getPostId());
        if (post == null){
            return error("帖子不存在");
        }

        // 检查：如果帖子是建议或意见类型，且当前用户是该帖子回应部门的接口人，则强制实名评论
        String postType = post.getPostType();
        Long responseDeptId = post.getResponseDeptId();
        // 判断帖子类型是否为建议或意见
        if (("suggestion".equals(postType) || "opinion".equals(postType))
                && responseDeptId != null)
        {
            // 查询该部门的接口人
            BbsDeptContact deptContact = bbsDeptContactService.selectBbsDeptContactByDeptId(responseDeptId);
            if (deptContact != null && "0".equals(deptContact.getStatus()))
            {
                String currentUserId = SecurityUtils.getUserId();
                String contactUserId = deptContact.getContactUserId();

                // 如果当前用户是该部门的接口人，强制设置为实名评论
                if (StringUtils.isNotEmpty(contactUserId) && contactUserId.equals(currentUserId))
                {
                    bbsComment.setIsAnonymous("0");
                }
            }
        }
        
        // 如果没有设置匿名标识，默认为实名
        if (bbsComment.getIsAnonymous() == null)
        {
            bbsComment.setIsAnonymous("0");
        }
        
        // 如果是实名评论，从getInfo获取部门名称并保存
        if ("0".equals(bbsComment.getIsAnonymous())) {
            try {
                com.ruoyi.common.core.domain.entity.SysUser user = SecurityUtils.getLoginUser().getUser();
                if (user != null && user.getDept() != null) {
                    bbsComment.setDeptName(user.getDept().getDeptName());
                }
                bbsComment.setUserId(SecurityUtils.getUserId());
                bbsComment.setCreateBy(SecurityUtils.getUsername());
            } catch (Exception e) {
                // 如果获取部门信息失败，不影响评论提交
            }
        } else if ("1".equals(bbsComment.getIsAnonymous())) { //匿名评论
            bbsComment.setUserId(bbsComment.getUserId());
            bbsComment.setCreateBy(bbsComment.getUserId());
            bbsComment.setNickName("匿名用户");
            bbsComment.setAvatar("https://wwcdn.weixin.qq.com/node/wwmng/wwmng/style/images/independent/DefaultAvatar$caf2a2d6.png");
            bbsComment.setReplyNickName("");
            bbsComment.setDeptName("");
        }
        // 如果没有设置图片，默认为空
        if (bbsComment.getImages() == null)
        {
            bbsComment.setImages("");
        }
        // 如果没有设置内容，默认为空
        if (bbsComment.getContent() == null)
        {
            bbsComment.setContent("");
        }
        if (bbsComment.getParentId() == null)
        {
            bbsComment.setParentId(0L);
        }
        bbsCommentService.insertBbsComment(bbsComment);

        return AjaxResult.success();
    }


//    /**
//     * 修改评论
//     */
//    @PutMapping
//    public AjaxResult edit(@Validated @RequestBody BbsComment bbsComment)
//    {
//        return toAjax(bbsCommentService.updateBbsComment(bbsComment));
//    }

    /**
     * 删除评论
     */
    @ApiOperation("删除评论")
    @DeleteMapping("/{commentIds}")
    public AjaxResult remove(@PathVariable Long[] commentIds)
    {
        return toAjax(bbsCommentService.deleteBbsCommentByIds(commentIds));
    }

    /**
     * 个人删除自己的评论
     */
    @ApiOperation("个人删除自己的评论")
    @DeleteMapping("/delByPersonal/{commentId}")
    public AjaxResult delByPersonal(@PathVariable Long commentId)
    {
        bbsCommentService.delByPersonal(commentId);
        return AjaxResult.success();
    }

    /**
     * 管理员删除评论
     */
    @ApiOperation("管理员删除评论")
    @DeleteMapping("/delByAdmin/{commentIds}")
    public AjaxResult delByAdmin(@PathVariable Long[] commentIds)
    {
        bbsCommentService.delByAdmin(commentIds);
        return AjaxResult.success();
    }

    /**
     * 点赞/取消点赞
     */
    @ApiOperation("点赞/取消点赞")
    @PostMapping("/like/{commentId}")
    public AjaxResult toggleLike(@PathVariable Long commentId)
    {
        String userId = SecurityUtils.getUserId();
        int result = bbsCommentService.toggleLike(commentId, userId);
        return success(result > 0 ? "点赞成功" : "取消点赞");
    }
}
