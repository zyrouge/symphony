import path from "path";
import { promises as fs } from "fs";
import { Paths } from "./paths";

export interface VersionData {
    versionCode: number;
    versionName: string;
}

export class Versioner {
    static appBuildGradlePath = path.join(Paths.appDir, "build.gradle.kts");
    static versionCodeRegex = RegExp(/versionCode = (\d+)/);
    static versionNameRegex = RegExp(/versionName = "(\d{4}\.\d{1,2}\.\d+)"/);

    static async getVersion() {
        const content = (await fs.readFile(this.appBuildGradlePath)).toString();
        const versionCode = this.versionCodeRegex.exec(content)?.[1];
        const versionName = this.versionNameRegex.exec(content)?.[1];
        if (!versionCode) throw new Error("Unable to parse version code");
        if (!versionName) throw new Error("Unable to parse version name");
        return {
            versionCode: parseInt(versionCode),
            versionName: versionName,
        };
    }

    static async updateVersion({ versionCode, versionName }: VersionData) {
        let content = (await fs.readFile(this.appBuildGradlePath)).toString();
        content = content.replace(
            this.versionCodeRegex,
            `versionCode = ${versionCode}`
        );
        content = content.replace(
            this.versionNameRegex,
            `versionName = "${versionName}"`
        );
        await fs.writeFile(this.appBuildGradlePath, content);
    }
}
