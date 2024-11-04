import { Git } from "../helpers/git";
import { Version, Versioner } from "../helpers/version";

const main = async () => {
    const pVersion = await Versioner.getVersion();
    const sha = await Git.getLatestRevisionShort();
    const time = await Git.getRevisionDate(sha);
    const version = new Version(
        time.getFullYear(),
        time.getMonth() + 1,
        pVersion.code + 1,
        "canary",
        sha,
    );
    console.log(version.toString());
};

main();
