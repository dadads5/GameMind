import { ElMessage, ElMessageBox } from 'element-plus'

// 统一的反馈提示，替代散落的 alert / 仅 console.error，保持交互一致
export const notifyError = (message: string) => ElMessage.error(message)
export const notifySuccess = (message: string) => ElMessage.success(message)
export const notifyWarning = (message: string) => ElMessage.warning(message)
export const notifyInfo = (message: string) => ElMessage.info(message)

/**
 * 确认对话框：用 Element Plus 的 MessageBox 取代浏览器原生 confirm，
 * 样式与 notify 一致且支持中文按钮。取消或关闭均返回 false。
 */
export const notifyConfirm = async (message: string, title = '确认操作'): Promise<boolean> => {
  try {
    await ElMessageBox.confirm(message, title, {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
    })
    return true
  } catch {
    return false
  }
}
