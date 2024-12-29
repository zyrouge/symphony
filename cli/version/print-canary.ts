import { Git } from "../helpers/git";
import { Versioner } from "../helpers/version";

const main = async () => {
    const pVersion = await Versioner.getVersion();
    const sha = await Git.getLatestRevisionShort();
    const time = await Git.getRevisionDate(sha);
    const version = pVersion.copyWith({
        year: time.getFullYear(),
        month: time.getMonth() + 1,
        code: pVersion.code + 1,
        prerelease: "nightly",
        build: sha,
    });
    console.log(version.toString());
};

main();
