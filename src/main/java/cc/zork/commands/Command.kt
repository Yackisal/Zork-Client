package cc.zork.commands


open class Command(var name: String, var description: String, var usage: String) {

    open fun execute(args: List<String>): Boolean {
        return true
    }
}