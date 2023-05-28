<script setup lang="ts">
import HeroLink from "./components/HeroLink.vue";

const images = {
    icon: "https://raw.githubusercontent.com/zyrouge/symphony/main/media/icon.svg",
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
    <nav>
        <div class="flex justify-center items-center py-4">
            <div class="flex gap-4 items-center">
                <img class="w-8 h-8" :src="images.icon" />
                <p class="font-bold text-xl">Symphony</p>
            </div>
        </div>
        <hr class="border-dark-500" />
    </nav>

    <div>
        <div class="px-10 pt-14 pb-12">
            <div class="relative">
                <div
                    :class="[
                        'absolute inset-0 -z-1 flex justify-center',
                        'filter blur-md opacity-20',
                    ]"
                >
                    <div
                        :class="[
                            'mt-2 sm:mt-0 w-full max-w-92 h-1/4 rounded-1/2',
                            'bg-violet-400',
                        ]"
                    ></div>
                </div>
                <div>
                    <p class="font-black text-3xl text-center leading-tight">
                        Offline playback is now a breeze.
                    </p>
                    <div class="pt-12 text-center">
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
                </div>
            </div>
            <img class="pt-16 w-full" :src="images.screenshots" />
        </div>
    </div>
</template>
