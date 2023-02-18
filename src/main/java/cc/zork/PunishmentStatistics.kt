package cc.zork

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import cc.zork.event.Subscribe
import cc.zork.event.TickEvent
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.ui.notification.Type
import cc.zork.utils.client.notify
import cc.zork.utils.math.TimerUtil
import java.net.URL
import java.io.InputStreamReader
import java.io.BufferedReader
import java.net.HttpURLConnection
import kotlin.concurrent.thread

class PunishmentStatistics : Module("BanTracker", "ban tracker", ModuleType.Misc) {
    companion object {
        var success: Boolean = false
        var watchdog_lastMinute: Int = 0
        var staff_rollingDaily: Int = 0
        var watchdog_total: Int = 0
        var watchdog_rollingDaily: Int = 0
        var staff_total: Int = 0
        fun update() {
            val url = URL("https://api.hypixel.net/punishmentstats")
//            val url = URL("https://baidu.com")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36"
            )
            conn.setRequestProperty(
                "accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
            )
            conn.setRequestProperty("accept-language", "en-US,en;q=0.9")
            conn.setRequestProperty("cache-control", "max-age=0")
            conn.setRequestProperty(
                "sec-ch-ua",
                "\"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"108\", \"Microsoft Edge\";v=\"108\""
            )
            conn.setRequestProperty("sec-ch-ua-mobile", "?0")
            conn.setRequestProperty("sec-ch-ua-platform", "\"Windows\"")
            conn.setRequestProperty("sec-fetch-user", "?1")
            conn.setRequestProperty("upgrade-insecure-requests", "1")
            conn.setRequestProperty("API-key", "3f611991-cc4b-49f8-a206-6b13284c35e0")
            try {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonString = reader.readText()
                println(jsonString)
                val gson = Gson()
                val json = gson.fromJson(jsonString, JsonObject::class.java)
                staff_rollingDaily = json.get("staff_rollingDaily").asInt
                watchdog_rollingDaily = json.get("watchdog_rollingDaily").asInt
                watchdog_lastMinute = json.get("watchdog_lastMinute").asInt
                staff_total = json.get("staff_total").asInt
                watchdog_total = json.get("watchdog_total").asInt
            } catch (e: Throwable) {
                println("get bans error: ${e.localizedMessage}")
            }
        }
    }

    val timer = TimerUtil()

    var wd = 0
    var staff = 0

    @Subscribe
    fun tick(e: TickEvent) {
        var watchdog_total = PunishmentStatistics.watchdog_total
        if (wd < watchdog_total && wd != 0) {
            notify("Watchdog banned ${watchdog_total - wd} player just now")
            wd = watchdog_total
        }
        var staff_total = PunishmentStatistics.staff_total
        if (staff < staff_total && staff != 0) {
            Zork.notificationManager.add(
                "Ban tracker",
                "Staff banned ${staff_total - staff} players just now",
                2f,
                Type.Info
            )
            staff = staff_total
        }

        if (timer.hasReached(10000.0) && Minecraft.getMinecraft().thePlayer != null) {
            thread {
                update()
            }
            timer.reset()
        }
    }

}