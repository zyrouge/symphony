const p = require("path");
const fs = require("fs-extra");

/**
 * @type {import("phrasey").PhraseyHooksHandler}
 */
const hook = {
    afterBuild: async ({ phrasey, log }) => {
        if (phrasey.options.source !== "build") {
            log.info("Skipping post-build due to non-build source");
            return;
        }
        await createTranslationKt(phrasey, log);
        await createTranslationsKt(phrasey, log);
    },
};

module.exports = hook;

const rootDir = p.resolve(__dirname, "..");
const appI18nDir = p.join(
    rootDir,
    "app/src/main/java/io/github/zyrouge/symphony/services/i18n"
);

/**
 *
 * @param {import("phrasey").Phrasey} phrasey
 * @param {import("phrasey").PhraseyLogger} log
 */
async function createTranslationsKt(phrasey, log) {
    const translations = [...phrasey.translations.values()];
    const content = `
package io.github.zyrouge.symphony.services.i18n

@Suppress("ClassName")
open class _Translations {
    val localeCodes: List<String> = listOf(
${translations.map((x) => `        "${x.locale.code}",`).join("\n")}
    )
    val localeNames: Map<String, String> = mapOf(
${translations
    .map((x) => `        "${x.locale.code}" to "${x.locale.name}",`)
    .join("\n")}
    )
}
    `;
    const path = p.join(appI18nDir, "Translations.g.kt");
    await fs.writeFile(path, content);
    log.success(`Generated "${p.relative(rootDir, path)}".`);
}

/**
 *
 * @param {import("phrasey").Phrasey} phrasey
 * @param {import("phrasey").PhraseyLogger} log
 */
async function createTranslationKt(phrasey, log) {
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
                `    fun ${x.name}(${params}): String = _keysJson.getString("${x.name}").format(${callArgs})`
            );
        } else {
            staticKeys.push(
                `    val ${x.name}: String get() = _keysJson.getString("${x.name}")`
            );
        }
    }

    const content = `
package io.github.zyrouge.symphony.services.i18n

import org.json.JSONObject

@Suppress("ClassName")
open class _Translation(val json: JSONObject) {
    private val _localeJson = json.getJSONObject("locale")
    private val _keysJson = json.getJSONObject("keys")

    val LocaleName: String get() = _localeJson.getString("name")
    val LocaleCode: String get() = _localeJson.getString("locale")

${staticKeys.join("\n")}

${dynamicKeys.join("\n")}
}
    `;
    const path = p.join(appI18nDir, "Translation.g.kt");
    await fs.writeFile(path, content);
    log.success(`Generated "${p.relative(rootDir, path)}".`);
}
