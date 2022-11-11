const versioner = require("./helpers/version");

const main = async () => {
    const currentVersion = await versioner.getVersion();
    const versionCode = currentVersion.versionCode + 1;
    const date = new Date();
    const versionName = `${date.getFullYear()}.${date.getMonth() + 1}.${versionCode}`;
    await versioner.updateVersion({ versionCode, versionName });
    console.log(`Version bumped from ${currentVersion.versionName} to ${versionName}!`);
}

main();
