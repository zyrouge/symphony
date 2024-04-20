import path from "path";
import pico from "picocolors";
import fs from "fs-extra";
import { PhraseyBuilder, PhraseyLogger } from "phrasey";
import { Paths } from "../helpers/paths";

const phraseyConfig = path.join(Paths.rootDir, ".phrasey/config.toml");
const outputDir = path.join(Paths.rootDir, "phrasey-dist");

const start = async () => {
    const summaryResult = await PhraseyBuilder.summary({
        phrasey: {
            cwd: path.dirname(phraseyConfig),
            log: PhraseyLogger.console(),
        },
        builder: {
            config: {
                file: phraseyConfig,
                format: path.extname(phraseyConfig).slice(1),
            },
        },
    });
    if (!summaryResult.success) {
        const errors = Array.isArray(summaryResult.error)
            ? summaryResult.error
            : [summaryResult.error];
        errors.forEach((x) => console.log(x));
        throw new Error("Phrasey summary failed due to errors");
    }
    const summary = summaryResult.data.json();
    await fs.mkdir(outputDir, { recursive: true });
    const mdPath = path.join(outputDir, `README.md`);
    await fs.writeFile(
        mdPath,
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
        `.trim(),
    );
    printGenerated(mdPath);

    const translationPercent = Math.floor(
        (summary.full.setCount / summary.full.total) * 100,
    );
    const badgeTranslatedPath = path.join(outputDir, `badge-translated.json`);
    await fs.writeFile(
        badgeTranslatedPath,
        JSON.stringify({
            schemaVersion: 1,
            label: "i18n",
            message: `${translationPercent}%`,
            color: "#328fa8",
        }),
    );
    printGenerated(badgeTranslatedPath);

    const languagesCount = Object.keys(summary.individual).length;
    const badgeLanguagesPath = path.join(outputDir, `badge-languages.json`);
    await fs.writeFile(
        badgeLanguagesPath,
        JSON.stringify({
            schemaVersion: 1,
            label: "i18n languages",
            message: `${languagesCount}`,
            color: "#3279a8",
        }),
    );
    printGenerated(mdPath);

    const keysCount = summary.full.keysCount;
    const badgeStringsPath = path.join(outputDir, `badge-strings.json`);
    await fs.writeFile(
        badgeStringsPath,
        JSON.stringify({
            schemaVersion: 1,
            label: "i18n strings",
            message: `${keysCount}`,
            color: "#3265a8",
        }),
    );
    printGenerated(mdPath);
};

start();

function printGenerated(value: string) {
    console.log(`Generated ${pico.green(value)}.`);
}
