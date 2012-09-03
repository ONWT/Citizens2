package net.citizensnpcs.npc.ai;

import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Util;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMonster;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Navigation;
import net.minecraft.server.Packet18ArmAnimation;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class MCTargetStrategy implements PathStrategy, EntityTarget {
    private final boolean aggro;
    private int attackTicks;
    private final EntityLiving handle, target;
    private final Navigation navigation;
    private final NavigatorParameters parameters;

    public MCTargetStrategy(CitizensNPC handle, LivingEntity target, boolean aggro, NavigatorParameters params) {
        this.handle = handle.getHandle();
        this.target = ((CraftLivingEntity) target).getHandle();
        this.navigation = this.handle.getNavigation();
        this.aggro = aggro;
        this.parameters = params;
    }

    private boolean canAttack() {
        return attackTicks == 0
                && (handle.boundingBox.e > target.boundingBox.b && handle.boundingBox.b < target.boundingBox.e)
                && distanceSquared() <= ATTACK_DISTANCE && handle.l(target);
    }

    private double distanceSquared() {
        return handle.getBukkitEntity().getLocation().distanceSquared(target.getBukkitEntity().getLocation());
    }

    @Override
    public LivingEntity getTarget() {
        return (LivingEntity) target.getBukkitEntity();
    }

    @Override
    public Location getTargetAsLocation() {
        return getTarget().getLocation();
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.ENTITY;
    }

    @Override
    public boolean isAggressive() {
        return aggro;
    }

    @Override
    public void stop() {
        navigation.g();
    }

    @Override
    public boolean update() {
        if (target == null || target.dead)
            return true;
        navigation.a(target, parameters.speed());
        handle.getControllerLook().a(target, 10.0F, handle.bf());
        if (aggro && canAttack()) {
            if (handle instanceof EntityMonster) {
                ((EntityMonster) handle).k(target);
                // the cast is necessary to resolve overloaded method a
            } else if (handle instanceof EntityPlayer) {
                EntityPlayer humanHandle = (EntityPlayer) handle;
                humanHandle.attack(target);
                Util.sendPacketNearby(handle.getBukkitEntity().getLocation(), new Packet18ArmAnimation(
                        humanHandle, 1), 64);
            }
            attackTicks = ATTACK_DELAY_TICKS;
        }
        if (attackTicks > 0)
            attackTicks--;

        return false;
    }
    private static final int ATTACK_DELAY_TICKS = 20;

    private static final double ATTACK_DISTANCE = 1.75 * 1.75;
}