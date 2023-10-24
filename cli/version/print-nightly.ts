import { spawnSync } from "child_process";

const main = async () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const date = now.getDate();
    const sha = getRevision();
    const versionName = `${year}.${month}.${date}-${sha}`;
    console.log(versionName);
};

main();

function getRevision() {
    const proc = spawnSync("git", ["rev-parse", "--short", "HEAD"]);
    if (proc.status !== 0) throw new Error(`Unable to get sha`);
    return proc.stdout.toString().trim();
}
