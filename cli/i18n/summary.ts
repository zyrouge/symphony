import path from "path";
import fs from "fs/promises";
import { PhraseySummaryJson } from "phrasey";
import { Paths } from "../helpers/paths";

const outputDir = path.join(Paths.rootDir, "phrasey-dist");
const summaryJsonPath = path.join(outputDir, "summary.json");

const start = async () => {
    const summaryContent = await fs.readFile(summaryJsonPath, "utf-8");
    const summary: PhraseySummaryJson = JSON.parse(summaryContent);
    await fs.writeFile(
        path.join(outputDir, `README.md`),
        `
# Symphony i18n

> Last updated at ${new Date().toLocaleString()}

Read [Translations Guide](https://github.com/zyrouge/symphony/wiki/Translations-Guide) on how Symphony handles localization.

| Status | Locale | % Translated |
| --- | --- | --- |
${Object.entries(summary.individual)
    .map(([locale, x]) => {
        const status = x.set.percent === 100 ? "✅" : "⚠️";
        const url = `https://github.com/zyrouge/symphony/blob/main/i18n/${locale}.toml`;
        const percentage = `${x.set.percent.toFixed(1)}%`;
        return `| ${status} | [\`${locale}\`](${url}) | ${percentage} |`;
    })
    .join("\n")}
        `.trim()
    );
    const translationPercent = Math.floor(
        (summary.total.set / summary.total.total) * 100
    );
    await fs.writeFile(
        path.join(outputDir, `badge-translated.json`),
        JSON.stringify({
            schemaVersion: 1,
            label: "i18n",
            message: `${translationPercent}%`,
            color: "#328fa8",
        })
    );
    const languagesCount = Object.keys(summary.individual).length;
    await fs.writeFile(
        path.join(outputDir, `badge-languages.json`),
        JSON.stringify({
            schemaVersion: 1,
            label: "i18n languages",
            message: `${languagesCount}`,
            color: "#3279a8",
        })
    );
    const keysCount = summary.total.total / languagesCount;
    await fs.writeFile(
        path.join(outputDir, `badge-strings.json`),
        JSON.stringify({
            schemaVersion: 1,
            label: "i18n strings",
            message: `${keysCount}`,
            color: "#3265a8",
        })
    );
};

start();
