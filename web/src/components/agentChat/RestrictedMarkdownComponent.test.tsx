import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { RestrictedMarkdownComponent } from "./RestrictedMarkdownComponent";

const writeText = vi.fn();

/**
 * 受限 Markdown 组件测试。
 *
 * @author qty
 */
describe("RestrictedMarkdownComponent", () => {
  beforeEach(() => {
    writeText.mockReset();
    Object.defineProperty(navigator, "clipboard", {
      configurable: true,
      value: { writeText }
    });
  });

  it("应将 HTML 作为文本而不创建 HTML 元素", () => {
    const { container } = render(<RestrictedMarkdownComponent content="<script>local text</script>" />);

    expect(container.querySelector("script")).toBeNull();
    expect(screen.getByText("<script>local text</script>")).toBeInTheDocument();
  });

  it("应渲染 GFM 表格横向容器和带语言标识的代码复制按钮", async () => {
    const content = "| 名称 | 值 |\n| --- | --- |\n| 本地 | 测试 |\n\n```java\nclass LocalTest {}\n```";
    const { container } = render(<RestrictedMarkdownComponent content={content} />);

    expect(container.querySelector(".restricted-markdown-table table")).toBeInTheDocument();
    expect(screen.getByText("java")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button"));
    expect(writeText).toHaveBeenCalledWith("class LocalTest {}");
  });
});
