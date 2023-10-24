import { spawnSync } from "child_process";

const main = async () => {
    const [tag] = process.argv.slice(2);
    if (!tag) throw new Error("Missing argument: tag");
    const proc = spawnSync("git", [
        "ls-remote",
        "--exit-code",
        "--tags",
        "origin",
        tag,
    ]);
    if (proc.status === 0) throw new Error(`Tag ${tag} already exists`);
    console.log(`Tag ${tag} is available`);
};

main();
