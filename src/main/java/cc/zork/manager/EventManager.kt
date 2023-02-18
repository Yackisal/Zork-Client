package cc.zork.manager

import cc.zork.event.Event
import cc.zork.event.Subscribe
import java.lang.reflect.Method

class EventManager {
    companion object{
        private val registry = HashMap<Class<out Event>, MutableList<EventHook>>()

        fun registerListener(listener: Any) {
            for (method in listener.javaClass.declaredMethods) {
                if (method.isAnnotationPresent(Subscribe::class.java) && method.parameterTypes.size == 1) {
                    if (!method.isAccessible)
                        method.isAccessible = true

                    val eventClass = method.parameterTypes[0] as Class<out Event>

                    val invokableEventTargets = registry.getOrDefault(eventClass, ArrayList())
                    invokableEventTargets.add(EventHook(listener, method))
                    registry[eventClass] = invokableEventTargets
                }
            }
        }

        /**
         * Unregister listener
         *
         * @param listenable for unregister
         */
        fun unregisterListener(listenable: Any) {
            for ((key, targets) in registry) {
                targets.removeIf { it.eventClass == listenable }

                registry[key] = targets
            }
        }

        /**
         * Call event to listeners
         *
         * @param event to call
         */
        fun callEvent(event: Event) {
            val targets = registry[event.javaClass] ?: return
            for (i in 0 until targets.size) {
                try {
                    targets[i].method.invoke(targets[i].eventClass, event)
                } catch (throwable: Throwable) {
                    throwable.printStackTrace()
                }
            }
        }
    }
}
internal class EventHook(val eventClass: Any, val method: Method) {
}