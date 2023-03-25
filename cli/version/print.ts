import { Versioner } from "../helpers/version";

const main = async () => {
    const { versionName } = await Versioner.getVersion();
    console.log(versionName);
};

main();
