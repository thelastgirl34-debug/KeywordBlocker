package com.example.keywordblocker

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class BlockerService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        showToast("🔥 Keyword Blocker Aktif!")
    }

    private val whitelistedDomains = listOf(
        "aistudio.google.com",
        "ai.google.dev"
    )

    private val blockedWords = listOf(
        "lol hentai", "league hentai", "league of legends hentai",
        "lol r34", "lol rule34", "league r34", "league rule34",
        "lol nsfw", "league nsfw", "league of legends nsfw",
        "lol porn", "league porn", "leaguerule34", "leagueoflegendsnsfw",
        "kda hentai", "k/da hentai", "kda r34", "kda rule34", "kda nsfw", "kda porn", "k/da nsfw",
        "ahri hentai", "ahri r34", "ahri rule34", "ahri nsfw", "ahri porn",
        "evelynn hentai", "evelynn r34", "evelynn rule34", "evelynn nsfw", "evelynn porn",
        "kaisa hentai", "kai'sa hentai", "kaisa r34", "kai'sa r34", "kaisa nsfw", "kai'sa nsfw", "kaisa porn",
        "akali hentai", "akali r34", "akali rule34", "akali nsfw", "akali porn",
        "jinx hentai", "jinx r34", "jinx rule34", "jinx nsfw",
        "miss fortune hentai", "miss fortune r34", "miss fortune nsfw",
        "gwen hentai", "gwen r34", "riven hentai", "lux hentai", "lux r34",
        "seraphine hentai", "seraphine r34", "seraphine nsfw",
        "hentai", "ecchi", "doujin", "doujinshi", "rule34", "r34",
        "hanime", "nhentai", "hentaigasm", "hentaihaven", "e-hentai",
        "eromanga", "ero anime", "ahegao", "anime nsfw", "anime porn",
        "18+ anime", "anime 18+", "manga 18+", "tsumino", "hitomi.la",
        "fakku", "danbooru", "gelbooru", "pururin", "luscious"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event == null) return

            // 1. Klavyeden yazılan anlık metin kontrolü
            val eventText = event.text.joinToString(" ").lowercase()
            if (containsBlockedWord(eventText)) {
                triggerBlock()
                return
            }

            // 2. Ekrandaki metinleri güvenli kontrol et
            val rootNode = rootInActiveWindow ?: return
            if (isWhitelisted(rootNode)) return

            checkNodeSafely(rootNode)
        } catch (e: Throwable) {
            // Hiçbir hata servisi kapatamaz, sessizce devam eder
        }
    }

    private fun checkNodeSafely(node: AccessibilityNodeInfo?) {
        if (node == null) return
        try {
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""

            if (containsBlockedWord(text) || containsBlockedWord(desc)) {
                triggerBlock()
                return
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    checkNodeSafely(child)
                }
            }
        } catch (e: Throwable) {
            // Düğüm okuma hatası olursa görmezden gel
        }
    }

    private fun isWhitelisted(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        try {
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""
            if (whitelistedDomains.any { text.contains(it) || desc.contains(it) }) return true

            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null && isWhitelisted(child)) return true
            }
        } catch (e: Throwable) {
            return false
        }
        return false
    }

    private fun containsBlockedWord(text: String): Boolean {
        if (text.isEmpty()) return false
        return blockedWords.any { text.contains(it) }
    }

    private fun triggerBlock() {
        try {
            performGlobalAction(GLOBAL_ACTION_HOME)
            showToast("🚫 Yasaklı İçerik Kapatıldı!")
        } catch (e: Throwable) {}
    }

    private fun showToast(msg: String) {
        try {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {}
    }

    override fun onInterrupt() {}
}
