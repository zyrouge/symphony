import path from "path";
import fs from "fs-extra";
import archiver from "archiver";
import { Paths } from "../helpers/paths";

const main = async () => {
    const mappingTxt = path.join(
        Paths.appDir,
        "build/outputs/mapping/release/mapping.txt",
    );
    const symbolsZip = path.join(Paths.rootDir, "dist/symbols.zip");
    await fs.ensureFile(symbolsZip);
    const archive = archiver.create("zip");
    archive.pipe(fs.createWriteStream(symbolsZip));
    archive.file(mappingTxt, {
        name: path.basename(mappingTxt),
    });
    await archive.finalize();
    console.log(`Generated "${symbolsZip}".`);
};

main();
