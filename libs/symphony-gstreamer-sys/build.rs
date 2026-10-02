use std::env;
use std::path::PathBuf;
use std::process::{Command, exit};

fn main() {
    let manifest_dir = PathBuf::from(env::var_os("CARGO_MANIFEST_DIR").unwrap());
    let gstreamer_dir = manifest_dir.join("vendor").join("gstreamer");
    let gstreamer_build_dir = gstreamer_dir.join("build");
    println!("cargo::rerun-if-changed=vendor/gstreamer");
    if !is_meson_build_configured(&gstreamer_build_dir) {
        let command_result = meson_setup(&gstreamer_dir, &gstreamer_build_dir);
        if command_result.is_err() {
            println!("cargo::error=failed to configure GStreamer Meson");
            exit(1);
        }
        let status = command_result.unwrap();
        if !status.success() {
            println!("cargo::error=configure GStreamer Meson returned error status {status}");
            exit(1);
        }
    }
    let command_result = meson_compile(&gstreamer_dir, &gstreamer_build_dir);
    if command_result.is_err() {
        println!("cargo::error=failed to compile GStreamer Meson build");
        exit(1);
    }
    let status = command_result.unwrap();
    if !status.success() {
        println!("cargo::error=compile GStreamer Meson returned error status {status}");
        exit(1);
    }
}

fn is_meson_build_configured(build_dir: &PathBuf) -> bool {
    build_dir.join("build.ninja").exists()
}

fn meson_setup(
    source_dir: &PathBuf,
    build_dir: &PathBuf,
) -> Result<std::process::ExitStatus, std::io::Error> {
    Command::new("meson")
        .arg("setup")
        .arg(build_dir)
        .current_dir(source_dir)
        .env_remove("RUSTC")
        .env_remove("MSYSTEM")
        .status()
}

fn meson_compile(
    source_dir: &PathBuf,
    build_dir: &PathBuf,
) -> Result<std::process::ExitStatus, std::io::Error> {
    Command::new("meson")
        .arg("compile")
        .arg("-C")
        .arg(build_dir)
        .current_dir(source_dir)
        .status()
}
