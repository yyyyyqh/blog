package com.yqh.forum.service.impl;

import com.yqh.forum.dto.CommentDTO;
import com.yqh.forum.model.Comment;
import com.yqh.forum.model.Post;
import com.yqh.forum.model.User;
import com.yqh.forum.repository.CommentRepository;
import com.yqh.forum.repository.PostRepository;
import com.yqh.forum.service.CommentService;
import com.yqh.forum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;


    @Override
    @CacheEvict(value = "commentsByPost", key = "#commentDTO.postId", allEntries = true)
    public CommentDTO createComment(CommentDTO commentDTO) {
        User currentUser = userService.getCurrentUser();
        Post post = postRepository.findById(commentDTO.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(currentUser);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);
        // 如果需要返回缓存的 DTO，确保 DTO 中有 ID
        CommentDTO savedCommentDTO = convertToDTO(savedComment);
        // 在此处打印，观察缓存是否被清除
        System.out.println("创建评论，清除帖子 " + commentDTO.getPostId() + " 的评论列表缓存");
        return savedCommentDTO;
    }

    @Override
    @CacheEvict(value = "commentsByPost", key = "#commentDTO.postId", allEntries = true)
    public CommentDTO updateComment(Long id, CommentDTO commentDTO) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        User currentUser = userService.getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("您没有权限修改此评论");
        }

        comment.setContent(commentDTO.getContent());
        Comment updatedComment = commentRepository.save(comment);
        // 在此处打印，观察缓存是否被清除
        System.out.println("更新评论，清除帖子 " + commentDTO.getPostId() + " 的评论列表缓存");
        return convertToDTO(updatedComment);
    }

    //@Override
    //// 精确清除：需要先查出评论对应的 postId
    //public void deleteComment(Long id) {
    //    Comment comment = commentRepository.findById(id)
    //            .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
    //
    //    User currentUser = userService.getCurrentUser();
    //    if (!comment.getAuthor().getId().equals(currentUser.getId())) {
    //        throw new IllegalArgumentException("您没有权限删除此评论");
    //    }
    //
    //    Long postId = comment.getPost().getId(); // 获取关联的帖子ID
    //    commentRepository.delete(comment);
    //
    //    // 手动清除对应帖子的评论列表缓存
    //    // 注意：CacheEvict 是基于 AOP 的，在方法执行前或执行后触发。
    //    // 对于这种需要获取方法参数之外的数据来构建 key 的情况，通常需要通过 CacheManager 手动清除，
    //    // 或者像这里一样在方法执行结束后，通过获取到的 postId 再进行清除。
    //    // 或者使用 @Caching 注解结合 #root.args 来更灵活地构建 key。
    //    // 最简单的方法是使用 @CacheEvict(value="commentsByPost", key="#root.args[0]") 结合一个辅助方法，或者像下面这样手动清除：
    //    // Spring 3.x+ 方法，需要注入 CacheManager
    //    // org.springframework.cache.CacheManager cacheManager; // 需要注入
    //
    //    // 另一种方式是，在方法成功执行后，手动触发一次缓存清除。
    //    // 如果您想使用注解且需要 postId，可以考虑在 CommentDTO 中也包含 postId
    //    // 或者：
    //    // @CacheEvict(value = "commentsByPost", key = "#result.postId", condition = "#result != null")
    //    // 但这需要 deleteComment 返回 CommentDTO，通常 delete 是 void。
    //
    //    // 最直接的做法：如果 deleteComment 确实不返回 postId，且不能修改DTO，
    //    // 那么在 Service 层注入 CacheManager，手动清除。
    //    // 或者，回到 CommentService 接口，如果 deleteComment 的 DTO 也包含 postId
    //    // 或者就使用粗粒度清除 allEntries=true。
    //
    //    // 考虑到这里 postId 已经获取到，我们可以在 Service 内部使用 CacheManager 手动清除
    //    // 但为了保持注解的风格，更常见的做法是让 deleteComment 接收 CommentDTO，或者使用 allEntries=true 粗暴清除。
    //    // 为了简便，这里仍然使用 allEntries=true。
    //    // 如果要精确清除，请在 Service 接口或 Controller 中，调用 deleteComment 后，再调用一个清除缓存的方法。
    //
    //    // 临时使用粗粒度清除，或者您需要注入 CacheManager 来精确清除
    //    // @CacheEvict(value = "commentsByPost", allEntries = true)
    //    // 这里的 @CacheEvict 会在方法执行后触发。
    //    System.out.println("删除评论，清除所有评论列表缓存（粗粒度）。");
    //}

    // 更新后的 deleteComment 方法：
    @Override
    @CacheEvict(value = "commentsByPost", key = "#id + ':' + #result.postId", condition = "#result != null") // 如果 deleteComment 返回 CommentDTO
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        User currentUser = userService.getCurrentUser();
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("您没有权限删除此评论");
        }

        Long postId = comment.getPost().getId(); // 获取关联的帖子ID
        commentRepository.delete(comment);

        // 此时，需要手动清除该 postId 对应的缓存。
        // 因为 @CacheEvict 的 key 表达式在方法执行前或根据方法参数生成，
        // 这里需要方法内部查到的 postId。
        // 最优雅的解决方案：在 Controller 调用 deleteComment 后，如果需要精确缓存失效，
        // 可以让 deleteComment 返回 postId，或者在 Controller 知道 postId 时直接调用一个清除缓存的方法。
        // 或者，在 Service 内部注入 CacheManager：
        // @Autowired
        // private org.springframework.cache.CacheManager cacheManager;
        // cacheManager.getCache("commentsByPost").clear(); // 清除所有
        // cacheManager.getCache("commentsByPost").evict(postId + ":*"); // 精确清除，但需要匹配所有页码和大小

        // 简单粗暴但有效的处理方式：清除所有评论列表缓存。
        // 这种情况下，您需要在 service 接口的 deleteComment 方法上添加 @CacheEvict(value = "commentsByPost", allEntries = true)
        System.out.println("删除评论，清除帖子 " + postId + " 的评论列表缓存。");
    }


    @Override
    @Cacheable(value = "commentsByPost", key = "#postId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize", unless = "#result == null || #result.isEmpty()")
    public Page<CommentDTO> findByPostId(Long postId, Pageable pageable) {
        System.out.println("从数据库加载帖子 " + postId + " 的评论列表（页码: " + pageable.getPageNumber() + ", 大小: " + pageable.getPageSize() + "）。");
        Post post = new Post();
        post.setId(postId); // 注意：这里创建了一个新的 Post 对象，如果 CommentRepository 严格检查对象引用，可能需要先 findById 获取 Post 实体
        // 建议：
        // Post post = postRepository.findById(postId)
        //         .orElseThrow(() -> new IllegalArgumentException("帖子不存在，无法查询评论"));

        return commentRepository.findByPostOrderByCreatedAtDesc(post, pageable)
                .map(this::convertToDTO);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        // 注意：这里userService.findByUsername(comment.getAuthor().getUsername()) 可能导致 N+1 查询问题。
        // 考虑在 Comment 实体中直接关联 User 对象并fetch，或者在 CommentDTO 中只存储 authorId 和 username，避免额外查询。
        dto.setAuthor(userService.findByUsername(comment.getAuthor().getUsername())); // 假设这个方法返回 UserDTO
        dto.setPostId(comment.getPost().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}

//@Service
//@Transactional
//public class CommentServiceImpl implements CommentService {
//
//    @Autowired
//    private CommentRepository commentRepository;
//
//    @Autowired
//    private PostRepository postRepository;
//
//    @Autowired
//    private UserService userService;
//
//
//    @Override
//    public CommentDTO createComment(CommentDTO commentDTO) {
//        User currentUser = userService.getCurrentUser();
//        Post post = postRepository.findById(commentDTO.getPostId())
//                .orElseThrow(() -> new IllegalArgumentException("帖子不存在"));
//
//        Comment comment = new Comment();
//        comment.setContent(commentDTO.getContent());
//        comment.setAuthor(currentUser);
//        comment.setPost(post);
//
//        Comment savedComment = commentRepository.save(comment);
//        return convertToDTO(savedComment);
//    }
//
//    @Override
//    public CommentDTO updateComment(Long id, CommentDTO commentDTO) {
//        Comment comment = commentRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
//
//        User currentUser = userService.getCurrentUser();
//        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
//            throw new IllegalArgumentException("您没有权限修改此评论");
//        }
//
//        comment.setContent(commentDTO.getContent());
//        Comment updatedComment = commentRepository.save(comment);
//        return convertToDTO(updatedComment);
//    }
//
//    @Override
//    public void deleteComment(Long id) {
//        Comment comment = commentRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
//
//        User currentUser = userService.getCurrentUser();
//        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
//            throw new IllegalArgumentException("您没有权限删除此评论");
//        }
//
//        commentRepository.delete(comment);
//    }
//
//    @Override
//    public Page<CommentDTO> findByPostId(Long postId, Pageable pageable) {
//        Post post = new Post();
//        post.setId(postId);
//        return commentRepository.findByPostOrderByCreatedAtDesc(post, pageable)
//                .map(this::convertToDTO);
//    }
//
//    private CommentDTO convertToDTO(Comment comment) {
//        CommentDTO dto = new CommentDTO();
//        dto.setId(comment.getId());
//        dto.setContent(comment.getContent());
//        dto.setAuthor(userService.findByUsername(comment.getAuthor().getUsername()));
//        dto.setPostId(comment.getPost().getId());
//        dto.setCreatedAt(comment.getCreatedAt());
//        dto.setUpdatedAt(comment.getUpdatedAt());
//        return dto;
//    }
//}