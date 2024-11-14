import { Git } from "../helpers/git";

const main = async () => {
    const [branch, ...pathPrefix] = process.argv.slice(2);
    if (!branch) throw new Error("Missing argument: tag");
    if (!pathPrefix.length) throw new Error("Missing argument: pathPrefix");
    const files = await Git.diffNames(branch);
    const affected = files.some((x) => affectsPathPrefix(pathPrefix, x));
    console.log(affected ? "yes" : "no");
};

main();

function affectsPathPrefix(pathPrefix: string[], file: string) {
    return pathPrefix.some((x) => file.startsWith(x));
}
