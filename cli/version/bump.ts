import pico from "picocolors";
import { Versioner } from "../helpers/version";

const main = async () => {
    const currentVersion = await Versioner.getVersion();
    const versionCode = currentVersion.versionCode + 1;
    const date = new Date();
    const versionName = [
        date.getFullYear(),
        date.getMonth() + 1,
        versionCode,
    ].join(".");
    await Versioner.updateVersion({ versionCode, versionName });
    console.log(
        `Version bumped from ${pico.cyan(
            currentVersion.versionName
        )} to ${pico.cyan(versionName)}!`
    );
};

main();
