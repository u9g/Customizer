package dev.u9g.customizer;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.common.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CustomItemRegistry <CustomizedCustomItem extends CustomItem> implements Listener {
    protected final Map<String, CustomizedCustomItem> itemsMap = new HashMap<>();

    public CustomItemRegistry(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        List<CustomizedCustomItem> items = new ArrayList<>();
        try {
            for (var clazz : Util.getExtendingClasses(plugin, new TypeToken<CustomizedCustomItem>() {}.getRawType())) {
                CustomizedCustomItem item = (CustomizedCustomItem) clazz.getConstructor().newInstance();
                items.add(item);
            }
        } catch (ReflectiveOperationException ignored) {}
        init((CustomizedCustomItem[]) items.toArray());
    }

    @SafeVarargs
    public CustomItemRegistry(Plugin plugin, CustomizedCustomItem... items) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        init(items);
    }

    private void init(CustomizedCustomItem[] items) {
        for (CustomizedCustomItem item : items) {
            itemsMap.put(item.name(), item);
            if (item.recipe() != null) {
                Bukkit.addRecipe(item.recipe());
            }
        }
    }

    @EventHandler
    private void onRightClick(PlayerInteractEvent e) {
        if (!e.getAction().isRightClick()) return;
        callOnItemClass(e.getItem(), c -> c.onRightClick(e));
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent e) {
        callOnItemClass(e.getItemInHand(), c -> c.onPlace(e));
    }

    @EventHandler
    private void onAttack(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            callOnItemClass(p.getEquipment(), c -> c.onTakeDamage(e));
        }
    }

    // allows for projectile hit event to register the entity
    @EventHandler
    private void onPlayerLaunchProjectile(PlayerLaunchProjectileEvent e) {
        String customItemName = e.getItemStack().getItemMeta().getPersistentDataContainer().get(CustomItemConstants.CUSTOM_ITEM_TYPE, PersistentDataType.STRING);
        if (customItemName == null) return;
        var customItemClass = itemsMap.get(customItemName);
        if (customItemClass == null) return;
        e.getProjectile().getPersistentDataContainer().set(CustomItemConstants.CUSTOM_ITEM_TYPE, PersistentDataType.STRING, customItemName);
    }

    @EventHandler
    private void onProjectileHit(ProjectileHitEvent e) {
        callOnItemClass(e.getEntity(), c -> c.onProjectileHitObject(e));
    }

    @EventHandler
    private void onEntityHurt(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            callOnItemClass(player.getEquipment().getItemInMainHand(), c-> c.onAttack(e));
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (e.hasChangedBlock()) {
            callOnItemClass(e.getPlayer().getEquipment(), c -> c.onWalkOnNewBlockWhileWearing(e));
        }
    }

    @EventHandler
    private void onJump(PlayerJumpEvent e) {
        callOnItemClass(e.getPlayer().getEquipment(), c -> c.onJumpWhileWearing(e));
    }

    @EventHandler
    private void onSneak(PlayerToggleSneakEvent e) {
        callOnItemClass(e.getPlayer().getEquipment(), c -> c.onToggleSneakWhileWearing(e));
    }

    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    protected void callOnItemClass (@NotNull EntityEquipment equipment, Consumer<CustomizedCustomItem> consumer) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            callOnItemClass(equipment.getItem(slot), consumer);
        }
    }

    protected void callOnItemClass (@Nullable ItemStack item, Consumer<CustomizedCustomItem> consumer) {
        if (item == null || item.getItemMeta() == null) return;
        callOnItemClass(item.getItemMeta(), consumer);
    }

    protected void callOnItemClass (@NotNull PersistentDataHolder pdh, Consumer<CustomizedCustomItem> consumer) {
        String customItemType = pdh.getPersistentDataContainer().get(CustomItemConstants.CUSTOM_ITEM_TYPE, PersistentDataType.STRING);
        if (customItemType == null) return;
        consumer.accept(itemsMap.get(customItemType));
    }
}
