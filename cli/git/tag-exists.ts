import { Git } from "../helpers/git";

const main = async () => {
    const [tag] = process.argv.slice(2);
    if (!tag) throw new Error("Missing argument: tag");
    const exists = await Git.tagExists(tag);
    if (exists) throw new Error(`Tag ${tag} already exists`);
    console.log(`Tag ${tag} is available`);
};

main();
