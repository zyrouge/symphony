import { Git } from "../helpers/git";
import { Versioner } from "../helpers/version";

const main = async () => {
    const { versionCode } = await Versioner.getVersion();
    const sha = await Git.getLatestRevisionShort();
    const time = await Git.getRevisionDate(sha);
    const year = time.getFullYear();
    const month = time.getMonth() + 1;
    const versionName = `${year}.${month}.${versionCode + 1}-nightly+${sha}`;
    console.log(versionName);
};

main();
