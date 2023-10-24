import { Git } from "../helpers/git";

const main = async () => {
    const sha = await Git.getLatestRevisionShort();
    const time = await Git.getRevisionDate(sha);
    const year = time.getFullYear();
    const month = time.getMonth() + 1;
    const date = time.getDate();
    const versionName = `${year}.${month}.${date}-${sha}`;
    console.log(versionName);
};

main();
