package io.github.zyrouge.symphony

interface SymphonyHooks {
    fun onSymphonyReady() {}
    fun onSymphonyDestroy() {}
    fun onSymphonyActivityReady() {}
    fun onSymphonyActivityPause() {}
    fun onSymphonyActivityDestroy() {}
}