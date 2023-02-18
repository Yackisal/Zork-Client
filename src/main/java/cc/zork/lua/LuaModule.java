package cc.zork.lua;

import cc.zork.event.*;
import cc.zork.features.Module;
import cc.zork.features.ModuleType;
import cc.zork.values.Value;
import party.iroiro.luajava.value.LuaValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LuaModule extends Module {

    Map<String, LuaValue> events = new HashMap<>();

    public LuaModule(String name, String desc, Map<String, LuaValue> events, List<Value> values) {
        super(name,desc, ModuleType.Lua);
        for (Value value : values) {
            addValue(value);
        }
        this.events.putAll(events);
    }

    public void callEvent(String name, Object... args) {
        try {
            events.forEach((k, v) -> {
                if (k.equals(name)) {
                    v.call(args);
                }
            });
        } catch (Exception e) {
            System.out.println("error when calling " + name);
        }
    }

    public void addValue(Value v){
        this.getValues().add(v);
    }

    @Override
    public void onEnable() {
        callEvent("on_enable");
    }

    @Override
    public void onDisable() {
        callEvent("on_disable");
    }


    @Subscribe
    public void onRender2D(Render2DEvent e) {
        callEvent("on_render_2d", e);
    }

    @Subscribe
    public void onEventKey(KeyEvent e) {
        callEvent("on_key", e);
    }

    @Subscribe
    public void eventRender3D(Render3DEvent e) {
        callEvent("on_render_3d", e);
    }

    @Subscribe
    public void eventMove(MotionEvent e) {
        callEvent("on_move", e);
    }

    @Subscribe
    public void eventPacketRecieve(PacketEvent e) {
        if (e.getType() == EventType.SEND) {
            callEvent("on_packet_send", e);
        } else if (e.getType() == EventType.RECEIVE) {
            callEvent("on_packet_received", e);
        }
    }

    @Subscribe
    public void eventPostUpdate(UpdateEvent e) {
        callEvent("on_post_update", e);
    }

    @Subscribe
    public void eventTick(TickEvent e) {
        callEvent("on_tick", e);
    }

}
