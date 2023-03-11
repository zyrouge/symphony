import path from "path";

export class Paths {
    static rootDir = path.resolve(__dirname, "../../");
    static appDir = path.join(Paths.rootDir, "app");
}
