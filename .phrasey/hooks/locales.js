const p = require("path");
const fs = require("fs-extra");
const { PhraseyLocaleBuilder } = require("@zyrouge/phrasey-locales-builder");
const { rootDir, rootI18nDir } = require("./utils");

/**
 * @type {import("phrasey").PhraseyHooksHandler}
 */
const hook = {
    beforeLocalesParsing: async ({ phrasey, log }) => {
        await createLocalesJson(phrasey, log);
    },
};

module.exports = hook;

/**
 *
 * @param {import("phrasey").Phrasey} phrasey
 * @param {import("phrasey").PhraseyLogger} log
 */
async function createLocalesJson(phrasey, log) {
    const path = p.join(rootI18nDir, "locales.g.json");
    if (await fs.exists(path)) {
        log.info(`Skipping generation of locales as it already exists.`);
        return;
    }
    const additionalLocalesPath = p.resolve(
        __dirname,
        `../additional-locales.json`,
    );
    /**
     * @type {import("phrasey").PhraseyLocaleType[]}
     */
    const defaultLocales = await PhraseyLocaleBuilder.build({
        displayLocaleCode: "en",
    });
    /**
     * @type {import("phrasey").PhraseyLocaleType[]}
     */
    const additionalLocales = await fs.readJSON(additionalLocalesPath);
    const locales = [...defaultLocales, ...additionalLocales];
    await fs.writeFile(path, JSON.stringify(locales));
    log.success(`Generated "${p.relative(rootDir, path)}".`);
}
