package gmail.aryanj1010.immunesmp.files;

import gmail.aryanj1010.immunesmp.ImmuneSMP;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class PlayerImmunities extends AbstractFile{


    public PlayerImmunities(ImmuneSMP main) {
        super(main, "playerImmunities.yml");
    }

    public void tryNewPlayer(Player p) {
        String immunity = ImmuneSMP.random().toString();
        if (config.get(p.getUniqueId().toString()) == null) config.set(p.getUniqueId().toString(), immunity);
        p.sendMessage("Your Immunity is: " + immunity);
    }

    public boolean tryAddImmunity(Player p, ImmuneSMP.Immunities i) {
        String[] rawImmunities = config.get(p.getUniqueId().toString()).toString().split(",");
        if (Arrays.asList(rawImmunities).contains(i.toString())) {
            return false;
        } else if (Arrays.asList(rawImmunities).contains("NONE")){
            config.set(p.getUniqueId().toString(),  i.toString());
            save();
            return true;
        }
        else {
            config.set(p.getUniqueId().toString(),  config.get(p.getUniqueId().toString()) + "," + i.toString());
            save();
            return true;

        }

    }

    public boolean tryRemoveImmunity(Player p, ImmuneSMP.Immunities i) {
        String[] rawImmunities = config.get(p.getUniqueId().toString()).toString().split(",");
        if (Arrays.asList(rawImmunities).contains(i.toString()) && !(getImmunities(p).length == 1)) {
            config.set(p.getUniqueId().toString(), config.get(p.getUniqueId().toString()).toString().replace("," + i.toString(), ""));
            config.set(p.getUniqueId().toString(), config.get(p.getUniqueId().toString()).toString().replace(i.toString(), ""));
            config.set(p.getUniqueId().toString(), config.get(p.getUniqueId().toString()).toString().replace(i.toString() + ",", ""));

            save();
            return true;
        } else if (Arrays.asList(rawImmunities).contains(i.toString()) && getImmunities(p).length == 1){
            config.set(p.getUniqueId().toString(), config.get(p.getUniqueId().toString()).toString().replace(i.toString(), "NONE"));
            save();
            return true;
        }
        else {
            return false;
        }
    }
    public String[] getImmunities (Player p) {
        return config.get(p.getUniqueId().toString()).toString().split(",");
    }
    public boolean containsPlayer(Player player) {
        return config.contains(player.getUniqueId().toString());
    }

    public boolean playerHasImmunity(Player p, ImmuneSMP.Immunities i) {
        return Arrays.asList(config.get(p.getUniqueId().toString()).toString().split(",")).contains(i.toString());
    }

}
