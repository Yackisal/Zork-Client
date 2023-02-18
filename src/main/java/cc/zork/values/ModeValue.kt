package cc.zork.values

open class ModeValue(name: String, value: String, private vararg var modes: String) : Value<String>(name, value) {

    fun getCurrent() : String {
        return value
    }

    fun getModes(): Array<out String> {
        return modes
    }

    fun setMode(mode: String) {
        value = mode
    }

}