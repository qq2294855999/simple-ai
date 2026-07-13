import { Button, Typography } from "antd";
import { CopyOutlined } from "@ant-design/icons";
import { useCallback } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

interface RestrictedMarkdownComponentProps {
  content: string;
}

/**
 * 受限 Markdown 渲染组件。
 * 不启用 HTML 解析插件，模型返回的 HTML 将作为普通文本处理。
 *
 * @author qty
 */
export function RestrictedMarkdownComponent({ content }: RestrictedMarkdownComponentProps) {
  const copyCode = useCallback(async (code: string) => {
    try {
      await navigator.clipboard.writeText(code);
    } catch {
      // Clipboard API 不可用时不阻断安全文本展示
    }
  }, []);

  try {
    return (
      <div className="restricted-markdown">
        <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          components={{
            table: ({ children, ...props }) => (
              <div className="restricted-markdown-table">
                <table {...props}>{children}</table>
              </div>
            ),
            code: ({ className, children, ...props }) => {
              const code = String(children).replace(/\n$/, "");
              const language = className?.replace("language-", "") || "text";
              const isBlock = className?.startsWith("language-") || code.includes("\n");

              // 代码块提供显式复制操作，行内代码保持紧凑展示
              if (isBlock) {
                return (
                  <div className="restricted-code-block">
                    <div className="restricted-code-header">
                      <Typography.Text type="secondary">{language}</Typography.Text>
                      <Button type="text" size="small" icon={<CopyOutlined />} onClick={() => void copyCode(code)} />
                    </div>
                    <pre><code className={className} {...props}>{code}</code></pre>
                  </div>
                );
              }
              return <code className={className} {...props}>{children}</code>;
            }
          }}
        >
          {content}
        </ReactMarkdown>
      </div>
    );
  } catch {
    return <Typography.Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>{content}</Typography.Paragraph>;
  }
}
