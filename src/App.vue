<script setup lang="ts">
import GradientLink from "./components/GradientLink.vue";

const images = {
    icon: "https://github.com/zyrouge/symphony/blob/main/media/icon-circle.svg?raw=true",
    screenshots:
        "https://github.com/zyrouge/symphony/blob/main/media/screenshots.png?raw=true",
};

const links = {
    releases: "https://github.com/zyrouge/symphony/releases/latest",
    izzyOnDroid:
        "https://apt.izzysoft.de/fdroid/index/apk/io.github.zyrouge.symphony",
};
</script>

<template>
    <nav>
        <div class="flex justify-center items-center py-4">
            <div class="flex gap-4 items-center">
                <img class="w-10 h-10" :src="images.icon" />
                <p class="font-bold text-xl">Symphony</p>
            </div>
        </div>
        <hr class="border-dark-500" />
    </nav>

    <div>
        <div class="px-10 pt-14 pb-12">
            <p class="font-black text-3xl text-center leading-tight">
                Offline playback is now a breeze.
            </p>
            <div class="pt-12 text-center">
                <div>
                    <GradientLink :href="links.releases">
                        Download from <b>GitHub</b>
                    </GradientLink>
                </div>
                <div class="pt-3">
                    <GradientLink :href="links.izzyOnDroid">
                        Download from <b>IzzyOnDroid</b>
                    </GradientLink>
                </div>
            </div>
            <img class="pt-16 w-full" :src="images.screenshots" />
        </div>
    </div>
</template>
