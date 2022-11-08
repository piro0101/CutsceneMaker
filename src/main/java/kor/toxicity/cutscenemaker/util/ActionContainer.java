package kor.toxicity.cutscenemaker.util;

import kor.toxicity.cutscenemaker.CutsceneMaker;
import kor.toxicity.cutscenemaker.actions.CutsceneAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Predicate;

public class ActionContainer {

    private final CutsceneMaker pl;
    private final List<CutsceneAction> actions;
    private Predicate<LivingEntity> conditions;

    private BukkitTask delay;

    private final Map<LivingEntity,ActionRunning> tasks = new WeakHashMap<>();

    public ActionContainer(CutsceneMaker pl) {
        this.pl = pl;
        actions = new ArrayList<>();
    }

    public void add(CutsceneAction act) {
        actions.add(act);
    }

    public int size() {return actions.size();}
    public void setConditions(Predicate<LivingEntity> cond) {
        conditions = cond;
    }

    public boolean run(LivingEntity entity) {
        if ((conditions != null && !conditions.test(entity)) || delay != null) return false;
        if (tasks.containsKey(entity)) tasks.get(entity).kill();
        delay = Bukkit.getScheduler().runTaskLaterAsynchronously(pl,() -> delay = null,4);
        tasks.put(entity,new ActionRunning(entity));
        return true;
    }

    private class ActionRunning implements Listener {

        private BukkitTask task;
        private int loop;
        private final LivingEntity player;

        private final Map<String,LivingEntity> entities = new WeakHashMap<>();


        private ActionRunning(LivingEntity player) {
            this.player = player;
            EvtUtil.register(pl,this);
            load();
        }
        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            if (e.getPlayer().equals(player)) task.cancel();
        }

        private void kill() {
            if (task != null) task.cancel();
            entities.forEach((s,e) -> e.remove());
            EvtUtil.unregister(this);
            ActionContainer.this.tasks.remove(player);
        }
        private void load() {
            if (loop < actions.size()) {
                CutsceneAction action = actions.get(loop);
                action.apply(player);
                if (action.delay == 0) load();
                else task = Bukkit.getScheduler().runTaskLater(pl, this::load,action.delay);
                loop++;
            } else {
                task.cancel();
                EvtUtil.unregister(this);
            }
        }

    }

}
