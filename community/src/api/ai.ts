import request from './request'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export interface StreamCallbacks {
  /** 每收到一段增量文本时回调 */
  onToken?: (text: string) => void
  /** 流正常结束时回调 */
  onDone?: () => void
  /** 出错时回调（参数为错误信息） */
  onError?: (message: string) => void
  /** 服务端分配的会话 id（首个事件），前端据此沿用同一会话 */
  onConversation?: (id: number) => void
  /** RAG 检索到的来源片段（sources 事件），用于「可溯源」展示 */
  onSources?: (sources: { postId: number; title: string }[]) => void
}

/** 流式请求可选参数 */
export interface StreamOptions {
  /** 用于中断生成（AbortController.signal） */
  signal?: AbortSignal
  /** 会话 id：传入则沿用该会话，不传由服务端自动新建 */
  conversationId?: number
  /** 是否持久化到会话，默认 true；润写/智能回复等一次性辅助应传 false */
  persist?: boolean
}

/** 同步问答可选参数 */
export interface AskOptions {
  /** 会话 id */
  conversationId?: number
  /** 是否持久化到会话，默认 true；润写等一次性辅助传 false */
  persist?: boolean
}

export interface AiConversation {
  id: number
  userId: number
  title: string
  createdAt: string
  updatedAt: string
}

export interface AiMessageItem {
  id: number
  conversationId: number
  role: 'user' | 'assistant'
  content: string
  createdAt: string
}

/**
 * 调用 AI 问答接口，直接返回回答文本
 */
export const askAI = async (
  question: string,
  history: ChatMessage[] = [],
  options: AskOptions = {},
): Promise<string> => {
  const res = await request.post<string>('/ai/ask', {
    question,
    history,
    conversationId: options.conversationId,
    persist: options.persist,
  })
  return res?.data ?? ''
}

/**
 * 流式调用 AI 问答接口（SSE）
 *
 * <p>使用原生 fetch 读取 {@code /api/ai/ask/stream} 的 text/event-stream 响应，
 * 逐段把增量文本通过 {@link StreamCallbacks.onToken} 吐出。后端事件约定：
 * <ul>
 *   <li>{@code event: conversation} / {@code data: "<会话id>"} —— 首个事件，会话标识</li>
 *   <li>{@code event: token} / {@code data: "<片段>"} —— 文本片段</li>
 *   <li>{@code event: done} —— 结束</li>
 *   <li>{@code event: error} / {@code data: "<信息>"} —— 出错</li>
 * </ul>
 *
 * 传入 {@link StreamOptions.signal} 可在生成过程中主动中断（AbortController），
 * 中断属于正常行为，不会触发 onError。
 */
export const streamAI = async (
  question: string,
  history: ChatMessage[] = [],
  callbacks: StreamCallbacks = {},
  options: StreamOptions = {},
): Promise<void> => {
  const token = localStorage.getItem('token')
  let response: Response
  try {
    response = await fetch('/api/ai/ask/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        question,
        history,
        conversationId: options.conversationId,
        persist: options.persist,
      }),
      ...(options.signal ? { signal: options.signal } : {}),
    })
  } catch (e) {
    if ((e as Error)?.name === 'AbortError') return
    callbacks.onError?.('网络错误，无法连接 AI 服务')
    return
  }

  if (!response.ok || !response.body) {
    callbacks.onError?.('AI 服务暂时不可用，请稍后重试')
    return
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let eventName = ''

  const dispatch = (line: string) => {
    const trimmed = line.trim()
    if (!trimmed) {
      eventName = ''
      return
    }
    if (trimmed.startsWith('event:')) {
      eventName = trimmed.slice(6).trim()
      return
    }
    if (!trimmed.startsWith('data:')) {
      return
    }
    const data = trimmed.slice(5).trim()
    if (!data || data === '[DONE]') {
      return
    }

    // 会话 id：纯数字，优先处理
    if (eventName === 'conversation') {
      const id = Number(data)
      if (!Number.isNaN(id)) callbacks.onConversation?.(id)
      eventName = ''
      return
    }

    // RAG 来源片段
    if (eventName === 'sources') {
      try {
        const parsed = JSON.parse(data)
        if (Array.isArray(parsed)) callbacks.onSources?.(parsed)
      } catch {
        // 忽略来源解析失败
      }
      eventName = ''
      return
    }

    // 后端用 SseEmitter.event().data(String) 发送，字符串不会经过 JSON 编码，
    // 因此 data 行往往是纯文本（如 "你好"）。优先按 JSON 解析，
    // 解析失败则把原始文本当作增量内容，避免 token 被静默丢弃。
    let text: string | undefined
    try {
      const json = JSON.parse(data)
      text = typeof json === 'string' ? json : (json?.data ?? undefined)
    } catch {
      text = data
    }
    if (eventName === 'error') {
      callbacks.onError?.(text ?? 'AI 回复失败')
      return
    }
    if (text) callbacks.onToken?.(text)
    eventName = ''
  }

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }
      buffer += decoder.decode(value, { stream: true })
      let idx: number
      while ((idx = buffer.indexOf('\n')) >= 0) {
        const line = buffer.slice(0, idx)
        buffer = buffer.slice(idx + 1)
        dispatch(line)
      }
    }
    // 处理残余缓冲
    if (buffer.trim()) {
      dispatch(buffer)
    }
    callbacks.onDone?.()
  } catch (e) {
    // 用户主动中断：按正常结束处理（已生成内容保留）
    if ((e as Error)?.name === 'AbortError') {
      callbacks.onDone?.()
      return
    }
    callbacks.onError?.('AI 回复中断')
  }
}

// ------------------------------------------------------------------
// 会话持久化
// ------------------------------------------------------------------

/** 当前用户的会话列表 */
export const getConversations = async (): Promise<AiConversation[]> => {
  try {
    const res = await request.get<AiConversation[]>('/ai/conversations')
    return res?.data ?? []
  } catch {
    return []
  }
}

/** 新建会话，返回会话 id */
export const createConversation = async (): Promise<number | null> => {
  try {
    const res = await request.post<{ conversationId: number }>('/ai/conversations')
    return res?.data?.conversationId ?? null
  } catch {
    return null
  }
}

/** 某会话的历史消息 */
export const getConversationMessages = async (id: number): Promise<AiMessageItem[]> => {
  try {
    const res = await request.get<AiMessageItem[]>(`/ai/conversations/${id}/messages`)
    return res?.data ?? []
  } catch {
    return []
  }
}

/** 删除会话 */
export const deleteConversationApi = async (id: number): Promise<void> => {
  await request.delete(`/ai/conversations/${id}`)
}

export default { askAI, streamAI }
