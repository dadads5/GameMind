package com.zuel.springtest.services;

import com.zuel.springtest.common.BusinessException;
import com.zuel.springtest.common.ResultCode;
import com.zuel.springtest.entity.AiConversation;
import com.zuel.springtest.entity.AiMessage;
import com.zuel.springtest.mapper.AiConversationMapper;
import com.zuel.springtest.mapper.AiMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * AI 会话持久化
 *
 * <p>把原本由前端临时携带的对话历史改为服务端存储，好处：
 * <ul>
 *   <li>刷新页面 / 换设备后可继续同一会话；</li>
 *   <li>前端无需每次请求携带 20 条历史，减少请求体体积；</li>
 *   <li>会话归属在服务端校验，避免伪造 conversationId 越权读取。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationService {

    /** 取首条提问的前 N 个字符作为会话标题 */
    private static final int TITLE_MAX = 20;

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    /**
     * 解析会话：未传 id 时按首条提问自动新建会话。
     *
     * @return 会话 id
     */
    public Long resolve(Long conversationId, Long userId, String firstQuestion) {
        if (conversationId != null) {
            AiConversation existed = conversationMapper.selectById(conversationId);
            if (existed != null && existed.getUserId().equals(userId)) {
                return conversationId;
            }
            // id 不存在或不属于当前用户：不报错，降级为新建，保证聊天不中断
            log.warn("会话 {} 不存在或不属于用户 {}，自动新建会话", conversationId, userId);
        }
        return create(userId, firstQuestion);
    }

    public Long create(Long userId, String firstQuestion) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(buildTitle(firstQuestion));
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    public List<AiConversation> listByUser(Long userId) {
        return conversationMapper.selectByUser(userId);
    }

    /**
     * 读取会话消息，带归属校验
     */
    public List<AiMessage> listMessages(Long conversationId, Long userId) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        return messageMapper.selectByConversation(conversationId);
    }

    /**
     * 删除会话及其消息，带归属校验
     */
    public void delete(Long conversationId, Long userId) {
        int rows = conversationMapper.deleteByIdAndUser(conversationId, userId);
        if (rows <= 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "会话不存在或无权删除");
        }
        messageMapper.deleteByConversation(conversationId);
    }

    /**
     * 保存一条消息，并刷新会话更新时间
     */
    public void saveMessage(Long conversationId, String role, String content) {
        if (conversationId == null || !StringUtils.hasText(content)) {
            return;
        }
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        try {
            messageMapper.insert(message);
            conversationMapper.touch(conversationId);
        } catch (Exception e) {
            // 会话持久化属于附加能力，失败不应影响 AI 回答
            log.warn("保存 AI 会话消息失败 conversation={} role={}", conversationId, role, e);
        }
    }

    /**
     * 把历史消息转为可直接发给模型的上下文
     */
    public List<AiMessage> context(Long conversationId, Long userId) {
        try {
            return listMessages(conversationId, userId);
        } catch (Exception e) {
            log.warn("读取会话上下文失败，降级为无上下文 conversation={}", conversationId, e);
            return List.of();
        }
    }

    private String buildTitle(String question) {
        if (!StringUtils.hasText(question)) {
            return "新会话";
        }
        String plain = question.replaceAll("\\s+", " ").trim();
        return plain.length() <= TITLE_MAX ? plain : plain.substring(0, TITLE_MAX) + "…";
    }
}
