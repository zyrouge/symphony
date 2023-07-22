const p = require("path");

const rootDir = p.resolve(__dirname, "../..");

const appI18nDir = p.join(
    rootDir,
    "app/src/main/java/io/github/zyrouge/symphony/services/i18n"
);

const rootI18nDir = p.join(rootDir, "i18n");

module.exports = {
    rootDir,
    appI18nDir,
    rootI18nDir,
};
