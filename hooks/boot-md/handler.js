import fs from "node:fs/promises";
import path from "node:path";

export default async (event) => {
  if (event.type !== "gateway" || event.action !== "startup") {
    return;
  }

  const workspaceDir = event.context?.workspaceDir;
  if (!workspaceDir) return;

  const bootFile = path.join(workspaceDir, "BOOT.md");
  try {
    const content = await fs.readFile(bootFile, "utf-8");
    console.log("[boot-md] BOOT.md detected.");
    // Return content to be injected as a system event PASSIVELY
    return content;
  } catch (err) {
    // No BOOT.md, skip
  }
};
