const path = require("path");
const fs = require("fs").promises;

module.exports = class {
    static appBuildGradlePath = path.join(__dirname, "../../app/build.gradle");
    static versionCodeRegex = RegExp(/versionCode (\d+)/);
    static versionNameRegex = RegExp(/versionName "(.+?)"/);
    
    static async getVersion() {
        const content = (await fs.readFile(this.appBuildGradlePath)).toString();
        return {
            versionCode: parseInt(this.versionCodeRegex.exec(content)[1]),
            versionName: this.versionNameRegex.exec(content)[1],
        }
    }

    static async updateVersion({ versionCode, versionName }) {
        let content = (await fs.readFile(this.appBuildGradlePath)).toString();
        content = content.replace(this.versionCodeRegex, `versionCode ${versionCode}`);
        content = content.replace(this.versionNameRegex, `versionName "${versionName}"`);
        await fs.writeFile(this.appBuildGradlePath, content);
    }
}
