package dev.u9g.customizer;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

public interface CustomItem {
    String name();
    @Nullable default Recipe recipe() { return null; }
    default ItemStack fuseRecipe(@Nullable ItemStack itemOne, @Nullable ItemStack itemTwo) { return null; }
    default void onRightClick(PlayerInteractEvent e) {}
    default void onPlace(BlockPlaceEvent e) {}
    default void onTakeDamage(EntityDamageEvent e) {}
    default void onAttack(EntityDamageByEntityEvent e) {}
    default void onProjectileHitObject(ProjectileHitEvent e) {}
    default void onWalkOnNewBlockWhileWearing(PlayerMoveEvent e) {}
    default void onJumpWhileWearing(PlayerJumpEvent e) {}
    default void onToggleSneakWhileWearing(PlayerToggleSneakEvent e) {}
}
