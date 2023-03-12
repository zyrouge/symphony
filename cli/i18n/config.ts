import path from "path";
import { definePhraseyConfig } from "phrasey";
import { Paths } from "../helpers/paths";
import TranslationKeys from "./keys.json";

export const keys = TranslationKeys;

export const config = definePhraseyConfig({
    input: {
        include: [path.join(Paths.rootDir, "i18n/**.yaml")],
    },
    defaultLocale: "en",
    keys: [...keys.static, ...Object.keys(keys.dynamic)],
    transpile: {
        output: async (translation) => {
            const t = translation.translations;
            return {
                path: path.join(
                    Paths.appDir,
                    "src/main/java/io/github/zyrouge/symphony/services/i18n/translations",
                    `${translation.locale}.g.kt`
                ),
                content: `
package io.github.zyrouge.symphony.services.i18n.translations

import io.github.zyrouge.symphony.services.i18n.Translations

class English : Translations {
    override val language = "${escapeText(translation.language)}"
    override val locale = "${escapeText(translation.locale)}"

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
    return text.replace(/"/g, '\\"').replace(/\n/g, "\\n").trim();
}
