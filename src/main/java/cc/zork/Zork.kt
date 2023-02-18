package cc.zork

import cc.zork.event.Shader2DEvent
import cc.zork.features.ui.HUD.Companion.blur
import cc.zork.features.ui.HUD.Companion.iterations
import cc.zork.features.ui.HUD.Companion.offset
import cc.zork.features.ui.HUD.Companion.radius
import cc.zork.features.ui.HUD.Companion.shadow
import cc.zork.features.ui.HUD.Companion.shadowOffset
import cc.zork.features.ui.HUD.Companion.shadowRadius
import cc.zork.lua.LuaManager
import cc.zork.manager.CommandsManager
import cc.zork.manager.ConfigManager
import cc.zork.manager.EventManager
import cc.zork.manager.ModuleManager
import cc.zork.manager.ui.NotificationManager
import cc.zork.ui.font.FontManager
import cc.zork.ui.mainmanu.MainScreen
import cc.zork.utils.auth.Auth
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.StencilUtil
import cc.zork.utils.render.shader.BloomUtil
import cc.zork.utils.render.shader.GaussianBlur
import cc.zork.utils.render.shader.KawaseBlur
import me.konago.nativeobfuscator.Macros
import me.konago.nativeobfuscator.Native
import me.konago.nativeobfuscator.Winlicense
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Native
class Zork {
    @Native
    companion object {
        @JvmField
        var rank: String = ""
        val INSTANCE = Zork()
        var verify_step = 0

        @JvmField
        var chars: CharArray? = CharArray(65535)

        @JvmField
        var ascii_chars = CharArray(256)
        const val CLIENT_NAME = "Zork"
        const val CLIENT_VERSION = "v1.1"
        const val CLIENT_AUTHORS = "SuperSkidder, Kendall"
        lateinit var fontManager: FontManager
        lateinit var notificationManager: NotificationManager
        lateinit var configManager: ConfigManager
        lateinit var luaManager: LuaManager
        var banchecker = PunishmentStatistics()
        var commandsManager: CommandsManager = CommandsManager()

        var username: String = ""

        val logger: Logger = LogManager.getLogger(CLIENT_NAME)

        init {
            for (i in 0 until chars!!.size) {
                chars!![i] = i.toChar()
            }

            for (i in ascii_chars.indices) {
                ascii_chars[i] = i.toChar()
            }
        }

        @JvmStatic
        fun startClient(code: Int) {
            Macros.define(Winlicense.VM_TIGER_BLACK_START)
            var verify = true
            if (code == 213) {
                verify = false
                logger.info("$CLIENT_NAME $CLIENT_VERSION  By: $CLIENT_AUTHORS")
                //todo: 修复textfield和告示板等位置文字无法显示的bug
//            mc.fontRendererObj = FontManager.small
                notificationManager = NotificationManager()
                configManager = ConfigManager()
            }
            if(verify){
                Auth.doCrash()
                if(!MainScreen.verify_crash) {
                    ModuleManager.mods.clear()
                }
            }
            configManager.read("config")
            logger.info("Read config")
            Macros.define(Winlicense.VM_TIGER_BLACK_START)
        }

        fun stopClient() {
            configManager.save("config")
        }

    }

}