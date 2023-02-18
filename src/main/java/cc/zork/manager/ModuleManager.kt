package cc.zork.manager

import cc.zork.PunishmentStatistics
import cc.zork.event.KeyEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.combat.*
import cc.zork.features.misc.*
import cc.zork.features.movement.*
import cc.zork.features.player.*
import cc.zork.features.render.*
import cc.zork.features.ui.*
import net.minecraft.client.Minecraft

object ModuleManager {
    var mods: ArrayList<Module> = ArrayList()

    init {
        EventManager.registerListener(this)
    }


    @Subscribe
    fun key(event: KeyEvent) {
        mods.forEach {
            if (it.bind == event.key)
                it.toggle()
        }
    }


    @JvmStatic
    fun add(mod: Module) {
            mods.add(mod)
    }

    fun add(mod: Class<out Module>) {
        mods.add(mod.newInstance())
    }

    init {
        addModules(
            HUD::class.java,
            KeepSprint::class.java,
            Sprint::class.java,
            BetterHotbar::class.java,
            ClickGui::class.java,
            VLReset::class.java,
            BlockAnimation::class.java,
            AntiBot::class.java,
            KillAura::class.java,
            Teams::class.java,
            Reach::class.java,
            Disabler::class.java,
            Timer::class.java,
            NoSlow::class.java,
            Speed::class.java,
            Velocity::class.java,
            AutoArmor::class.java,
            ChestStealer::class.java,
            InvManager::class.java,
            Nuker::class.java,
            Scaffold::class.java,
            ESP::class.java,
            NameTags::class.java,
            Xray::class.java,
            InvMove::class.java,
            AntiFall::class.java,
            Blink::class.java,
            AutoSword::class.java,
            AutoPotion::class.java,
            AutoTool::class.java,
            ChestESP::class.java,
            Chams::class.java,
            Glow::class.java,
            Criticals::class.java,
            Capes::class.java,
            NoFall::class.java,
            PunishmentStatistics::class.java,
            SpeedMine::class.java,
            AntiStuck::class.java,
            BetterChat::class.java,
            Scoreboard::class.java,
            TargetStrafe::class.java,
            FastPlace::class.java,
            AutoPlay::class.java,
            NoJumpDelay::class.java
        )
    }

    fun addModules(vararg mods: Module) {
        mods.forEach(::add)
    }

    fun addModules(vararg mods: Class<out Module>) {
        mods.forEach(::add)
    }

    @JvmStatic
    fun getMod(type: Class<out Module>): Module {
        for (mod in mods) {
            if (mod.javaClass == type) {
                return mod
            }
        }
        throw NullPointerException("Module ${type.name} Not Found")
    }

    fun getModWithType(type: ModuleType): ArrayList<Module> {
        val mods = ArrayList<Module>()
        this.mods.forEach {
            if (it.category == type) {
                mods.add(it)
            }
        }
        return mods
    }
}