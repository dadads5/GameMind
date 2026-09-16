package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.dto.auth.UpdateProfileRequest;
import com.zuel.springtest.entity.User;
import com.zuel.springtest.entity.UserLike;
import com.zuel.springtest.mapper.UserLikeMapper;
import com.zuel.springtest.mapper.UserMapper;
import com.zuel.springtest.mapper.PostMapper;
import com.zuel.springtest.mapper.CommentMapper;
import com.zuel.springtest.vo.UserStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 用户业务
 *
 * <p>所有失败场景统一抛出 {@link BusinessException}，由全局异常处理器转换为响应。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{3,20}$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";

    /** 账号状态：正常 */
    public static final int STATUS_NORMAL = 1;
    /** 账号状态：禁用 */
    public static final int STATUS_DISABLED = 0;

    private final UserMapper userMapper;
    private final UserLikeMapper userLikeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final PasswordEncoder passwordEncoder;
    private final StatisticsService statisticsService;

    // ------------------------------------------------------------------
    // 注册与认证
    // ------------------------------------------------------------------

    /**
     * 用户注册
     */
    @Transactional
    public User register(String username, String rawPassword, String email) {
        if (!StringUtils.hasText(username) || !username.matches(USERNAME_PATTERN)) {
            throw new BusinessException("用户名只能包含字母、数字、下划线，长度 3-20 位");
        }
        if (!StringUtils.hasText(rawPassword) || rawPassword.length() < 6 || rawPassword.length() > 100) {
            throw new BusinessException("密码长度需要 6-100 位");
        }
        if (!StringUtils.hasText(email) || !email.matches(EMAIL_PATTERN) || email.length() > 100) {
            throw new BusinessException("邮箱格式不正确");
        }

        if (userMapper.existsByUsername(username)) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        if (userMapper.existsByEmail(email)) {
            throw new BusinessException(ResultCode.CONFLICT, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        // 密码加密存储
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setNickname(username);
        user.setAvatar("");
        user.setBio("");
        user.setStatus(STATUS_NORMAL);
        user.setCreatedAt(LocalDateTime.now());

        if (userMapper.insert(user) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "注册失败，请稍后重试");
        }
        log.info("新用户注册成功：{}（id={}）", username, user.getId());
        statisticsService.evict();
        return user;
    }

    /**
     * 用户名密码认证
     *
     * @return 认证通过的用户
     * @throws BusinessException 用户名或密码错误、账号被禁用
     */
    public User authenticate(String username, String rawPassword) {
        User user = userMapper.selectByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (Integer.valueOf(STATUS_DISABLED).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        return user;
    }

    /**
     * 把明文密码重置为 BCrypt 哈希（用于存量数据迁移）
     *
     * @return 是否发生了修改
     */
    @Transactional
    public boolean migratePasswordToBcrypt(Long userId, String rawPassword) {
        User user = userMapper.selectById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        // BCrypt 哈希以 $2a$ / $2b$ / $2y$ 开头且长度为 60，据此判断是否需要迁移
        String current = user.getPassword();
        if (current != null && current.length() == 60 && current.matches("^\\$2[aby]\\$.*")) {
            return false;
        }
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(rawPassword));
        return true;
    }

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    public Optional<User> findById(Long id) {
        return userMapper.selectById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    public User getById(Long id) {
        return userMapper.selectById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "用户不存在"));
    }

    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    public int countAll() {
        return userMapper.countAll();
    }

    public int countToday() {
        return userMapper.countToday();
    }

    /**
     * 用户活跃度统计（用于前端计算等级与徽章）
     */
    public UserStatsVO getStats(Long userId) {
        User user = getById(userId);
        int totalPosts = postMapper.countByAuthor(userId);
        int totalReplies = commentMapper.countByAuthor(userId);
        int totalLikes = postMapper.sumLikeCountByAuthor(userId);
        int homeLikeCount = user.getLikeCount() != null ? user.getLikeCount() : 0;
        return UserStatsVO.of(totalPosts, totalReplies, totalLikes, homeLikeCount);
    }

    // ------------------------------------------------------------------
    // 更新
    // ------------------------------------------------------------------

    /**
     * 修改个人资料
     *
     * <p>请求中为 null 的字段表示不修改。
     */
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getById(userId);

        if (StringUtils.hasText(request.getUsername())
                && !request.getUsername().equals(user.getUsername())) {
            if (userMapper.existsByUsername(request.getUsername())) {
                throw new BusinessException(ResultCode.CONFLICT, "用户名已被使用");
            }
            user.setUsername(request.getUsername());
        }

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            if (userMapper.existsByEmail(request.getEmail())) {
                throw new BusinessException(ResultCode.CONFLICT, "邮箱已被注册");
            }
            user.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        user.setUpdatedAt(LocalDateTime.now());

        if (userMapper.update(user) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新资料失败");
        }
        return user;
    }

    /**
     * 给某用户主页点赞：同一人对同一人每天一次。
     */
    @Transactional
    public void likeUser(Long likerId, Long likedUserId) {
        if (likerId.equals(likedUserId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能给自己点赞");
        }
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        if (userLikeMapper.countByLikerAndDate(likerId, likedUserId, today) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "今天已经赞过啦，明天再来~");
        }
        UserLike like = new UserLike();
        like.setId((long) (userLikeMapper.selectMaxId() + 1));
        like.setLikerId(likerId);
        like.setLikedUserId(likedUserId);
        like.setLikeDate(today);
        like.setCreatedAt(LocalDateTime.now());
        userLikeMapper.insert(like);
        userMapper.incrementLikeCount(likedUserId);
    }

    /**
     * 修改密码，必须校验原密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "原密码不正确");
        }
        if (newPassword.length() < 6 || newPassword.length() > 100) {
            throw new BusinessException("新密码长度需要 6-100 位");
        }

        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
        log.info("用户 {} 修改密码成功", user.getUsername());
    }

    /**
     * 更新头像
     */
    @Transactional
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = getById(userId);
        if (userMapper.updateAvatar(userId, avatarUrl) <= 0) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新头像失败");
        }
        user.setAvatar(avatarUrl);
        return user;
    }
}
