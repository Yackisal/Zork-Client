package cc.zork.values

open class NumberValue(name: String, value: Number, var min: Number, var max: Number, var inc: Number) : Value<Number>(name, value) {
    override fun get(): Number {
        return value
    }
}