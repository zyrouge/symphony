import pico from "picocolors";
import { Versioner } from "../helpers/version";

const main = async () => {
    const pVersion = await Versioner.getVersion();
    const version = pVersion.bumpBuild();
    await Versioner.updateVersion(version);
    console.log(
        `Version bumped from ${pico.cyan(pVersion.toString())} to ${pico.cyan(version.toString())}!`,
    );
};

main();
