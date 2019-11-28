package org.bukkit.event.entity;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;

/**
 * Called when a projectile hits an object
 */
public class ProjectileHitEvent extends EntityEvent {
    private static final HandlerList handlers = new HandlerList();
    //Uranium start
    private final Entity hitEntity;
    private final Block hitBlock;
    public ProjectileHitEvent(final Projectile projectile) {
        this(projectile,null,null);
    }

    public ProjectileHitEvent(Projectile projectile, Entity hitEntity) {
        this(projectile, hitEntity, (Block)null);
    }

    public ProjectileHitEvent(Projectile projectile, Block hitBlock) {
        this(projectile, (Entity)null, hitBlock);
    }

    public ProjectileHitEvent(Projectile projectile, Entity hitEntity, Block hitBlock) {
        super(projectile);
        this.hitEntity = hitEntity;
        this.hitBlock = hitBlock;
    }

    public Block getHitBlock() {
        return this.hitBlock;
    }

    public Entity getHitEntity() {
        return this.hitEntity;
    }

    //Uranium end

    @Override
    public Projectile getEntity() {
        return (Projectile) entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}