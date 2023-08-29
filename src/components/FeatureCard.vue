<script setup lang="ts">
interface Props {
    icon: string;
    borderColor: string;
    backgroundColor: string;
}

const { icon } = defineProps<Props>();
</script>

<template>
    <div
        :class="['h-fit rounded-lg border py-4', borderColor, backgroundColor]"
    >
        <div
            :class="[
                'u-flex aspect-square w-16 rounded-r-lg border-b border-r border-t',
                borderColor,
                backgroundColor,
            ]"
        >
            <p class="text-3xl">
                {{ icon }}
            </p>
        </div>
        <div class="mt-4 px-4">
            <div class="font-display text-xl">
                <slot name="title" />
            </div>
            <div class="mt-2 text-sm text-secondary-400">
                <slot name="text" />
            </div>
        </div>
    </div>
</template>
