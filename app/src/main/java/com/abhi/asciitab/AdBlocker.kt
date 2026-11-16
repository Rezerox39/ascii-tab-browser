package com.abhi.asciitab

object AdBlocker {
    private val blocked = setOf(
        "doubleclick.net", "googleadservices.com", "googlesyndication.com",
        "facebook.com/tr", "analytics.google.com", "scorecardresearch.com",
        "outbrain.com", "taboola.com", "amazon-adsystem.com",
        "adsystem.amazon.com", "moatads.com", "adnxs.com"
    )

    fun isBlocked(host: String): Boolean {
        val h = host.lowercase()
        return blocked.any { h == it || h.endsWith(".$it") }
    }
}
