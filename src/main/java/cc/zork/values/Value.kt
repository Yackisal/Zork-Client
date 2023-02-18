package cc.zork.values


open class Value<T : Any>(var name: String, value: T) : Annotation {
    var value = value
    open fun get(): Any {
        return when (value) {
            is String -> value as String
            is Int -> value as Int
            is Double -> value as Double
            is Float -> value as Float
            is Boolean -> value as Boolean
            is Long -> value as Long
            is Short -> value as Short
            is Byte -> value as Byte
            else -> throw IllegalArgumentException("value: $name is not a valid type")
        }
    }

    open fun set(value: T) {
        this.value = value
    }
}