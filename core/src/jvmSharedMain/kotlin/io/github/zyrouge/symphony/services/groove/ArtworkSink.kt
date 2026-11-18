package io.github.zyrouge.symphony.services.groove

interface ArtworkSink {
    fun store(plan: CoverPlan, picture: EmbeddedPicture?): String?
}
