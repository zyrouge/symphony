import path from "path";
import { promises as fs } from "fs";
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
        const version = Version.parse(versionName);
        if (version.code.toString() !== versionCode) {
            throw new Error("Mismatching version code and version name");
        }
        return version;
    }

    static async updateVersion(version: Version) {
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

export class Version {
    constructor(
        public readonly year: number,
        public readonly month: number,
        public readonly code: number,
        public readonly prerelease?: string,
        public readonly build?: string,
    ) {}

    bump() {
        const date = new Date();
        return new Version(
            date.getFullYear(),
            date.getMonth() + 1,
            this.code + 1,
            this.prerelease,
        );
    }

    bumpBuild() {
        if (
            typeof this.build === "string" &&
            !Version.numericRegex.test(this.build)
        ) {
            throw new Error("Build is not numeric");
        }
        return new Version(
            this.year,
            this.month,
            this.code,
            this.prerelease,
            (parseInt(this.build ?? "0") + 1).toString(),
        );
    }

    toString() {
        let version = `${this.year}.${this.month}.${this.code}`;
        if (this.prerelease) {
            version += `-${this.prerelease}`;
        }
        if (this.build) {
            version += `+${this.build}`;
        }
        return version;
    }

    static numericRegex = /^\d+$/;
    static versionRegex =
        /^(\d{4})\.(\d{1,2})\.(\d+)(?:\-([^+]+))?(?:\+(.+))?$/;

    static parse(value: string) {
        const match = value.match(this.versionRegex);
        if (match === null) {
            throw new Error("Unable to parse version");
        }
        const year = parseInt(match[1]!);
        const month = parseInt(match[2]!);
        const code = parseInt(match[3]!);
        const prerelease = match[4];
        const build = match[5];
        return new Version(year, month, code, prerelease, build);
    }
}
