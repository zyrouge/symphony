import path from "path";
import fs from "fs-extra";
import archiver from "archiver";
import { Paths } from "../helpers/paths";

const APP_BUILD_TYPE = process.env.APP_BUILD_TYPE ?? "release";
const APP_VERSION_NAME = process.env.APP_VERSION_NAME;

const main = async () => {
    if (typeof APP_VERSION_NAME !== "string") {
        throw new Error("Missing environment variable: APP_VERSION_NAME");
    }
    await fs.ensureDir(Paths.distDir);
    await move(
        path.join(
            Paths.appDir,
            `build/outputs/apk/${APP_BUILD_TYPE}/app-${APP_BUILD_TYPE}.apk`,
        ),
        path.join(Paths.distDir, `symphony-v${APP_VERSION_NAME}.apk`),
    );
    await move(
        path.join(
            Paths.appDir,
            `build/outputs/bundle/${APP_BUILD_TYPE}/app-${APP_BUILD_TYPE}.aab`,
        ),
        path.join(Paths.distDir, `symphony-v${APP_VERSION_NAME}.aab`),
    );
    await moveZipped(
        path.join(
            Paths.appDir,
            `build/outputs/mapping/${APP_BUILD_TYPE}/mapping.txt`,
        ),
        path.join(Paths.distDir, "mapping.zip"),
    );
    await move(
        path.join(
            Paths.appDir,
            `build/outputs/native-debug-symbols/${APP_BUILD_TYPE}/native-debug-symbols.zip`,
        ),
        path.join(Paths.distDir, "native-debug-symbols.zip"),
        true,
    );
};

main();

async function move(from: string, to: string, skippable: boolean = false) {
    if (skippable && !(await fs.exists(from))) {
        return;
    }
    await fs.move(from, to);
    console.log(`Moved "${from}" to "${to}".`);
}

async function moveZipped(from: string, to: string) {
    await fs.ensureFile(to);
    const archive = archiver.create("zip");
    archive.pipe(fs.createWriteStream(to));
    archive.file(from, {
        name: path.basename(from),
    });
    await archive.finalize();
    console.log(`Zipped "${from}" to "${to}".`);
}
