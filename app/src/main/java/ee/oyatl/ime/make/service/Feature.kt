package ee.oyatl.ime.make.service

import ee.oyatl.ime.make.BuildConfig

enum class Feature {
    ComponentEditHint,
    ComponentLanguageTabBar,
    ;

    val enabled: Boolean get() = this in ENABLED_CURRENT

    companion object {
        private const val DEBUG = "debug"
        private const val RELEASE = "release"

        private val ENABLED_DEBUG: Set<Feature> = setOf(
            ComponentEditHint,
            ComponentLanguageTabBar
        )

        private val ENABLED_RELEASE: Set<Feature> = setOf(
            ComponentEditHint
        )

        private val ENABLED_CURRENT: Set<Feature> = mapOf(
            DEBUG to ENABLED_DEBUG,
            RELEASE to ENABLED_RELEASE
        )[BuildConfig.BUILD_TYPE].orEmpty()
    }
}