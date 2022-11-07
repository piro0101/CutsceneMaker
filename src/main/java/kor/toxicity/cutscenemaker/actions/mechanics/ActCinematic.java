package kor.toxicity.cutscenemaker.actions.mechanics;

import kor.toxicity.cutscenemaker.CutsceneMaker;
import kor.toxicity.cutscenemaker.CutsceneManager;
import kor.toxicity.cutscenemaker.actions.RepeatableAction;
import kor.toxicity.cutscenemaker.util.DataField;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ActCinematic extends RepeatableAction {

    @DataField(aliases = {"f","s"},throwable = true)
    public String from;
    @DataField(aliases = "e",throwable = true)
    public String to;

    @DataField(aliases = "c")
    public boolean cloak = true;

    private final Map<LivingEntity,Integer> loops = new HashMap<>();
    private final CutsceneManager manager;
    private Location first;
    private Location last;

    private Function<Integer,Location> function;

    public ActCinematic(CutsceneManager pl) {
        super(pl);
        manager = pl;
    }

    @Override
    public void initialize() {
        Map<String,Location> loc = manager.getLocations();
        if (loc.containsKey(from) && loc.containsKey(to)) {
            first = loc.get(from);
            last = loc.get(to);

            int loop = ticks * interval;

            double x = (last.getX() - first.getX())/loop;
            double y = (last.getY() - first.getY())/loop;
            double z = (last.getZ() - first.getZ())/loop;
            float pitch = (last.getPitch() - first.getPitch())/loop;
            float t = last.getYaw() > first.getYaw() ? last.getYaw() - first.getYaw() : 360 + last.getYaw() - first.getYaw();
            float yaw = ((cloak) ? -360 + t : t)/loop;

            function = i -> {
                double v1 = (double) i;
                float v2 = (float) i;

                double x1 = x * v1;
                double y1 = y * v1;
                double z1 = z * v1;
                float pitch1 = pitch * v2;
                float yaw1 = yaw * v2;

                Location get = new Location(first.getWorld(),first.getX() + x1, first.getY() + y1, first.getZ() + z1);
                get.setPitch(first.getPitch() + pitch1);
                get.setYaw(first.getPitch() + yaw1);

                return get;
            };
        } else CutsceneMaker.warn("unable to find location. (" + from + ", " + to + ")");
    }

    @Override
    protected void initialize(LivingEntity entity) {
        if (first != null && last != null) {
            loops.put(entity,0);
            entity.teleport(first, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    @Override
    protected void update(LivingEntity entity) {
        if (loops.containsKey(entity)) {
            int i = loops.get(entity);
            entity.teleport(function.apply(i), PlayerTeleportEvent.TeleportCause.PLUGIN);
            loops.put(entity,i + 1);
        }
    }

    @Override
    protected void end(LivingEntity end) {
        loops.remove(end);
    }
}
