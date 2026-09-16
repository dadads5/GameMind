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
}

/**
 * 调用 AI 问答接口，直接返回回答文本
 */
export const askAI = async (
  question: string,
  history: ChatMessage[] = [],
): Promise<string> => {
  const res = await request.post<string>('/ai/ask', { question, history })
  return res?.data ?? ''
}

/**
 * 流式调用 AI 问答接口（SSE）
 *
 * <p>使用原生 fetch 读取 {@code /api/ai/ask/stream} 的 text/event-stream 响应，
 * 逐段把增量文本通过 {@link StreamCallbacks.onToken} 吐出。后端事件约定：
 * <ul>
 *   <li>{@code event: token} / {@code data: "<片段>"} —— 文本片段</li>
 *   <li>{@code event: done} —— 结束</li>
 *   <li>{@code event: error} / {@code data: "<信息>"} —— 出错</li>
 * </ul>
 */
export const streamAI = async (
  question: string,
  history: ChatMessage[] = [],
  callbacks: StreamCallbacks = {},
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
      body: JSON.stringify({ question, history }),
    })
  } catch (e) {
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
    callbacks.onError?.('AI 回复中断')
  }
}

export default { askAI, streamAI }
