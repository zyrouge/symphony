<script setup lang="ts">
import Hero from "./components/Hero.vue";
import Features from "./components/Features.vue";
import Footer from "./components/Footer.vue";

const images = {
    screenshots:
        "https://raw.githubusercontent.com/zyrouge/symphony/main/media/screenshots.png",
};

const links = {
    releases: "https://github.com/zyrouge/symphony/releases/latest",
    izzyOnDroid:
        "https://apt.izzysoft.de/fdroid/index/apk/io.github.zyrouge.symphony",
};
</script>

<template>
    <Hero />
    <Features />
    <Footer />

    <!-- <div>
        <div class="px-10 pt-20 pb-16">
        <div class="pt-20">
            <p class="font-display text-3xl text-center leading-tight">
                Offline playback is now a breeze.
            </p>
        </div>
        <div class="pt-16 text-center">
            <div>
                <HeroLink :href="links.releases">
                    Download from <b>GitHub</b>
                </HeroLink>
            </div>
            <div class="pt-3">
                <HeroLink :href="links.izzyOnDroid">
                    Download from <b>IzzyOnDroid</b>
                </HeroLink>
            </div>
        </div>
        <div class="pt-20 px-16">
            <img class="w-full" :src="images.screenshots" />
        </div>
    </div> -->
    <!-- </div> -->
</template>
