import dayjs from "dayjs";

export class DateUtil {
  public static formatDateTime(value?: string | number | Date | null): string {
    if (!value) {
      return "-";
    }
    return dayjs(value).format("YYYY-MM-DD HH:mm:ss");
  }
}
