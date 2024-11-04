import { Versioner } from "../helpers/version";

const main = async () => {
    const version = await Versioner.getVersion();
    console.log(version.toString());
};

main();
