<script setup lang="ts">
interface Props {
    href: string;
}

const { href } = defineProps<Props>();
</script>

<template>
    <a
        :class="[
            'inline-block px-4 py-2 rounded-lg text-sm',
            'bg-gradient-to-br from-violet-500 via-violet-600 to-violet-600',
            'transition transform hover:-translate-y-0.75',
        ]"
        :href="href"
    >
        <slot />
    </a>
</template>
