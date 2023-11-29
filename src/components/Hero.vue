<script setup lang="ts">
import Icon from "/icon.svg";

const GITHUB_URL = "https://github.com/zyrouge/symphony";
const GITHUB_RELEASES_URL = "https://github.com/zyrouge/symphony/releases";
const GITHUB_RELEASES_LATEST_URL =
    "https://github.com/zyrouge/symphony/releases/latest";
const GITHUB_README_DONATIONS_URL =
    "https://github.com/zyrouge/symphony#contributing";
const IZZY_ON_DROID_URL =
    "https://apt.izzysoft.de/fdroid/index/apk/io.github.zyrouge.symphony";
const FDROID_URL = "https://f-droid.org/en/packages/io.github.zyrouge.symphony";
const PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=io.github.zyrouge.symphony";
</script>

<template>
    <div class="u-flex bg-primary-600 pb-8 pt-24 md:pb-16 md:pt-32">
        <div class="u-container flex flex-col items-start justify-center">
            <div class="u-flex gap-8">
                <img
                    class="aspect-square w-10 md:w-14"
                    :src="Icon"
                    alt="Logo"
                />
            </div>
            <p
                class="mt-6 text-2xl leading-relaxed tracking-normal md:text-3xl"
            >
                <span class="hero-slant">Symphony</span>, a
                <span class="hero-slant">lightweight</span>,
                <span class="hero-slant">elegant</span>
                music player to enhance your
                <span class="hero-slant">offline music experience</span>.
            </p>
            <div class="u-flex mt-12 flex-wrap !justify-start gap-2">
                <a :href="GITHUB_URL" class="hero-btn">Source Code</a>
                <a :href="GITHUB_RELEASES_LATEST_URL" class="hero-btn"
                    >Download</a
                >
            </div>
            <p class="mt-4">
                Get it from
                <a :href="GITHUB_RELEASES_URL" class="hero-link"
                    >GitHub Releases</a
                >, <a :href="FDROID_URL" class="hero-link">F-Droid</a>,
                <a :href="IZZY_ON_DROID_URL" class="hero-link">IzzyOnDroid</a>
                or
                <a :href="PLAY_STORE_URL" class="hero-link">Play Store</a>.
            </p>
        </div>
    </div>
    <div class="u-flex bg-secondary-800 py-4">
        <div class="u-container u-flex w-max gap-4">
            <p class="text-2xl">❤️</p>
            <p>
                Consider
                <a :href="GITHUB_README_DONATIONS_URL" class="hero-link"
                    >contributing or donating</a
                >
                to support the development.
            </p>
        </div>
    </div>
</template>

<style scoped>
.hero-slant {
    @apply font-display;
}

.hero-btn {
    @apply rounded-lg bg-primary-700 px-4 py-2 font-bold transition-colors duration-150 hover:bg-primary-800;
}

.hero-link {
    @apply border-b border-primary-500 font-bold hover:border-primary-400;
}
</style>
