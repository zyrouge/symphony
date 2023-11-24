const p = require("path");
const fs = require("fs-extra");
const { rootDir, appI18nDir } = require("./utils");

/**
 * @type {import("phrasey").PhraseyHooksHandler}
 */
const hook = {
    onTranslationsBuildFinished: async ({ phrasey, state, log }) => {
        if (phrasey.options.source !== "build") {
            log.info("Skipping post-build due to non-build source");
            return;
        }
        await createTranslationKt(phrasey, state, log);
        await createTranslationsKt(phrasey, state, log);
    },
};

module.exports = hook;

/**
 *
 * @param {import("phrasey").Phrasey} phrasey
 * @param {import("phrasey").PhraseyState} state
 * @param {import("phrasey").PhraseyLogger} log
 */
async function createTranslationsKt(phrasey, state, log) {
    const translations = [...state.getTranslations().values()];
    const sortedTranslations = translations.sort((a, b) =>
        a.locale.display.localeCompare(b.locale.display),
    );
    const content = `
package io.github.zyrouge.symphony.services.i18n

@Suppress("ClassName")
open class _Translations {
    val localeCodes: List<String> = listOf(
${sortedTranslations.map((x) => `        "${x.locale.code}",`).join("\n")}
    )
    val localeDisplayNames: Map<String, String> = mapOf(
${sortedTranslations
    .map((x) => `        "${x.locale.code}" to "${x.locale.display}",`)
    .join("\n")}
    )
    val localeNativeNames: Map<String, String> = mapOf(
${sortedTranslations
    .map((x) => `        "${x.locale.code}" to "${x.locale.native}",`)
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
 * @param {import("phrasey").PhraseyState} state
 * @param {import("phrasey").PhraseyLogger} log
 */
async function createTranslationKt(phrasey, state, log) {
    /**
     * @type {string[]}
     */
    const staticKeys = [];
    /**
     * @type {string[]}
     */
    const dynamicKeys = [];

    for (const x of state.getSchema().z.keys) {
        if (x.parameters && x.parameters.length > 0) {
            const params = x.parameters.map((x) => `${x}: String`).join(", ");
            const callArgs = x.parameters.join(", ");
            dynamicKeys.push(
                `    fun ${x.name}(${params}): String = _keysJson.getString("${x.name}").format(${callArgs})`,
            );
        } else {
            staticKeys.push(
                `    val ${x.name}: String get() = _keysJson.getString("${x.name}")`,
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

    val LocaleDisplayName: String get() = _localeJson.getString("display")
    val LocaleNativeName: String get() = _localeJson.getString("native")
    val LocaleCode: String get() = _localeJson.getString("code")
    val LocaleDirection: String get() = _localeJson.getString("direction")

${staticKeys.join("\n")}

${dynamicKeys.join("\n")}
}
    `;
    const path = p.join(appI18nDir, "Translation.g.kt");
    await fs.writeFile(path, content);
    log.success(`Generated "${p.relative(rootDir, path)}".`);
}
