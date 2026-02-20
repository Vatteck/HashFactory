import fs from "node:fs/promises";
import path from "node:path";
import os from "node:os";

export default async (event) => {
  if (event.type !== "command" || event.action !== "new") {
    return;
  }

  try {
    const context = event.context || {};
    const workspaceDir = context.workspaceDir || path.join(os.homedir(), ".openclaw", "workspace");
    const memoryDir = path.join(workspaceDir, "memory");
    await fs.mkdir(memoryDir, { recursive: true });

    const now = new Date(event.timestamp);
    const dateStr = now.toISOString().split("T")[0];
    const timeSlug = now.toISOString().split("T")[1].split(".")[0].replace(/:/g, "");
    const slug = timeSlug.slice(0, 4);

    const filename = `${dateStr}-${slug}.md`;
    const memoryFilePath = path.join(memoryDir, filename);

    const sessionEntry = (context.previousSessionEntry || context.sessionEntry || {});
    const sessionFile = sessionEntry.sessionFile;
    
    let sessionContent = "";
    if (sessionFile) {
        try {
            const rawContent = await fs.readFile(sessionFile, "utf-8");
            const lines = rawContent.trim().split("\n");
            for (const line of lines) {
                try {
                    const entry = JSON.parse(line);
                    if (entry.type === "message" && entry.message) {
                        const msg = entry.message;
                        if ((msg.role === "user" || msg.role === "assistant") && msg.content) {
                            const text = Array.isArray(msg.content) ? msg.content.find(c => c.type === "text")?.text : msg.content;
                            if (text && !text.startsWith("/")) {
                                sessionContent += `${msg.role.toUpperCase()}: ${text}\n`;
                            }
                        }
                    }
                } catch (e) {}
            }
        } catch (e) {}
    }

    const timeStr = now.toISOString().split("T")[1].split(".")[0];
    const entry = `# Session: ${dateStr} ${timeStr} UTC\n\n- **Session Key**: ${event.sessionKey}\n- **Source**: ${context.commandSource || "unknown"}\n\n## Conversation Summary\n\n${sessionContent || "No messages recorded."}`;

    await fs.writeFile(memoryFilePath, entry, "utf-8");
    console.log(`[session-memory] Session saved to memory/${filename}`);
  } catch (err) {
    console.error("[session-memory] Failed to save session:", err);
  }
};
