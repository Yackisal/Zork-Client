package cc.zork.commands

import me.superskidder.IRCLoader
import me.superskidder.utils.irc.MessageUtil
import me.superskidder.utils.irc.Type

class IRCCommand: Command("irc","send client message",".irc <message>") {
    override fun execute(args: List<String>): Boolean {
        if(args.isEmpty())
            return false
        val message = args.joinToString(" ")
        IRCLoader.getClient().send(MessageUtil.makeMessage(Type.MESSAGE,"msg=$message"))
        return true
    }
}