import fs from "node:fs/promises";
import path from "node:path";
import os from "node:os";

export default async (event) => {
  if (event.type !== "command") {
    return;
  }

  const logDir = path.join(os.homedir(), ".openclaw", "logs");
  const logFile = path.join(logDir, "commands.log");

  try {
    await fs.mkdir(logDir, { recursive: true });
    const logEntry = JSON.stringify({
      timestamp: new Date(event.timestamp).toISOString(),
      sessionKey: event.sessionKey,
      action: event.action,
      source: event.context?.commandSource || "unknown"
    }) + "\n";
    
    await fs.appendFile(logFile, logEntry, "utf-8");
  } catch (err) {
    console.error("[command-logger] Failed to log command:", err);
  }
};
