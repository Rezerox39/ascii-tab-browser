package com.abhi.asciitab

object TabManager {
    val tabs = mutableListOf<Tab>()
    var activeTabId: Long? = null
    fun active(): Tab? = tabs.find { it.id == activeTabId }
}
