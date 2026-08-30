package com.example.keywordblocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.LinkedList

class BlockerService : AccessibilityService() {

    // Servis telefonda gerçekten başladığı an bu uyarı çıkar
    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(applicationContext, "🔥 KEYWORD BLOCKER AKTİF EDİLDİ!", Toast.LENGTH_LONG).show()
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
        if (event == null) return

        // 1. Anlık event metinlerini kontrol et
        for (text in event.text) {
            if (checkText(text?.toString())) return
        }

        val source = event.source
        if (source != null) {
            if (checkText(source.text?.toString())) return
            if (checkText(source.contentDescription?.toString())) return
        }

        // 2. Ekrandaki tüm pencereleri ve Chrome ağacını derinlemesine tara (BFS)
        val rootNode = rootInActiveWindow ?: return

        // Whitelist kontrolü (AI Studio'daysa es geç)
        if (isWhitelisted(rootNode)) return

        scanWindowFast(rootNode)
    }

    private fun scanWindowFast(root: AccessibilityNodeInfo) {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)

        var count = 0
        while (queue.isNotEmpty() && count < 300) {
            val node = queue.poll() ?: continue
            count++

            val text = node.text?.toString()
            val desc = node.contentDescription?.toString()

            if (checkText(text) || checkText(desc)) {
                return
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
    }

    private fun checkText(rawText: String?): Boolean {
        if (rawText.isNullOrEmpty()) return false
        val lower = rawText.lowercase()

        for (word in blockedWords) {
            if (lower.contains(word)) {
                triggerBlock(word)
                return true
            }
        }
        return false
    }

    private fun isWhitelisted(root: AccessibilityNodeInfo): Boolean {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)

        var count = 0
        while (queue.isNotEmpty() && count < 50) {
            val node = queue.poll() ?: continue
            count++

            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""

            if (whitelistedDomains.any { text.contains(it) || desc.contains(it) }) {
                return true
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    private fun triggerBlock(matchedWord: String) {
        // Anında ana ekrana fırlat ve bildir
        performGlobalAction(GLOBAL_ACTION_HOME)
        Toast.makeText(applicationContext, "🚫 Yasaklı Kelime Engellendi: $matchedWord", Toast.LENGTH_SHORT).show()
    }

    override fun onInterrupt() {}
}
