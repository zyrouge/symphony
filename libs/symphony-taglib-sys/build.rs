use std::{env, path::PathBuf};

fn main() {
    let manifest_dir = PathBuf::from(env::var_os("CARGO_MANIFEST_DIR").unwrap());
    let out_dir = PathBuf::from(env::var_os("OUT_DIR").unwrap());
    println!("cargo::rerun-if-changed=CMakeLists.txt");
    println!("cargo::rerun-if-changed=cpp");
    println!("cargo::rerun-if-changed=vendor/taglib");
    println!("cargo::rerun-if-changed=vendor/utfcpp");
    println!("cargo::rerun-if-changed=vendor/zlib");
    let dst = cmake::Config::new(manifest_dir).build();
    println!(
        "cargo::rustc-link-search=native={}",
        dst.join("lib").display()
    );
    println!("cargo::rustc-link-lib=dylib=tag_c");
    println!("cargo::rustc-link-lib=dylib=tag");
    match env::var("CARGO_CFG_TARGET_OS").unwrap_or_default().as_str() {
        "linux" => {
            println!("cargo::rustc-link-lib=dylib=stdc++");
            println!("cargo::rustc-link-lib=dylib=z");
        }
        "android" => {
            println!("cargo::rustc-link-lib=dylib=c++");
            println!("cargo::rustc-link-lib=dylib=z");
        }
        _ => {}
    }
    println!("cargo::warning=CMake output: {}", dst.display());
    let bindings = bindgen::Builder::default()
        .header("vendor/taglib/bindings/c/tag_c.h")
        .header("cpp/include/taglib_fd_iostream.h")
        .allowlist_type("TagLib_.*")
        .allowlist_function("taglib_.*")
        .allowlist_var("TAGLIB_.*")
        .generate()
        .expect("Unable to generate bindings");
    bindings
        .write_to_file(out_dir.join("bindings.rs"))
        .expect("Couldn't write bindings!");
}
