import { message, notification } from "antd";

export class ToastUtil {
  public static success(content: string): void {
    message.success(content);
  }

  public static warning(content: string): void {
    notification.warning({ message: content, placement: "topRight" });
  }

  public static error(content: string): void {
    notification.error({ message: content, placement: "topRight" });
  }
}
