import p from "path";
import fs from "fs-extra";
import { Paths } from "../helpers/paths";

const CHANGELOGS_CHARACTER_LIMIT = 540;

const start = async () => {
    let failed = 0;
    const dirnames = await fs.readdir(Paths.metadataDir);
    for (const x of dirnames) {
        const dir = p.join(Paths.metadataDir, x, "changelogs");
        for (const name of fs.readdirSync(dir)) {
            const path = p.join(dir, name);
            const content = await fs.readFile(path, "utf-8");
            const length = content.length;
            const rpath = p.relative(Paths.metadataDir, path);
            if (length > CHANGELOGS_CHARACTER_LIMIT) {
                failed++;
                const excess = length - CHANGELOGS_CHARACTER_LIMIT;
                console.log(
                    `${rpath} has ${length} characters (excess by ${excess} characters)`,
                );
            }
        }
    }
    process.exit(failed === 0 ? 0 : 1);
};

start();
