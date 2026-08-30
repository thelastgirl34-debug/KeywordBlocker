package com.example.keywordblocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class BlockerService : AccessibilityService() {

    // Servis açıldığı an ekrana uyarı basar (Çalıştığından emin olmak için)
    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(applicationContext, "Keyword Blocker Koruması Başladı!", Toast.LENGTH_LONG).show()
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

        // 1. Klavyeden yazılan metinleri kontrol et (Klavye açık olsa bile yakalar)
        val eventTexts = event.text.joinToString(" ").lowercase()
        val sourceText = event.source?.text?.toString()?.lowercase() ?: ""
        val sourceDesc = event.source?.contentDescription?.toString()?.lowercase() ?: ""

        val rootNode = rootInActiveWindow

        // Whitelist kontrolü (AI Studio'daysa es geç)
        if (rootNode != null && isWhitelisted(rootNode)) {
            return
        }

        // Kelime eşleşmesi var mı?
        if (containsBlockedWord(eventTexts) || containsBlockedWord(sourceText) || containsBlockedWord(sourceDesc)) {
            blockAction(rootNode)
            return
        }

        // 2. Ekrandaki yazıları kontrol et
        if (rootNode != null) {
            checkNode(rootNode)
        }
    }

    private fun isWhitelisted(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        if (whitelistedDomains.any { text.contains(it) || desc.contains(it) }) return true

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null && isWhitelisted(child)) return true
        }
        return false
    }

    private fun checkNode(node: AccessibilityNodeInfo) {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        if (containsBlockedWord(text) || containsBlockedWord(desc)) {
            blockAction(node)
            return
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { checkNode(it) }
        }
    }

    private fun containsBlockedWord(text: String): Boolean {
        if (text.isEmpty()) return false
        return blockedWords.any { text.contains(it) }
    }

    private fun blockAction(rootNode: AccessibilityNodeInfo?) {
        Toast.makeText(applicationContext, "YASAKLI İÇERİK ENGELLENDİ!", Toast.LENGTH_SHORT).show()

        // 1. Önce sekmeyi kapatmayı dene
        if (rootNode != null) {
            val closeButtons = rootNode.findAccessibilityNodeInfosByViewId("com.android.chrome:id/close_button")
            for (button in closeButtons) {
                if (button.isClickable) {
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return
                }
            }
        }

        // 2. Sekme butonu yoksa sayfayı geri al ve anında Ana Ekrana fırlat (Garanti Kapatma)
        performGlobalAction(GLOBAL_ACTION_BACK)
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onInterrupt() {}
}
