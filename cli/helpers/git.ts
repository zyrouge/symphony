import { spawnSync } from "child_process";

export interface GitSpawnResult {
    status: number | null;
    stdout: string;
    stderr: string;
}

export class Git {
    static async getLatestRevisionShort() {
        const proc = await Git.spawn(["rev-parse", "--short", "HEAD"]);
        if (proc.status !== 0) throw new Error(`Unable to get revision`);
        return proc.stdout;
    }

    static async getRevisionDate(sha: string) {
        const proc = await Git.spawn([
            "show",
            "--no-patch",
            "--no-notes",
            "--pretty='%cI'",
            sha,
        ]);
        if (proc.status !== 0) throw new Error(`Unable to get revision date`);
        const isoString = proc.stdout.slice(1, -1);
        return new Date(isoString);
    }

    static async tagExists(tag: string) {
        const proc = await Git.spawn([
            "ls-remote",
            "--exit-code",
            "--tags",
            "origin",
            tag,
        ]);
        return proc.status === 0;
    }

    static async diffNames(tag: string) {
        const proc = await Git.spawn(["diff", "--name-only", tag, "."]);
        if (proc.status !== 0) {
            throw new Error(`Unable to get diff (${proc.stderr})`);
        }
        return proc.stdout.split("\n");
    }

    static async latestTag() {
        const proc = await Git.spawn(["describe", "--abbrev=0", "--tags"]);
        if (proc.status !== 0) {
            throw new Error(`Unable to get latest tag (${proc.stderr})`);
        }
        return proc.stdout;
    }

    static async spawn(args: string[]) {
        const proc = spawnSync("git", args);
        const result: GitSpawnResult = {
            status: proc.status,
            stdout: proc.stdout.toString().trim(),
            stderr: proc.stderr.toString().trim(),
        };
        return result;
    }
}
