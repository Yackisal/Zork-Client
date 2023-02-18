package cc.zork.features.combat

import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.NumberValue

class Reach : Module("Reach", "Make yor arm longer", ModuleType.Combat) {
    companion object {
        val reach = NumberValue("Reach", 3.2, 0, 6, 0.1)
    }
}