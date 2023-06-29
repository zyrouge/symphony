const p = require("path");
const fs = require("fs-extra");

/**
 * @type {import("phrasey").PhraseyHooksPartialHandler}
 */
const hook = {
    afterBuild: async (phrasey) => {
        if (phrasey.additional.source !== "build") return;
        await createTranslationKt(phrasey);
        await createTranslationsKt(phrasey);
    },
};

module.exports = hook;

const i18nDir = p.join(
    __dirname,
    "../app/src/main/java/io/github/zyrouge/symphony/services/i18n"
);

/**
 *
 * @param {import("phrasey").Phrasey} phrasey
 */
async function createTranslationsKt(phrasey) {
    const translations = [...phrasey.translations.values()];
    const content = `
package io.github.zyrouge.symphony.services.i18n

open class TranslationsBase {
    val localeCodes = listOf(
${translations.map((x) => `        "${x.locale.code}",`).join("\n")}    
    )
    val localeNames = mapOf(
${translations
    .map((x) => `        "${x.locale.code}" to "${x.locale.name}",`)
    .join("\n")}
    )
}
    `;
    await fs.writeFile(p.join(i18nDir, "Translations.g.kt"), content);
}

/**
 *
 * @param {import("phrasey").Phrasey} phrasey
 */
async function createTranslationKt(phrasey) {
    /**
     * @type {string[]}
     */
    const staticKeys = [];
    /**
     * @type {string[]}
     */
    const dynamicKeys = [];

    for (const x of phrasey.schema.z.keys) {
        if (x.parameters && x.parameters.length > 0) {
            const params = x.parameters.map((x) => `${x}: String`).join(", ");
            const callArgs = x.parameters.join(", ");
            dynamicKeys.push(
                `    fun ${x.name}(${params}): String = json.getString("${x.name}").format(${callArgs})`
            );
        } else {
            staticKeys.push(
                `    val ${x.name}: String get() = json.getString("${x.name}")`
            );
        }
    }

    const content = `
package io.github.zyrouge.symphony.services.i18n

import org.json.JSONObject

class TranslationBase(val json: JSONObject) {
    private val _localeJson: JSONObject get() = json.getJSONObject("locale")
    val LocaleName: String get() = _localeJson.getString("name")
    val LocaleCode: String get() = _localeJson.getString("locale")

${staticKeys.join("\n")}

${dynamicKeys.join("\n")}
}
    `;
    await fs.writeFile(p.join(i18nDir, "Translation.g.kt"), content);
}
