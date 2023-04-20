package kor.toxicity.cutscenemaker.actions.mechanics;

import kor.toxicity.cutscenemaker.CutsceneMaker;
import kor.toxicity.cutscenemaker.CutsceneManager;
import kor.toxicity.cutscenemaker.actions.CutsceneAction;
import kor.toxicity.cutscenemaker.util.reflect.DataField;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ActClone extends CutsceneAction {


    @DataField(aliases = "k",throwable = true)
    public String key;
    @DataField(aliases = {"loc","l"})
    public String location;

    private Location loc;

    public ActClone(CutsceneManager pl) {
        super(pl);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (location != null) loc = manager.getLocations().getValue(location);
    }

    @Override
    protected void apply(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            try {
                Entity entity1 = manager.getEntityManager().createMob(player,key, EntityType.ARMOR_STAND,(loc != null) ? loc : player.getLocation()).getEntity();
                entity1.setInvulnerable(true);
                entity1.setCustomNameVisible(false);
                DisguiseAPI.disguiseToPlayers(entity1,new PlayerDisguise(player).setNameVisible(false),player);
            } catch (Throwable e) {
                CutsceneMaker.warn("Unable to make a clone.");
            }
        }
    }
}
