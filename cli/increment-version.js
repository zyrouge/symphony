const versioner = require("./helpers/version");

const main = async () => {
    const args = process.argv.slice(2);
    if (!args.includes("-y")) throw new Error("Missing flag '-y'");
    const currentVersion = await versioner.getVersion();
    const versionCode = currentVersion.versionCode + 1;
    const date = new Date();
    const versionName = [
        date.getFullYear(),
        date.getMonth(),
        date.getDate(),
        versionCode
    ].map(x => x.toString()).join(".");
    return versioner.updateVersion({ versionCode, versionName });
}

main();
