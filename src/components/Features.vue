<script setup lang="ts">
import FeatureCard from "./FeatureCard.vue";
</script>

<template>
    <div class="u-flex my-8 md:my-16">
        <div class="u-container grid w-full gap-4 md:grid-cols-2">
            <FeatureCard
                icon="✨"
                border-color="border-yellow-600/10"
                background-color="bg-yellow-600/5"
            >
                <template v-slot:title>
                    <p>Beautiful Interface.</p>
                </template>
                <template v-slot:text>
                    <p>
                        Includes light, dark and black theme along with Material
                        You.
                    </p>
                </template>
            </FeatureCard>

            <FeatureCard
                icon="📜"
                border-color="border-purple-500/10"
                background-color="bg-purple-500/5"
            >
                <template v-slot:title>
                    <p>Robust Queue System.</p>
                </template>
                <template v-slot:text>
                    <p>
                        Queue based music playback for easier management of
                        tracks.
                    </p>
                </template>
            </FeatureCard>

            <FeatureCard
                icon="🔒"
                border-color="border-stone-500/10"
                background-color="bg-stone-500/5"
            >
                <template v-slot:title>
                    <p>Offline, offline.</p>
                </template>
                <template v-slot:text>
                    <p>
                        Focused for mainly for offline usage. No internet
                        required.
                    </p>
                </template>
            </FeatureCard>

            <FeatureCard
                icon="🗃️"
                border-color="border-gray-100/10"
                background-color="bg-gray-100/5"
            >
                <template v-slot:title>
                    <p>Android MediaStore.</p>
                </template>
                <template v-slot:text>
                    <p>
                        Uses Android's built-in MediaStore. No more scanning
                        that takes forever.
                    </p>
                </template>
            </FeatureCard>

            <FeatureCard
                icon="🧮"
                border-color="border-sky-500/10"
                background-color="bg-sky-500/5"
            >
                <template v-slot:title>
                    <p>M3U Playlists.</p>
                </template>
                <template v-slot:text>
                    <p>
                        Supports importing M3U playlists. Automatically synced
                        when updated.
                    </p>
                </template>
            </FeatureCard>

            <FeatureCard
                icon="🔮"
                border-color="border-pink-500/10"
                background-color="bg-pink-500/5"
            >
                <template v-slot:title>
                    <p>And much more!</p>
                </template>
                <template v-slot:text>
                    <p>
                        Unique features such as tree view, folders view, pause
                        on song end, etc...
                    </p>
                </template>
            </FeatureCard>

            <FeatureCard
                icon="❤️"
                border-color="border-red-500/10"
                background-color="bg-red-500/5"
            >
                <template v-slot:title>
                    <p>Open-source.</p>
                </template>
                <template v-slot:text>
                    <p>Free as in freedom. Licensed under AGPL-3.0.</p>
                </template>
            </FeatureCard>

            <FeatureCard
                icon="💜"
                border-color="border-purple-500/10"
                background-color="bg-purple-500/5"
            >
                <template v-slot:title>
                    <p>Community focused.</p>
                </template>
                <template v-slot:text>
                    <p>Made for enthusiasts, by enthusiasts.</p>
                </template>
            </FeatureCard>
        </div>
    </div>
</template>
