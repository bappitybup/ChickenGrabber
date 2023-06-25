package net.bappity;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class ChickenGrabEvents implements Listener {

    private final ChickenGrabber _plugin;
    private FileConfiguration _config;

    public ChickenGrabEvents(ChickenGrabber plugin, FileConfiguration config) {
        _plugin = plugin;
        _config = config;
        loadConfigValues();
    }

    private void loadConfigValues() {
        _config = _plugin.getConfig();
    }

    public void reload() {
        loadConfigValues();
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();

        if (clickedEntity instanceof Chicken && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            setChickenOnHead(player, (Chicken) clickedEntity);
            giveGlideFeather(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack currentItem = event.getCursor();

        if (currentItem != null && checkAndRemoveFeather(player, currentItem)) {
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(_plugin, () -> {
                player.setItemOnCursor(new ItemStack(Material.AIR));
                player.updateInventory();
            });
        }
    }

    private void setChickenOnHead(Player player, Chicken chicken) {
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof Chicken) {
                return;
            }
        }

        player.addPassenger(chicken);

        // Play mount sound
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity deadEntity = event.getEntity();

        if (deadEntity instanceof Chicken) {
            Chicken chicken = (Chicken) deadEntity;
            List<Entity> passengers = chicken.getPassengers();

            for (Entity passenger : passengers) {
                if (passenger instanceof Player) {
                    Player player = (Player) passenger;

                    // Dismount the chicken
                    dismountAllChickens(player, true);

                    // Remove the Glide Feather
                    ItemStack mainHandItem = player.getInventory().getItemInMainHand();
                    if (checkAndRemoveFeather(player, mainHandItem)) {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> droppedItems = event.getDrops();

        ItemStack glideFeather = null;

        for (ItemStack item : droppedItems) {
            if (checkAndRemoveFeather(player, item)) {
                glideFeather = item;
                break;
            }
        }

        // Remove the Glide Feather from the dropped items
        if (glideFeather != null) {
            event.getDrops().remove(glideFeather);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = event.getMainHandItem();
        ItemStack offHandItem = event.getOffHandItem();

        if (checkAndRemoveFeather(player, offHandItem)) {
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            event.setCancelled(true);
            player.getInventory().setItemInMainHand(mainHandItem);
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (checkAndRemoveFeather(event.getPlayer(), event.getItemDrop().getItemStack())) {
            event.getPlayer().removePotionEffect(PotionEffectType.SLOW_FALLING);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack previousItem = player.getInventory().getItem(event.getPreviousSlot());

        if (previousItem != null) {
            checkAndRemoveFeather(player, previousItem);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        checkAndRemoveFeather(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
    }

    private void giveGlideFeather(Player player) {
        ItemStack glideFeather = new ItemStack(Material.FEATHER);
        ItemMeta glideFeatherMeta = glideFeather.getItemMeta();

        glideFeatherMeta.setDisplayName(ChatColor.AQUA + "Glide Feather");
        glideFeatherMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        glideFeatherMeta.addEnchant(EnchantmentWrapper.getByKey(Enchantment.DURABILITY.getKey()), 1, true);

        glideFeather.setItemMeta(glideFeatherMeta);

        player.getInventory().setItemInMainHand(glideFeather);
    }

    private boolean checkAndRemoveFeather(Player player, ItemStack itemHeld) {
        boolean featherDetected = false;

        if (itemHeld != null && itemHeld.getType() == Material.FEATHER && itemHeld.getItemMeta().hasDisplayName()
                && itemHeld.getItemMeta().getDisplayName().equals(ChatColor.AQUA + "Glide Feather")) {
            itemHeld.setAmount(0);
            dismountAllChickens(player, false);
            featherDetected = true;
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }

        return featherDetected;
    }

    private void dismountAllChickens(Player player, boolean shouldPlayDamageSound) {
        for (Entity passenger : player.getPassengers()) {
            player.removePassenger(passenger);

            if (shouldPlayDamageSound) {
                // Play the damage dismount sound
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_HURT, SoundCategory.PLAYERS, 1.0f,
                        1.0f);
            } else {
                // Play the dismount sound
                player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.PLAYERS, 1.0f,
                        1.0f);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Optional<Chicken> chickenOnHead = getChickenOnHead(player);

        if (chickenOnHead.isPresent()) {
            Chicken chicken = chickenOnHead.get();

            // Apply Slow Falling effect if the player has a chicken on their head
            applySlowFallingEffect(player);

            // Emit particle from the chicken's position if the player is not on the ground
            if (!isPlayerOnGround(player)) {
                emitParticlesFromChicken(chicken);
            }
        } else {
            // Remove Slow Falling effect if the player doesn't have a chicken on their head
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Chicken)) {
            return;
        }

        Chicken chicken = (Chicken) event.getEntity();
        Entity vehicle = chicken.getVehicle();

        if (vehicle == null || !(vehicle instanceof Player)) {
            return;
        }

        if (vehicle instanceof Player) {
            event.setDamage(0);
            event.setCancelled(true);
        }

        Player player = (Player) vehicle;

        // Dismount the chicken
        dismountAllChickens(player, true);

        // Remove the Glide Feather
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (checkAndRemoveFeather(player, mainHandItem)) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        // Get the direction of the hit
        Vector hitDirection;
        if (event.getDamager() instanceof Player) {
            hitDirection = ((Player) event.getDamager()).getEyeLocation().getDirection();
        } else if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Player) {
                hitDirection = ((Player) shooter).getEyeLocation().getDirection();
            } else {
                return;
            }
        } else {
            return;
        }

        // Normalize the direction and apply a multiplication factor for the launch
        // velocity
        hitDirection.normalize().multiply(0.8);

        // Set the Y component of the direction to create an upward launch
        hitDirection.setY(0.4);

        // Apply the launch velocity to the chicken
        chicken.setVelocity(hitDirection);
    }

    private Optional<Chicken> getChickenOnHead(Player player) {
        return player.getPassengers().stream()
                .filter(entity -> entity instanceof Chicken)
                .map(entity -> (Chicken) entity)
                .findFirst();
    }

    private void applySlowFallingEffect(Player player) {
        PotionEffect slowFalling = new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, true, false,
                false);
        player.addPotionEffect(slowFalling);
    }

    private void emitParticlesFromChicken(Chicken chicken) {
        World world = chicken.getWorld();
        world.spawnParticle(Particle.WHITE_ASH, chicken.getLocation().add(0, 0.5, 0), 5, 0.2, 0, 0.2, 0);
    }

    private boolean isPlayerOnGround(Player player) {
        Location loc = player.getLocation();
        loc.setY(loc.getY() - 0.01);
        return player.getWorld().getBlockAt(loc).getType().isSolid();
    }
}