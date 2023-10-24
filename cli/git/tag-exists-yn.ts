import { Git } from "../helpers/git";

const main = async () => {
    const [tag] = process.argv.slice(2);
    if (!tag) throw new Error("Missing argument: tag");
    const exists = await Git.tagExists(tag);
    console.log(exists ? "yes" : "no");
};

main();
