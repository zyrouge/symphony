import path from "path";
import { promises as fs } from "fs";
import { ZemVer } from "@zyrouge/zemver";
import { Paths } from "./paths";

export class Versioner {
    static appBuildGradlePath = path.join(Paths.appDir, "build.gradle.kts");
    static versionCodeRegex = /versionCode = (\d+)/;
    static versionNameRegex = /versionName = "([^"]+)"/;

    static async getVersion() {
        const content = (await fs.readFile(this.appBuildGradlePath)).toString();
        const versionCode = this.versionCodeRegex.exec(content)?.[1];
        const versionName = this.versionNameRegex.exec(content)?.[1];
        if (!versionCode) {
            throw new Error("Unable to parse version code");
        }
        if (!versionName) {
            throw new Error("Unable to parse version name");
        }
        const version = ZemVer.parse(versionName);
        if (version.code.toString() !== versionCode) {
            throw new Error("Mismatching version code and version name");
        }
        return version;
    }

    static async updateVersion(version: ZemVer) {
        let content = (await fs.readFile(this.appBuildGradlePath)).toString();
        content = content.replace(
            this.versionCodeRegex,
            `versionCode = ${version.code}`,
        );
        content = content.replace(
            this.versionNameRegex,
            `versionName = "${version}"`,
        );
        await fs.writeFile(this.appBuildGradlePath, content);
    }
}
