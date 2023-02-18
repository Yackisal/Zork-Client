package cc.zork.values

open class BooleanValue(name: String, value: Boolean) : Value<Boolean>(name, value) {

    open fun getCurrent(): Boolean {
        return value
    }

    fun setBoolean(stage: Boolean) {
        value = stage
    }

}