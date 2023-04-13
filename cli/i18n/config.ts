import fs from "fs-extra";
import path from "path";
import { definePhraseyConfig } from "phrasey";
import { Paths } from "../helpers/paths";
import TranslationKeys from "./keys.json";

export const keys = TranslationKeys;

const translationDir = path.join(
    Paths.appDir,
    "src/main/java/io/github/zyrouge/symphony/services/i18n/translations"
);

export const config = definePhraseyConfig({
    input: {
        include: [path.join(Paths.rootDir, "i18n/**.yaml")],
    },
    defaultLocale: "en",
    keys: [...keys.static, ...Object.keys(keys.dynamic)] as const,
    transpile: {
        beforeOutput: async () => {
            const modelFilePath = path.join(translationDir, "Model.g.kt");

            await fs.writeFile(
                modelFilePath,
                `
package io.github.zyrouge.symphony.services.i18n.translations

interface ITranslations {
    val Language: String
    val Locale: String

${keys.static.map((x) => `    val ${x}: String`).join("\n")}

${Object.entries(keys.dynamic)
    .map(
        ([x, args]) =>
            `    fun ${x}(${args
                .map((x) => `${x}: String`)
                .join(", ")}): String`
    )
    .join("\n")}
}
                `.trim()
            );
        },
        output: async (translation) => {
            const t = translation.translations;
            const localeCapitalized = capitalize(translation.locale);

            return {
                path: path.join(translationDir, `${localeCapitalized}.g.kt`),
                content: `
package io.github.zyrouge.symphony.services.i18n.translations

class ${localeCapitalized}Translation : ITranslations {
    override val Language = "${escapeText(translation.language)}"
    override val Locale = "${escapeText(translation.locale)}"

${keys.static
    .map((x) => `    override val ${x} = "${escapeText(t[x]!)}"`)
    .join("\n")}

${Object.entries(keys.dynamic)
    .map(
        ([x, args]) =>
            `    override fun ${x}(${args
                .map((x) => `${x}: String`)
                .join(", ")}) = "${escapeText(t[x]!)}"`
    )
    .join("\n")}
}
                        `.trim(),
            };
        },
    },
});

function escapeText(text: String) {
    return text.trim().replace(/"/g, '\\"').replace(/\n/g, "\\n");
}

function capitalize(text: String) {
    return text
        .replace(/^\w/, (match) => match[0]!.toUpperCase())
        .replace(/-(\w)/, (match) => match[1]!.toUpperCase())
        .trim();
}
