import { spawnSync } from "child_process";

const main = async () => {
    const proc = spawnSync("git", ["rev-parse", "--short", "HEAD"]);
    if (proc.status !== 0) throw new Error(`Unable to get ref`);
    console.log(proc.stdout.toString().trim());
};

main();
