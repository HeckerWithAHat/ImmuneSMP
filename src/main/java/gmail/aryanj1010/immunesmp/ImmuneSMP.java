package gmail.aryanj1010.immunesmp;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import gmail.aryanj1010.immunesmp.files.PlayerImmunities;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static gmail.aryanj1010.immunesmp.ImmuneSMP.Immunities.*;

public final class ImmuneSMP extends JavaPlugin implements Listener {
    PlayerImmunities pi;

    public ItemStack RandomizerToken =  new ItemStack(Material.SCUTE);
    public ItemMeta RTMeta = RandomizerToken.getItemMeta();

    private final HashMap<UUID, Long> voidCD;
    private final HashMap<UUID, Long> regenCD;
    private final HashMap<UUID, Integer> playerActionBars;

    public ImmuneSMP() {
        this.voidCD = new HashMap<>();
        this.regenCD = new HashMap<>();

        this.playerActionBars = new HashMap<>();
    }
    @Override
    public void onEnable() {
        // Plugin startup logic
        pi = new PlayerImmunities(this);
        getServer().getPluginManager().registerEvents(this,this);

        RTMeta.setDisplayName(ChatColor.getByChar('k') + "#" + ChatColor.RESET + "Randomizer Token" + ChatColor.getByChar('k') + "#");
        RTMeta.setLore(List.of("Give The User A Random New Immunity"));
        RTMeta.setUnbreakable(true);
        RandomizerToken.setItemMeta(RTMeta);

        ShapedRecipe rtr = new ShapedRecipe(new NamespacedKey(this, "RandomizerToken"), RandomizerToken);
        rtr.shape("abc","ded","fgh");
        rtr.setIngredient('a', Material.LAVA_BUCKET);
        rtr.setIngredient('b', Material.ENDER_PEARL);
        rtr.setIngredient('c', Material.WATER_BUCKET);
        rtr.setIngredient('d', Material.NETHERITE_INGOT);
        rtr.setIngredient('e', Material.ELYTRA);
        rtr.setIngredient('f', Material.POWDER_SNOW_BUCKET);
        rtr.setIngredient('g', Material.ENDER_EYE);
        rtr.setIngredient('h', Material.GRASS_BLOCK);
        getServer().addRecipe(rtr);

        ShapedRecipe sbr = new ShapedRecipe(new NamespacedKey(this, "CraftableShulkerBoxes"),new ItemStack(Material.SHULKER_BOX));
        sbr.shape("dnd","dcd","dnd");
        sbr.setIngredient('d', Material.DIAMOND);
        sbr.setIngredient('n', Material.NETHERITE_SCRAP);
        sbr.setIngredient('c', Material.CHEST);
        getServer().addRecipe(sbr);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> pi.save(), 1, 20*60*5);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        pi.save();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if (command.getName().equalsIgnoreCase("disableImmunityDisplay")) {
            if (playerActionBars.containsKey(p.getUniqueId())) {
                getServer().getScheduler().cancelTask(playerActionBars.get(p.getUniqueId()));
                p.sendMessage("Disabled!");
                playerActionBars.remove(p.getUniqueId());
                return true;
            } else {
                p.sendMessage("You can't disable what is already disabled");
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("enableImmunityDisplay")) {
            if (!playerActionBars.containsKey(p.getUniqueId())) {
                addActionBar(p);
                p.sendMessage("Enabled!");
                playerActionBars.put(p.getUniqueId(), getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> addActionBar(p), 1, 30));
                return true;
            } else {
                p.sendMessage("You can't enable what is already enabled");
                return true;
            }
        }
        return true;
    }

    public static Immunities random() {
        return Immunities.values()[new Random().nextInt(Immunities.values().length - 1)];
    }
    public static Immunities random(Immunities i) {
        while (true) {
            Immunities randI = Immunities.values()[new Random().nextInt(Immunities.values().length-1)];
            if (randI == i) {
                return randI;
            }
        }
    }
    public static Immunities random(Immunities[] i) {
        if (i.length == 0) return null;
        if (i.length == 1) return i[0];
        while (true) {
            Immunities randI = Immunities.values()[new Random().nextInt(Immunities.values().length-1)];
            if ((Arrays.asList(i).contains(randI))) {
                return randI;
            }
        }
    }
    public static Immunities randomExempt(Immunities i) {
        while (true) {
            Immunities randI = Immunities.values()[new Random().nextInt(Immunities.values().length-1)];
            if (randI != i) {
                return randI;
            }
        }
    }
    public static Immunities randomExempts(Immunities[] i) {
        if (i.length == 7) return null;
        while (true) {
            Immunities randI = Immunities.values()[new Random().nextInt(Immunities.values().length-1)];
            if (!(Arrays.asList(i).contains(randI))) {
                return randI;
            }
        }
    }
    public Immunities stringToImmunity (String stringToConvert) {
        Immunities output = null;
        switch (stringToConvert) {
            case "FIRE": output = FIRE;break;
            case "WATER": output = WATER;break;
            case "GROUND": output = GROUND;break;
            //case "VOID": output = VOID;break;
            case "SNOW": output = SNOW;break;
            case "BLAST": output = BLAST;break;
            case "NONE": output = NONE;break;
            case "PROJECTION": output = PROJECTION;break;
            case "REGENERATION": output = REGENERATION;break;
        }
        return output;
    }
    public Immunities[] stringsToImmunities (String[] stringsToConvert) {
        Immunities[] output = {};
        List<Immunities> l = new ArrayList<>(Arrays.asList(output));
        for (String s: stringsToConvert) {
            switch (s) {
                case "FIRE": l.add(FIRE);break;
                case "WATER": l.add(WATER);break;
                case "GROUND": l.add(GROUND);break;
                //case "VOID": l.add(VOID);break;
                case "SNOW": l.add(SNOW);break;
                case "BLAST": l.add(BLAST);break;
                case "NONE": l.add(NONE);break;
                case "PROJECTION": l.add(PROJECTION);break;
                case "REGENERATION": l.add(REGENERATION);break;
            }
        }
        return l.toArray(output);
    }
    public enum Immunities {
        REGENERATION, PROJECTION, FIRE, WATER, GROUND, SNOW, BLAST, NONE
    }

    @EventHandler
    public void playerTakeDamage (EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        List<String> i = Arrays.asList(pi.getImmunities(p));
        DamageCause dc = e.getCause();
        switch (dc) {
            case FIRE: if (i.contains(FIRE.toString())) e.setCancelled(true);break;
            case LAVA: if (i.contains(FIRE.toString())) {e.setCancelled(true);p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*3, 0, true, true));}break;
            case FIRE_TICK: if (i.contains(FIRE.toString())) e.setCancelled(true);break;
            case DROWNING: if (i.contains(WATER.toString())) e.setCancelled(true);break;
            case FALL: if (i.contains(GROUND.toString())) e.setCancelled(true);break;
            case SUFFOCATION: if (i.contains(GROUND.toString())) e.setCancelled(true);break;
            case ENTITY_EXPLOSION: if (i.contains(BLAST.toString())) e.setCancelled(true);break;
            case BLOCK_EXPLOSION: if (i.contains(BLAST.toString())) e.setCancelled(true);break;
            case FREEZE: if (i.contains(SNOW.toString())) e.setCancelled(true);break;
            case SONIC_BOOM: if (i.contains(BLAST.toString())) e.setCancelled(true);break;
            case PROJECTILE: if (i.contains(PROJECTION.toString())) e.setCancelled(true);break;
            /*case VOID: if (i.contains(VOID.toString())) {
                e.setCancelled(true);
                Block highestBlock = p.getWorld().getHighestBlockAt(p.getLocation());
                p.teleport(highestBlock.getLocation().add(0,1,0));
            }break;*/

            }

        }

    @EventHandler public void onPlayerJoin (PlayerJoinEvent e) {
        if (!pi.containsPlayer(e.getPlayer())) {
            pi.tryNewPlayer(e.getPlayer());
        }
        updateEffects(e.getPlayer());
        addActionBar(e.getPlayer());
        playerActionBars.put(e.getPlayer().getUniqueId(), getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> addActionBar(e.getPlayer()), 1, 30));
    }
    @EventHandler public void onPlayerLeave (PlayerQuitEvent e) {
        getServer().getScheduler().cancelTask(playerActionBars.get(e.getPlayer().getUniqueId()));
        playerActionBars.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler public void onPlayerHitWithProjectile (ProjectileHitEvent e) {
        if ((e.getEntity() instanceof Snowball) && (Arrays.asList(pi.getImmunities((Player) e.getEntity().getShooter())).contains(Immunities.SNOW.toString())) && (e.getHitEntity() != null) && (e.getEntity().getShooter() instanceof Player) && (e.getEntity().getShooter() != null)) {
            ((LivingEntity) e.getHitEntity()).damage(6, (Player) e.getEntity().getShooter());
        }
    }

    @EventHandler public void onPlayerDie (PlayerDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;
        if (e.getEntity() == e.getEntity().getKiller()) return;

        Player killer = e.getEntity().getKiller();
        Player killed = e.getEntity();
        if (pi.getImmunities(killer).length == 7) return;
        if (Arrays.asList(pi.getImmunities(killed)).contains("NONE")) return;
        //if (pi.getImmunities(killed).length == 1) return;
        while (true) {
            Immunities imToTransfer = random(stringsToImmunities(pi.getImmunities(killed)));
            if (pi.playerHasImmunity(killer, imToTransfer)) return;
            if (!Arrays.asList(pi.getImmunities(killer)).contains(imToTransfer.name())) {
                pi.tryRemoveImmunity(killed, imToTransfer);
                killed.sendMessage("You Lost: " + imToTransfer);
                pi.tryAddImmunity(killer, imToTransfer);
                killer.sendMessage("You Gained: " + imToTransfer);
                updateEffects(killer);
                updateEffects(killed);
                break;
            }
        }
    }

    @EventHandler public void clickEvent (PlayerInteractEvent e) {
        Player p = e.getPlayer();
        List<String> i = Arrays.asList(pi.getImmunities(p));
        /*if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.getPlayer().isSneaking()) {
            if (i.contains(VOID.toString())) {
                if (!this.voidCD.containsKey(p.getUniqueId())) {
                    p.teleport(p.getLocation().getWorld().getHighestBlockAt(p.getLocation().add(Math.random() * 25, 0, Math.random() * 25)).getLocation());
                    this.voidCD.put(p.getUniqueId(), System.currentTimeMillis());
                } else {
                    long TimeElapsed = System.currentTimeMillis() - this.voidCD.get(p.getUniqueId());
                    if (TimeElapsed >= 30000) {
                        p.teleport(p.getLocation().getWorld().getHighestBlockAt(p.getLocation().add(Math.random() * 25, 0, Math.random() * 25)).getLocation());
                        this.voidCD.put(p.getUniqueId(), System.currentTimeMillis());
                    } else {
                        p.sendMessage("30 seconds hasn't passed! Please Wait " + Math.round((float) ((30000 - TimeElapsed) / 1000)) + " seconds");
                    }
                }
            }
        }*/
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getPlayer().isSneaking() && Arrays.asList(pi.getImmunities(p)).contains(REGENERATION.toString())) {
            if (!regenCD.containsKey(p.getUniqueId())){
                p.setHealth(p.getMaxHealth());
                regenCD.put(p.getUniqueId(), System.currentTimeMillis());
            } else {
                long TimeElapsed = System.currentTimeMillis() - regenCD.get(p.getUniqueId());
                if (TimeElapsed >= 1000*60*3) {
                    p.setHealth(p.getMaxHealth());
                    regenCD.put(p.getUniqueId(), System.currentTimeMillis());
                } else {
                    p.sendMessage("180 seconds hasn't passed! Please Wait " + Math.round((float) ((180000 - TimeElapsed) / 1000)) + " seconds");

                }
            }
        }
    }
    @EventHandler public void onClickItem (PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (e.getItem().getItemMeta().hasLore() && e.getItem().getItemMeta().getLore().equals(List.of("Give The User A Random New Immunity")) && (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            pi.tryAddImmunity(e.getPlayer(), random(stringsToImmunities(pi.getImmunities(e.getPlayer()))));
            pi.tryAddImmunity(e.getPlayer(), randomExempts(stringsToImmunities(pi.getImmunities(e.getPlayer()))));
            e.getPlayer().getInventory().getItem(Arrays.asList(e.getPlayer().getInventory().getContents()).indexOf(e.getItem())).setAmount(e.getItem().getAmount() - 1);
            updateEffects(e.getPlayer());
        }

    }

    public void updateEffects(Player p) {
        Immunities[] immunities = stringsToImmunities(pi.getImmunities(p));
        p.removePotionEffect(PotionEffectType.FAST_DIGGING);
        p.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
        p.removePotionEffect(PotionEffectType.WATER_BREATHING);
        PotionEffect haste = new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1, true, true);
        PotionEffect dolphinsGrace = new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 1, true, true);
        PotionEffect waterBreathing = new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 1, true, true);
        if (Arrays.asList(immunities).contains(WATER)) {p.addPotionEffect(dolphinsGrace);p.addPotionEffect(waterBreathing);}
        if (Arrays.asList(immunities).contains(GROUND)) p.addPotionEffect(haste);
        pi.save();
    }
    public Block findNearestBlock(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();
        double closestDistance = Double.MAX_VALUE;
        Block nearestBlock = null;

        for (int x = -500; x <= 500; x++) {
            for (int y = -500; y <= 500; y++) {
                for (int z = -500; z <= 500; z++) {
                    Location currentLocation = playerLocation.clone().add(x, y, z);
                    Block currentBlock = world.getBlockAt(currentLocation);
                    double distance = playerLocation.distance(currentLocation);

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        nearestBlock = currentBlock;
                    }
                }
            }
        }

        return nearestBlock;
    }
    @EventHandler public void onRespawn (PlayerRespawnEvent e) {
        updateEffects(e.getPlayer());
    }

    public void addActionBar (Player p) {

        String actionBarText = ChatColor.BOLD + "" + ChatColor.LIGHT_PURPLE + "Your Immunities Are: " + ChatColor.RESET;
        String immunitiesText = Arrays.toString(pi.getImmunities(p)).replaceAll("FIRE", ChatColor.DARK_RED + "FIRE");
        immunitiesText = immunitiesText.replaceAll("WATER", ChatColor.DARK_BLUE + "WATER" + ChatColor.RESET);
        immunitiesText = immunitiesText.replaceAll("VOID", ChatColor.BLACK + "VOID" + ChatColor.RESET);
        immunitiesText = immunitiesText.replaceAll("SNOW", ChatColor.WHITE + "SNOW" + ChatColor.RESET);
        immunitiesText = immunitiesText.replaceAll("BLAST", ChatColor.YELLOW + "BLAST" + ChatColor.RESET);
        immunitiesText = immunitiesText.replaceAll("GROUND", ChatColor.GREEN + "GROUND" + ChatColor.RESET);
        immunitiesText = immunitiesText.replaceAll("PROJECTION", ChatColor.GOLD + "PROJECTION" + ChatColor.RESET);
        immunitiesText = immunitiesText.replaceAll("REGENERATION", ChatColor.RED + "REGENERATION" + ChatColor.RESET);


        actionBarText = actionBarText + immunitiesText;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = manager.createPacket(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
        packet.getChatComponents().write(0, WrappedChatComponent.fromText(actionBarText));

        try {
            manager.sendServerPacket(p, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void onSmith(SmithItemEvent e) {
     Material m = e.getInventory().getResult().getType();
    if (m == Material.NETHERITE_HELMET || m == Material.NETHERITE_CHESTPLATE || m == Material.NETHERITE_LEGGINGS || m == Material.NETHERITE_BOOTS) e.setCancelled(true);
    }
}
