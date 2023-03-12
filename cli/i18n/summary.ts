import path from "path";
import fs from "fs/promises";
import { PhraseyCircuit } from "phrasey";
import { config } from "./config";
import { Paths } from "../helpers/paths";

const outputDir = path.join(Paths.rootDir, "build-data/i18n-summary");

const start = async () => {
    await fs.rm(outputDir, {
        recursive: true,
        force: true,
    });
    await fs.mkdir(outputDir, {
        recursive: true,
    });
    const circuit = PhraseyCircuit.create(config);
    const summary = await circuit.getFullSummary();
    await fs.writeFile(
        path.join(outputDir, `README.md`),
        `
# i18n

> Last updated at ${new Date().toLocaleString()}

| Status | Language | % Translated |
| --- | --- | --- |
${Object.values(summary.summary)
    .map(
        (x) =>
            `| ${x.keys.percents.set === 100 ? "✅" : "⚠️"} | ${
                x.translation.language
            } | ${x.keys.percents.set.toFixed(1)}% |`
    )
    .join("\n")}
        `.trim()
    );
    await fs.writeFile(
        path.join(outputDir, `badge-translated.json`),
        JSON.stringify({
            schemaVersion: 1,
            label: "Translated",
            message: `${Math.floor(summary.keys.percents.set)}%`,
            color: "#328fa8",
        })
    );
    await fs.writeFile(
        path.join(outputDir, `badge-languages.json`),
        JSON.stringify({
            schemaVersion: 1,
            label: "Languages",
            message: `${summary.keys.total}`,
            color: "#3279a8",
        })
    );
};

start();
