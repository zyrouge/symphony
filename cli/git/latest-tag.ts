import { Git } from "../helpers/git";

const main = async () => {
    const tag = await Git.latestTag();
    console.log(tag);
};

main();
