import { Git } from "../helpers/git";

const main = async () => {
    const [tag] = process.argv.slice(2);
    if (!tag) throw new Error("Missing argument: tag");
    const files = await Git.diffNames(tag);
    const affected = files.some((x) => affectsApp(x));
    console.log(!affected ? "yes" : "no");
};

main();

const affectDirs = ["app/", "i18n/", "gradle/"];

function affectsApp(file: string) {
    return affectDirs.some((x) => file.startsWith(x));
}
