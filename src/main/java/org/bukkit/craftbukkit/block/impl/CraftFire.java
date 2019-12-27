/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.block.impl;

public final class CraftFire extends org.bukkit.craftbukkit.block.data.CraftBlockData implements org.bukkit.block.data.type.Fire, org.bukkit.block.data.Ageable, org.bukkit.block.data.MultipleFacing {

    public CraftFire() {
        super();
    }

    public CraftFire(net.minecraft.block.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.block.data.CraftAgeable

    private static final net.minecraft.state.PropertyInteger AGE = getInteger(net.minecraft.block.FireBlock.class, "age");

    @Override
    public int getAge() {
        return get(AGE);
    }

    @Override
    public void setAge(int age) {
        set(AGE, age);
    }

    @Override
    public int getMaximumAge() {
        return getMax(AGE);
    }

    // org.bukkit.craftbukkit.block.data.CraftMultipleFacing

    private static final net.minecraft.state.PropertyBoolean[] FACES = new net.minecraft.state.PropertyBoolean[]{
        getBoolean(net.minecraft.block.FireBlock.class, "north", true), getBoolean(net.minecraft.block.FireBlock.class, "east", true), getBoolean(net.minecraft.block.FireBlock.class, "south", true), getBoolean(net.minecraft.block.FireBlock.class, "west", true), getBoolean(net.minecraft.block.FireBlock.class, "up", true), getBoolean(net.minecraft.block.FireBlock.class, "down", true)
    };

    @Override
    public boolean hasFace(org.bukkit.block.BlockFace face) {
        return get(FACES[face.ordinal()]);
    }

    @Override
    public void setFace(org.bukkit.block.BlockFace face, boolean has) {
        set(FACES[face.ordinal()], has);
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getFaces() {
        com.google.common.collect.ImmutableSet.Builder<org.bukkit.block.BlockFace> faces = com.google.common.collect.ImmutableSet.builder();

        for (int i = 0; i < FACES.length; i++) {
            if (FACES[i] != null && get(FACES[i])) {
                faces.add(org.bukkit.block.BlockFace.values()[i]);
            }
        }

        return faces.build();
    }

    @Override
    public java.util.Set<org.bukkit.block.BlockFace> getAllowedFaces() {
        com.google.common.collect.ImmutableSet.Builder<org.bukkit.block.BlockFace> faces = com.google.common.collect.ImmutableSet.builder();

        for (int i = 0; i < FACES.length; i++) {
            if (FACES[i] != null) {
                faces.add(org.bukkit.block.BlockFace.values()[i]);
            }
        }

        return faces.build();
    }
}
