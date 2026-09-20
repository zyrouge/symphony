use std::{env, path::PathBuf};

fn main() {
    let manifest_dir = PathBuf::from(env::var_os("CARGO_MANIFEST_DIR").unwrap());
    let taglib_dir = manifest_dir.join("vendor/taglib");
    println!("cargo:rerun-if-changed={}", taglib_dir.display());
    let dst = cmake::Config::new(&taglib_dir)
        .define("BUILD_SHARED_LIBS", "OFF")
        .define("BUILD_TESTING", "OFF")
        .define("BUILD_EXAMPLES", "OFF")
        .define("BUILD_BINDINGS", "ON")
        .define("CMAKE_BUILD_TYPE", "Release")
        .build();
    println!(
        "cargo:rustc-link-search=native={}",
        dst.join("lib").display()
    );
    println!("cargo:rustc-link-lib=static=tag");
}
