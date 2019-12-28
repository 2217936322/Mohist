package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.TicketManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.item.EyeOfEnderEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.LeashKnotEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.minecart.ChestMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.item.minecart.FurnaceMinecartEntity;
import net.minecraft.entity.item.minecart.HopperMinecartEntity;
import net.minecraft.entity.item.minecart.SpawnerMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.util.Direction;
import net.minecraft.entity.SpawnReason;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.chunk.ChunkPrimerWrapper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.server.Ticket;
import net.minecraft.world.server.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang.Validate;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.StructureType;
import org.bukkit.TreeType;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.metadata.BlockMetadataStore;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Cat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Illager;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.TraderLlama;
import org.bukkit.entity.Trident;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CraftWorld implements World {
    public static final int CUSTOM_DIMENSION_OFFSET = 10;

    private final ServerWorld world;
    private WorldBorder worldBorder;
    private Environment environment;
    private final CraftServer server = (CraftServer) Bukkit.getServer();
    private final ChunkGenerator generator;
    private final List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
    private final BlockMetadataStore blockMetadata = new BlockMetadataStore(this);
    private int monsterSpawn = -1;
    private int animalSpawn = -1;
    private int waterAnimalSpawn = -1;
    private int ambientSpawn = -1;

    private static final Random rand = new Random();

    public CraftWorld(ServerWorld world, ChunkGenerator gen, Environment env) {
        this.world = world;
        this.generator = gen;

        environment = env;
    }

    @Override
    public Block getBlockAt(int x, int y, int z) {
        return CraftBlock.at(world, new BlockPos(x, y, z));
    }

    @Override
    public int getHighestBlockYAt(int x, int z) {
        if (!isChunkLoaded(x >> 4, z >> 4)) {
            getChunkAt(x >> 4, z >> 4); // Transient load for this tick
        }

        return world.getHighestBlockYAt(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z)).getY();
    }

    @Override
    public Location getSpawnLocation() {
        BlockPos spawn = world.getSpawn();
        return new Location(this, spawn.getX(), spawn.getY(), spawn.getZ());
    }

    @Override
    public boolean setSpawnLocation(Location location) {
        Preconditions.checkArgument(location != null, "location");

        return equals(location.getWorld()) ? setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ()) : false;
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z) {
        try {
            Location previousLocation = getSpawnLocation();
            world.worldData.setSpawn(new BlockPos(x, y, z));

            // Notify anyone who's listening.
            SpawnChangeEvent event = new SpawnChangeEvent(this, previousLocation);
            server.getPluginManager().callEvent(event);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Chunk getChunkAt(int x, int z) {
        return this.world.getChunkProvider().getChunkAt(x, z, true).bukkitChunk;
    }

    @Override
    public Chunk getChunkAt(Block block) {
        return getChunkAt(block.getX() >> 4, block.getZ() >> 4);
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        net.minecraft.world.chunk.Chunk chunk = world.getChunkProvider().getChunkAt(x, z, false);
        return chunk != null;
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        try {
            return isChunkLoaded(x, z) || world.getChunkProvider().playerChunkDistanceGraph.read(new ChunkPos(x, z)) != null;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Chunk[] getLoadedChunks() {
        Long2ObjectLinkedOpenHashMap<PlayerChunk> chunks = world.getChunkProvider().playerChunkDistanceGraph.visibleChunks;
        return chunks.values().stream().map(PlayerChunk::getFullChunk).filter(Objects::nonNull).map(net.minecraft.world.chunk.Chunk::getBukkitChunk).toArray(Chunk[]::new);
    }

    @Override
    public void loadChunk(int x, int z) {
        loadChunk(x, z, true);
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return unloadChunk(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean unloadChunk(int x, int z) {
        return unloadChunk(x, z, true);
    }

    @Override
    public boolean unloadChunk(int x, int z, boolean save) {
        return unloadChunk0(x, z, save);
    }

    @Override
    public boolean unloadChunkRequest(int x, int z) {
        net.minecraft.world.chunk.IChunk chunk = world.getChunkProvider().getChunkAt(x, z, ChunkStatus.FULL, false);
        if (chunk != null) {
            world.getChunkProvider().removeTicket(TicketType.PLUGIN, chunk.getPos(), 1, Unit.INSTANCE);
        }

        return true;
    }

    private boolean unloadChunk0(int x, int z, boolean save) {
        net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) world.getChunkProvider().getChunkAt(x, z, ChunkStatus.FULL, false);
        if (chunk == null) {
            return true;
        }

        chunk.mustNotSave = !save;
        unloadChunkRequest(x, z);

        world.getChunkProvider().purgeUnload();
        return !isChunkLoaded(x, z);
    }

    @Override
    public boolean regenerateChunk(int x, int z) {
        throw new UnsupportedOperationException("Not supported in this Minecraft version! Unless you can fix it, this is not a bug :)");
        /*
        if (!unloadChunk0(x, z, false)) {
            return false;
        }

        final long chunkKey = ChunkPos.pair(x, z);
        world.getChunkProvider().unloadQueue.remove(chunkKey);

        net.minecraft.world.chunk.Chunk chunk = world.getChunkProvider().generateChunk(x, z);
        PlayerChunk playerChunk = world.getPlayerChunkDistanceGraph().getChunk(x, z);
        if (playerChunk != null) {
            playerChunk.chunk = chunk;
        }

        if (chunk != null) {
            refreshChunk(x, z);
        }

        return chunk != null;
        */
    }

    @Override
    public boolean refreshChunk(int x, int z) {
        if (!isChunkLoaded(x, z)) {
            return false;
        }

        int px = x << 4;
        int pz = z << 4;

        // If there are more than 64 updates to a chunk at once, it will update all 'touched' sections within the chunk
        // And will include biome data if all sections have been 'touched'
        // This flags 65 blocks distributed across all the sections of the chunk, so that everything is sent, including biomes
        int height = getMaxHeight() / 16;
        for (int idx = 0; idx < 64; idx++) {
            world.notify(new BlockPos(px + (idx / height), ((idx % height) * 16), pz), Blocks.AIR.getBlockData(), Blocks.STONE.getBlockData(), 3);
        }
        world.notify(new BlockPos(px + 15, (height * 16) - 1, pz + 15), Blocks.AIR.getBlockData(), Blocks.STONE.getBlockData(), 3);

        return true;
    }

    @Override
    public boolean isChunkInUse(int x, int z) {
        return isChunkLoaded(x, z);
    }

    @Override
    public boolean loadChunk(int x, int z, boolean generate) {
        IChunk chunk = world.getChunkProvider().getChunkAt(x, z, generate ? ChunkStatus.FULL : ChunkStatus.EMPTY, true);

        // If generate = false, but the chunk already exists, we will get this back.
        if (chunk instanceof ChunkPrimerExtension) {
            // We then cycle through again to get the full chunk immediately, rather than after the ticket addition
            chunk = world.getChunkProvider().getChunkAt(x, z, ChunkStatus.FULL, true);
        }

        if (chunk instanceof net.minecraft.world.chunk.Chunk) {
            world.getChunkProvider().addTicket(TicketType.PLUGIN, new ChunkPos(x, z), 1, Unit.INSTANCE);
            return true;
        }

        return false;
    }

    @Override
    public boolean isChunkLoaded(Chunk chunk) {
        return isChunkLoaded(chunk.getX(), chunk.getZ());
    }

    @Override
    public void loadChunk(Chunk chunk) {
        loadChunk(chunk.getX(), chunk.getZ());
        ((CraftChunk) getChunkAt(chunk.getX(), chunk.getZ())).getHandle().bukkitChunk = chunk;
    }

    @Override
    public boolean addPluginChunkTicket(int x, int z, Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "null plugin");
        Preconditions.checkArgument(plugin.isEnabled(), "plugin is not enabled");

        TicketManager chunkDistanceManager = this.world.getChunkProvider().playerChunkDistanceGraph.chunkDistanceManager;

        if (chunkDistanceManager.addTicketAtLevel(TicketType.PLUGIN_TICKET, new ChunkPos(x, z), 31, plugin)) { // keep in-line with force loading, add at level 31
            this.getChunkAt(x, z); // ensure loaded
            return true;
        }

        return false;
    }

    @Override
    public boolean removePluginChunkTicket(int x, int z, Plugin plugin) {
        Preconditions.checkNotNull(plugin, "null plugin");

        TicketManager chunkDistanceManager = this.world.getChunkProvider().playerChunkDistanceGraph.chunkDistanceManager;
        return chunkDistanceManager.removeTicketAtLevel(TicketType.PLUGIN_TICKET, new ChunkPos(x, z), 31, plugin); // keep in-line with force loading, remove at level 31
    }

    @Override
    public void removePluginChunkTickets(Plugin plugin) {
        Preconditions.checkNotNull(plugin, "null plugin");

        TicketManager chunkDistanceManager = this.world.getChunkProvider().playerChunkDistanceGraph.chunkDistanceManager;
        chunkDistanceManager.removeAllTicketsFor(TicketType.PLUGIN_TICKET, 31, plugin); // keep in-line with force loading, remove at level 31
    }

    @Override
    public Collection<Plugin> getPluginChunkTickets(int x, int z) {
        TicketManager chunkDistanceManager = this.world.getChunkProvider().playerChunkDistanceGraph.chunkDistanceManager;
        SortedArraySet<Ticket<?>> tickets = chunkDistanceManager.tickets.get(ChunkPos.pair(x, z));

        if (tickets == null) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<Plugin> ret = ImmutableList.builder();
        for (Ticket<?> ticket : tickets) {
            if (ticket.getTicketType() == TicketType.PLUGIN_TICKET) {
                ret.add((Plugin) ticket.identifier);
            }
        }

        return ret.build();
    }

    @Override
    public Map<Plugin, Collection<Chunk>> getPluginChunkTickets() {
        Map<Plugin, ImmutableList.Builder<Chunk>> ret = new HashMap<>();
        TicketManager chunkDistanceManager = this.world.getChunkProvider().playerChunkDistanceGraph.chunkDistanceManager;

        for (Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> chunkTickets : chunkDistanceManager.tickets.long2ObjectEntrySet()) {
            long chunkKey = chunkTickets.getLongKey();
            SortedArraySet<Ticket<?>> tickets = chunkTickets.getValue();

            Chunk chunk = null;
            for (Ticket<?> ticket : tickets) {
                if (ticket.getTicketType() != TicketType.PLUGIN_TICKET) {
                    continue;
                }

                if (chunk == null) {
                    chunk = this.getChunkAt(ChunkPos.getX(chunkKey), ChunkPos.getZ(chunkKey));
                }

                ret.computeIfAbsent((Plugin) ticket.identifier, (key) -> ImmutableList.builder()).add(chunk);
            }
        }

        return ret.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
    }

    @Override
    public boolean isChunkForceLoaded(int x, int z) {
        return getHandle().getForceLoadedChunks().contains(ChunkPos.pair(x, z));
    }

    @Override
    public void setChunkForceLoaded(int x, int z, boolean forced) {
        getHandle().setForceLoaded(x, z, forced);
    }

    @Override
    public Collection<Chunk> getForceLoadedChunks() {
        Set<Chunk> chunks = new HashSet<>();

        for (long coord : getHandle().getForceLoadedChunks()) {
            chunks.add(getChunkAt(ChunkPos.getX(coord), ChunkPos.getZ(coord)));
        }

        return Collections.unmodifiableCollection(chunks);
    }

    public ServerWorld getHandle() {
        return world;
    }

    @Override
    public org.bukkit.entity.Item dropItem(Location loc, ItemStack item) {
        Validate.notNull(item, "Cannot drop a Null item.");
        ItemEntity entity = new ItemEntity(world, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
        entity.pickupDelay = 10;
        world.addEntity(entity, SpawnReason.CUSTOM);
        // TODO this is inconsistent with how Entity.getBukkitEntity() works.
        // However, this entity is not at the moment backed by a server entity class so it may be left.
        return new CraftItem(world.getServer(), entity);
    }

    @Override
    public org.bukkit.entity.Item dropItemNaturally(Location loc, ItemStack item) {
        double xs = (world.random.nextFloat() * 0.5F) + 0.25D;
        double ys = (world.random.nextFloat() * 0.5F) + 0.25D;
        double zs = (world.random.nextFloat() * 0.5F) + 0.25D;
        loc = loc.clone();
        loc.setX(loc.getX() + xs);
        loc.setY(loc.getY() + ys);
        loc.setZ(loc.getZ() + zs);
        return dropItem(loc, item);
    }

    @Override
    public Arrow spawnArrow(Location loc, Vector velocity, float speed, float spread) {
        return spawnArrow(loc, velocity, speed, spread, Arrow.class);
    }

    @Override
    public <T extends AbstractArrow> T spawnArrow(Location loc, Vector velocity, float speed, float spread, Class<T> clazz) {
        Validate.notNull(loc, "Can not spawn arrow with a null location");
        Validate.notNull(velocity, "Can not spawn arrow with a null velocity");
        Validate.notNull(clazz, "Can not spawn an arrow with no class");

        AbstractArrowEntity arrow;
        if (TippedArrow.class.isAssignableFrom(clazz)) {
            arrow = EntityType.ARROW.a(world);
            ((ArrowEntity) arrow).setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
        } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
            arrow = EntityType.SPECTRAL_ARROW.a(world);
        } else if (Trident.class.isAssignableFrom(clazz)){
            arrow = EntityType.TRIDENT.a(world);
        } else {
            arrow = EntityType.ARROW.a(world);
        }

        arrow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        arrow.shoot(velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
        world.addEntity(arrow);
        return (T) arrow.getBukkitEntity();
    }

    @Override
    public Entity spawnEntity(Location loc, EntityType entityType) {
        return spawn(loc, entityType.getEntityClass());
    }

    @Override
    public LightningStrike strikeLightning(Location loc) {
        LightningBoltEntity lightning = new LightningBoltEntity(world, loc.getX(), loc.getY(), loc.getZ(), false);
        world.strikeLightning(lightning);
        return new CraftLightningStrike(server, lightning);
    }

    @Override
    public LightningStrike strikeLightningEffect(Location loc) {
        LightningBoltEntity lightning = new LightningBoltEntity(world, loc.getX(), loc.getY(), loc.getZ(), true);
        world.strikeLightning(lightning);
        return new CraftLightningStrike(server, lightning);
    }

    @Override
    public boolean generateTree(Location loc, TreeType type) {
        BlockPos pos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        net.minecraft.world.Feature gen;
        net.minecraft.world.gen.feature.IFeatureConfig conf;
        switch (type) {
        case BIG_TREE:
            gen = Feature.FANCY_TREE;
            conf = DefaultBiomeFeatures.FANCY_TREE;
            break;
        case BIRCH:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.BIRCH_TREE;
            break;
        case REDWOOD:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.SPRUCE_TREE;
            break;
        case TALL_REDWOOD:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.PINE_TREE;
            break;
        case JUNGLE:
            gen = Feature.MEGA_JUNGLE_TREE;
            conf = DefaultBiomeFeatures.MEGA_JUNGLE_TREE;
            break;
        case SMALL_JUNGLE:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.JUNGLE_TREE_NOVINE;
            break;
        case COCOA_TREE:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.JUNGLE_TREE;
            break;
        case JUNGLE_BUSH:
            gen = Feature.JUNGLE_GROUND_BUSH;
            conf = DefaultBiomeFeatures.JUNGLE_BUSH;
            break;
        case RED_MUSHROOM:
            gen = Feature.HUGE_RED_MUSHROOM;
            conf = DefaultBiomeFeatures.HUGE_RED_MUSHROOM;
            break;
        case BROWN_MUSHROOM:
            gen = Feature.HUGE_BROWN_MUSHROOM;
            conf = DefaultBiomeFeatures.HUGE_BROWN_MUSHROOM;
            break;
        case SWAMP:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.SWAMP_TREE;
            break;
        case ACACIA:
            gen = Feature.ACACIA_TREE;
            conf = DefaultBiomeFeatures.ACACIA_TREE;
            break;
        case DARK_OAK:
            gen = Feature.DARK_OAK_TREE;
            conf = DefaultBiomeFeatures.DARK_OAK_TREE;
            break;
        case MEGA_REDWOOD:
            gen = Feature.MEGA_SPRUCE_TREE;
            conf = DefaultBiomeFeatures.MEGA_PINE_TREE;
            break;
        case TALL_BIRCH:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.TALL_BIRCH_TREE;
            break;
        case CHORUS_PLANT:
            ((ChorusFlowerBlock) Blocks.CHORUS_FLOWER).a(world, pos, rand, 8);
            return true;
        case TREE:
        default:
            gen = Feature.NORMAL_TREE;
            conf = DefaultBiomeFeatures.NORMAL_TREE;
            break;
        }

        return gen.generate(world, world.worldProvider.getChunkGenerator(), rand, pos, conf);
    }

    @Override
    public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate) {
        world.captureTreeGeneration = true;
        world.captureBlockStates = true;
        boolean grownTree = generateTree(loc, type);
        world.captureBlockStates = false;
        world.captureTreeGeneration = false;
        if (grownTree) { // Copy block data to delegate
            for (BlockState blockstate : world.capturedBlockStates) {
                BlockPos position = ((CraftBlockState) blockstate).getPosition();
                net.minecraft.block.BlockState oldBlock = world.getType(position);
                int flag = ((CraftBlockState) blockstate).getFlag();
                delegate.setBlockData(blockstate.getX(), blockstate.getY(), blockstate.getZ(), blockstate.getBlockData());
                net.minecraft.block.BlockState newBlock = world.getType(position);
                world.notifyAndUpdatePhysics(position, null, oldBlock, newBlock, newBlock, flag);
            }
            world.capturedBlockStates.clear();
            return true;
        } else {
            world.capturedBlockStates.clear();
            return false;
        }
    }

    @Override
    public String getName() {
        return world.worldData.getName();
    }

    @Deprecated
    public long getId() {
        return world.worldData.getSeed();
    }

    @Override
    public UUID getUID() {
        return world.getDataManager().getUUID();
    }

    @Override
    public String toString() {
        return "CraftWorld{name=" + getName() + '}';
    }

    @Override
    public long getTime() {
        long time = getFullTime() % 24000;
        if (time < 0) time += 24000;
        return time;
    }

    @Override
    public void setTime(long time) {
        long margin = (time - getFullTime()) % 24000;
        if (margin < 0) margin += 24000;
        setFullTime(getFullTime() + margin);
    }

    @Override
    public long getFullTime() {
        return world.getDayTime();
    }

    @Override
    public void setFullTime(long time) {
        // Notify anyone who's listening
        TimeSkipEvent event = new TimeSkipEvent(this, TimeSkipEvent.SkipReason.CUSTOM, time - world.getDayTime());
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        world.setDayTime(world.getDayTime() + event.getSkipAmount());

        // Forces the client to update to the new time immediately
        for (Player p : getPlayers()) {
            CraftPlayer cp = (CraftPlayer) p;
            if (cp.getHandle().connection == null) continue;

            cp.getHandle().connection.sendPacket(new SUpdateTimePacket(cp.getHandle().world.getTime(), cp.getHandle().getPlayerTime(), cp.getHandle().world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
        }
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power) {
        return createExplosion(x, y, z, power, false, true);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire) {
        return createExplosion(x, y, z, power, setFire, true);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
        return createExplosion(x, y, z, power, setFire, breakBlocks, null);
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks, Entity source) {
        return !world.createExplosion(source == null ? null : ((CraftEntity) source).getHandle(), x, y, z, power, setFire, breakBlocks ? Explosion.Effect.BREAK : Explosion.Effect.NONE).wasCanceled;
    }

    @Override
    public boolean createExplosion(Location loc, float power) {
        return createExplosion(loc, power, false);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire) {
        return createExplosion(loc, power, setFire, true);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks) {
        return createExplosion(loc, power, setFire, breakBlocks, null);
    }

    @Override
    public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks, Entity source) {
        Preconditions.checkArgument(loc != null, "Location is null");
        Preconditions.checkArgument(this.equals(loc.getWorld()), "Location not in world");

        return createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire, breakBlocks, source);
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public Block getBlockAt(Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public int getHighestBlockYAt(Location location) {
        return getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public Chunk getChunkAt(Location location) {
        return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    @Override
    public ChunkGenerator getGenerator() {
        return generator;
    }

    @Override
    public List<BlockPopulator> getPopulators() {
        return populators;
    }

    @Override
    public Block getHighestBlockAt(int x, int z) {
        return getBlockAt(x, getHighestBlockYAt(x, z), z);
    }

    @Override
    public Block getHighestBlockAt(Location location) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public Biome getBiome(int x, int z) {
        return getBiome(x, 0, z);
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return CraftBlock.biomeBaseToBiome(this.world.getBiome(new BlockPos(x, y, z)));
    }

    @Override
    public void setBiome(int x, int z, Biome bio) {
        for (int y = 0; y < getMaxHeight(); y++) {
            setBiome(x, y, z, bio);
        }
    }

    @Override
    public void setBiome(int x, int y, int z, Biome bio) {
        Biome bb = CraftBlock.biomeToBiome(bio);
        BlockPos pos = new BlockPos(x, 0, z);
        if (this.world.isLoaded(pos)) {
            net.minecraft.world.chunk.Chunk chunk = this.world.getChunkAtWorldCoords(pos);

            if (chunk != null) {
                chunk.getBiomeIndex().setBiome(x, y, z, bb);

                chunk.markDirty(); // SPIGOT-2890
            }
        }
    }

    @Override
    public double getTemperature(int x, int z) {
        return getTemperature(x, 0, z);
    }

    @Override
    public double getTemperature(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return this.world.getBiome(pos).getAdjustedTemperature(pos);
    }

    @Override
    public double getHumidity(int x, int z) {
        return getHumidity(x, 0, z);
    }

    @Override
    public double getHumidity(int x, int y, int z) {
        return this.world.getBiome(new BlockPos(x, y, z)).getHumidity();
    }

    @Override
    public List<Entity> getEntities() {
        List<Entity> list = new ArrayList<Entity>();

        for (Object o : world.entitiesById.values()) {
            if (o instanceof net.minecraft.entity.Entity) {
                net.minecraft.entity.Entity mcEnt = (net.minecraft.entity.Entity) o;
                Entity bukkitEntity = mcEnt.getBukkitEntity();

                // Assuming that bukkitEntity isn't null
                if (bukkitEntity != null && bukkitEntity.isValid()) {
                    list.add(bukkitEntity);
                }
            }
        }

        return list;
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        List<LivingEntity> list = new ArrayList<LivingEntity>();

        for (Object o : world.entitiesById.values()) {
            if (o instanceof net.minecraft.entity.Entity) {
                net.minecraft.entity.Entity mcEnt = (net.minecraft.entity.Entity) o;
                Entity bukkitEntity = mcEnt.getBukkitEntity();

                // Assuming that bukkitEntity isn't null
                if (bukkitEntity != null && bukkitEntity instanceof LivingEntity && bukkitEntity.isValid()) {
                    list.add((LivingEntity) bukkitEntity);
                }
            }
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Deprecated
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
        return (Collection<T>)getEntitiesByClasses(classes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> clazz) {
        Collection<T> list = new ArrayList<T>();

        for (Object entity: world.entitiesById.values()) {
            if (entity instanceof net.minecraft.entity.Entity) {
                Entity bukkitEntity = ((net.minecraft.entity.Entity) entity).getBukkitEntity();

                if (bukkitEntity == null) {
                    continue;
                }

                Class<?> bukkitClass = bukkitEntity.getClass();

                if (clazz.isAssignableFrom(bukkitClass) && bukkitEntity.isValid()) {
                    list.add((T) bukkitEntity);
                }
            }
        }

        return list;
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
        Collection<Entity> list = new ArrayList<Entity>();

        for (Object entity: world.entitiesById.values()) {
            if (entity instanceof net.minecraft.entity.Entity) {
                Entity bukkitEntity = ((net.minecraft.entity.Entity) entity).getBukkitEntity();

                if (bukkitEntity == null) {
                    continue;
                }

                Class<?> bukkitClass = bukkitEntity.getClass();

                for (Class<?> clazz : classes) {
                    if (clazz.isAssignableFrom(bukkitClass)) {
                        if (bukkitEntity.isValid()) {
                            list.add(bukkitEntity);
                        }
                        break;
                    }
                }
            }
        }

        return list;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        return this.getNearbyEntities(location, x, y, z, null);
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z, Predicate<Entity> filter) {
        Validate.notNull(location, "Location is null!");
        Validate.isTrue(this.equals(location.getWorld()), "Location is from different world!");

        BoundingBox aabb = BoundingBox.of(location, x, y, z);
        return this.getNearbyEntities(aabb, filter);
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox) {
        return this.getNearbyEntities(boundingBox, null);
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox, Predicate<Entity> filter) {
        Validate.notNull(boundingBox, "Bounding box is null!");

        AxisAlignedBB bb = new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        List<net.minecraft.entity.Entity> entityList = getHandle().getEntities((net.minecraft.entity.Entity) null, bb, null);
        List<Entity> bukkitEntityList = new ArrayList<org.bukkit.entity.Entity>(entityList.size());

        for (net.minecraft.entity.Entity entity : entityList) {
            Entity bukkitEntity = entity.getBukkitEntity();
            if (filter == null || filter.test(bukkitEntity)) {
                bukkitEntityList.add(bukkitEntity);
            }
        }

        return bukkitEntityList;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance) {
        return this.rayTraceEntities(start, direction, maxDistance, null);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize) {
        return this.rayTraceEntities(start, direction, maxDistance, raySize, null);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, Predicate<Entity> filter) {
        return this.rayTraceEntities(start, direction, maxDistance, 0.0D, filter);
    }

    @Override
    public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize, Predicate<Entity> filter) {
        Validate.notNull(start, "Start location is null!");
        Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
        start.checkFinite();

        Validate.notNull(direction, "Direction is null!");
        direction.checkFinite();

        Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");

        if (maxDistance < 0.0D) {
            return null;
        }

        Vector startPos = start.toVector();
        Vector dir = direction.clone().normalize().multiply(maxDistance);
        BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
        Collection<Entity> entities = this.getNearbyEntities(aabb, filter);

        Entity nearestHitEntity = null;
        RayTraceResult nearestHitResult = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (Entity entity : entities) {
            BoundingBox boundingBox = entity.getBoundingBox().expand(raySize);
            RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);

            if (hitResult != null) {
                double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());

                if (distanceSq < nearestDistanceSq) {
                    nearestHitEntity = entity;
                    nearestHitResult = hitResult;
                    nearestDistanceSq = distanceSq;
                }
            }
        }

        return (nearestHitEntity == null) ? null : new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity, nearestHitResult.getHitBlockFace());
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance) {
        return this.rayTraceBlocks(start, direction, maxDistance, FluidCollisionMode.NEVER, false);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        return this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, false);
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) {
        Validate.notNull(start, "Start location is null!");
        Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
        start.checkFinite();

        Validate.notNull(direction, "Direction is null!");
        direction.checkFinite();

        Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");
        Validate.notNull(fluidCollisionMode, "Fluid collision mode is null!");

        if (maxDistance < 0.0D) {
            return null;
        }

        Vector dir = direction.clone().normalize().multiply(maxDistance);
        Vec3d startPos = new Vec3d(start.getX(), start.getY(), start.getZ());
        Vec3d endPos = new Vec3d(start.getX() + dir.getX(), start.getY() + dir.getY(), start.getZ() + dir.getZ());
        RayTraceResult nmsHitResult = this.getHandle().rayTrace(new RayTrace(startPos, endPos, ignorePassableBlocks ? RayTrace.BlockCollisionOption.COLLIDER : RayTrace.BlockCollisionOption.OUTLINE, CraftFluidCollisionMode.toNMS(fluidCollisionMode), null));

        return CraftRayTraceResult.fromNMS(this, nmsHitResult);
    }

    @Override
    public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize, Predicate<Entity> filter) {
        RayTraceResult blockHit = this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, ignorePassableBlocks);
        Vector startVec = null;
        double blockHitDistance = maxDistance;

        // limiting the entity search range if we found a block hit:
        if (blockHit != null) {
            startVec = start.toVector();
            blockHitDistance = startVec.distance(blockHit.getHitPosition());
        }

        RayTraceResult entityHit = this.rayTraceEntities(start, direction, blockHitDistance, raySize, filter);
        if (blockHit == null) {
            return entityHit;
        }

        if (entityHit == null) {
            return blockHit;
        }

        // Cannot be null as blockHit == null returns above
        double entityHitDistanceSquared = startVec.distanceSquared(entityHit.getHitPosition());
        if (entityHitDistanceSquared < (blockHitDistance * blockHitDistance)) {
            return entityHit;
        }

        return blockHit;
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> list = new ArrayList<Player>(world.getPlayers().size());

        for (PlayerEntity human : world.getPlayers()) {
            HumanEntity bukkitEntity = human.getBukkitEntity();

            if ((bukkitEntity != null) && (bukkitEntity instanceof Player)) {
                list.add((Player) bukkitEntity);
            }
        }

        return list;
    }

    @Override
    public void save() {
        this.server.checkSaveState();
        try {
            boolean oldSave = world.savingDisabled;

            world.savingDisabled = false;
            world.save(null, false, false);

            world.savingDisabled = oldSave;
        } catch (SessionLockException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isAutoSave() {
        return !world.savingDisabled;
    }

    @Override
    public void setAutoSave(boolean value) {
        world.savingDisabled = !value;
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.getHandle().worldData.setDifficulty(Difficulty.getById(difficulty.getValue()));
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.getByValue(this.getHandle().getDifficulty().ordinal());
    }

    public BlockMetadataStore getBlockMetadata() {
        return blockMetadata;
    }

    @Override
    public boolean hasStorm() {
        return world.worldData.hasStorm();
    }

    @Override
    public void setStorm(boolean hasStorm) {
        world.worldData.setStorm(hasStorm);
        setWeatherDuration(0); // Reset weather duration (legacy behaviour)
    }

    @Override
    public int getWeatherDuration() {
        return world.worldData.getWeatherDuration();
    }

    @Override
    public void setWeatherDuration(int duration) {
        world.worldData.setWeatherDuration(duration);
    }

    @Override
    public boolean isThundering() {
        return world.worldData.isThundering();
    }

    @Override
    public void setThundering(boolean thundering) {
        world.worldData.setThundering(thundering);
        setThunderDuration(0); // Reset weather duration (legacy behaviour)
    }

    @Override
    public int getThunderDuration() {
        return world.worldData.getThunderDuration();
    }

    @Override
    public void setThunderDuration(int duration) {
        world.worldData.setThunderDuration(duration);
    }

    @Override
    public long getSeed() {
        return world.worldData.getSeed();
    }

    @Override
    public boolean getPVP() {
        return world.pvpMode;
    }

    @Override
    public void setPVP(boolean pvp) {
        world.pvpMode = pvp;
    }

    public void playEffect(Player player, Effect effect, int data) {
        playEffect(player.getLocation(), effect, data, 0);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data) {
        playEffect(location, effect, data, 64);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data) {
        playEffect(loc, effect, data, 64);
    }

    @Override
    public <T> void playEffect(Location loc, Effect effect, T data, int radius) {
        if (data != null) {
            Validate.isTrue(effect.getData() != null && effect.getData().isAssignableFrom(data.getClass()), "Wrong kind of data for this effect!");
        } else {
            Validate.isTrue(effect.getData() == null, "Wrong kind of data for this effect!");
        }

        int datavalue = data == null ? 0 : CraftEffect.getDataValue(effect, data);
        playEffect(loc, effect, datavalue, radius);
    }

    @Override
    public void playEffect(Location location, Effect effect, int data, int radius) {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(effect, "Effect cannot be null");
        Validate.notNull(location.getWorld(), "World cannot be null");
        int packetData = effect.getId();
        SPlaySoundEventPacket packet = new SPlaySoundEventPacket(packetData, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data, false);
        int distance;
        radius *= radius;

        for (Player player : getPlayers()) {
            if (((CraftPlayer) player).getHandle().connection == null) continue;
            if (!location.getWorld().equals(player.getWorld())) continue;

            distance = (int) player.getLocation().distanceSquared(location);
            if (distance <= radius) {
                ((CraftPlayer) player).getHandle().connection.sendPacket(packet);
            }
        }
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
        return spawn(location, clazz, null, SpawnReason.CUSTOM);
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function) throws IllegalArgumentException {
        return spawn(location, clazz, function, SpawnReason.CUSTOM);
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, MaterialData data) throws IllegalArgumentException {
        Validate.notNull(data, "MaterialData cannot be null");
        return spawnFallingBlock(location, data.getItemType(), data.getData());
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data) throws IllegalArgumentException {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(material, "Material cannot be null");
        Validate.isTrue(material.isBlock(), "Material must be a block");

        FallingBlockEntity entity = new FallingBlockEntity(world, location.getX(), location.getY(), location.getZ(), CraftMagicNumbers.getBlock(material).getBlockData());
        entity.ticksLived = 1;

        world.addEntity(entity, SpawnReason.CUSTOM);
        return (FallingBlock) entity.getBukkitEntity();
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, BlockData data) throws IllegalArgumentException {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(data, "Material cannot be null");

        FallingBlockEntity entity = new FallingBlockEntity(world, location.getX(), location.getY(), location.getZ(), ((CraftBlockData) data).getState());
        entity.ticksLived = 1;

        world.addEntity(entity, SpawnReason.CUSTOM);
        return (FallingBlock) entity.getBukkitEntity();
    }

    @SuppressWarnings("unchecked")
    public net.minecraft.entity.Entity createEntity(Location location, Class<? extends Entity> clazz) throws IllegalArgumentException {
        if (location == null || clazz == null) {
            throw new IllegalArgumentException("Location or entity class cannot be null");
        }

        net.minecraft.entity.Entity entity = null;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        // order is important for some of these
        if (Boat.class.isAssignableFrom(clazz)) {
            entity = new BoatEntity(world, x, y, z);
            entity.setPositionRotation(x, y, z, yaw, pitch);
        } else if (FallingBlock.class.isAssignableFrom(clazz)) {
            entity = new FallingBlockEntity(world, x, y, z, world.getType(new BlockPos(x, y, z)));
        } else if (Projectile.class.isAssignableFrom(clazz)) {
            if (Snowball.class.isAssignableFrom(clazz)) {
                entity = new SnowballEntity(world, x, y, z);
            } else if (Egg.class.isAssignableFrom(clazz)) {
                entity = new EggEntity(world, x, y, z);
            } else if (AbstractArrow.class.isAssignableFrom(clazz)) {
                if (TippedArrow.class.isAssignableFrom(clazz)) {
                    entity = EntityType.ARROW.a(world);
                    ((ArrowEntity) entity).setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
                } else if (SpectralArrow.class.isAssignableFrom(clazz)) {
                    entity = EntityType.SPECTRAL_ARROW.a(world);
                } else if (Trident.class.isAssignableFrom(clazz)) {
                    entity = EntityType.TRIDENT.a(world);
                } else {
                    entity = EntityType.ARROW.a(world);
                }
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (ThrownExpBottle.class.isAssignableFrom(clazz)) {
                entity = EntityType.EXPERIENCE_BOTTLE.a(world);
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (EnderPearl.class.isAssignableFrom(clazz)) {
                entity = EntityType.ENDER_PEARL.a(world);
                entity.setPositionRotation(x, y, z, 0, 0);
            } else if (ThrownPotion.class.isAssignableFrom(clazz)) {
                if (LingeringPotion.class.isAssignableFrom(clazz)) {
                    entity = new EntityPotion(world, x, y, z);
                    ((EntityPotion) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
                } else {
                    entity = new EntityPotion(world, x, y, z);
                    ((EntityPotion) entity).setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPLASH_POTION, 1)));
                }
            } else if (Fireball.class.isAssignableFrom(clazz)) {
                if (SmallFireball.class.isAssignableFrom(clazz)) {
                    entity = EntityType.SMALL_FIREBALL.a(world);
                } else if (WitherSkull.class.isAssignableFrom(clazz)) {
                    entity = EntityType.WITHER_SKULL.a(world);
                } else if (DragonFireball.class.isAssignableFrom(clazz)) {
                    entity = EntityType.DRAGON_FIREBALL.a(world);
                } else {
                    entity = EntityType.FIREBALL.a(world);
                }
                entity.setPositionRotation(x, y, z, yaw, pitch);
                Vector direction = location.getDirection().multiply(10);
                ((DamagingProjectileEntity) entity).setDirection(direction.getX(), direction.getY(), direction.getZ());
            } else if (ShulkerBullet.class.isAssignableFrom(clazz)) {
                entity = EntityType.SHULKER_BULLET.a(world);
                entity.setPositionRotation(x, y, z, yaw, pitch);
            } else if (LlamaSpit.class.isAssignableFrom(clazz)) {
                entity = EntityType.LLAMA_SPIT.a(world);
                entity.setPositionRotation(x, y, z, yaw, pitch);
            }
        } else if (Minecart.class.isAssignableFrom(clazz)) {
            if (PoweredMinecart.class.isAssignableFrom(clazz)) {
                entity = new FurnaceMinecartEntity(world, x, y, z);
            } else if (StorageMinecart.class.isAssignableFrom(clazz)) {
                entity = new ChestMinecartEntity(world, x, y, z);
            } else if (ExplosiveMinecart.class.isAssignableFrom(clazz)) {
                entity = new TNTMinecartEntity(world, x, y, z);
            } else if (HopperMinecart.class.isAssignableFrom(clazz)) {
                entity = new HopperMinecartEntity(world, x, y, z);
            } else if (SpawnerMinecart.class.isAssignableFrom(clazz)) {
                entity = new SpawnerMinecartEntity(world, x, y, z);
            } else if (CommandMinecart.class.isAssignableFrom(clazz)) {
                entity = new MinecartCommandBlockEntity(world, x, y, z);
            } else { // Default to rideable minecart for pre-rideable compatibility
                entity = new MinecartEntity(world, x, y, z);
            }
        } else if (EnderSignal.class.isAssignableFrom(clazz)) {
            entity = new EyeOfEnderEntity(world, x, y, z);
        } else if (EnderCrystal.class.isAssignableFrom(clazz)) {
            entity = EntityType.END_CRYSTAL.a(world);
            entity.setPositionRotation(x, y, z, 0, 0);
        } else if (LivingEntity.class.isAssignableFrom(clazz)) {
            if (Chicken.class.isAssignableFrom(clazz)) {
                entity = EntityType.CHICKEN.a(world);
            } else if (Cow.class.isAssignableFrom(clazz)) {
                if (MushroomCow.class.isAssignableFrom(clazz)) {
                    entity = EntityType.MOOSHROOM.a(world);
                } else {
                    entity = EntityType.COW.a(world);
                }
            } else if (Golem.class.isAssignableFrom(clazz)) {
                if (Snowman.class.isAssignableFrom(clazz)) {
                    entity = EntityType.SNOW_GOLEM.a(world);
                } else if (IronGolem.class.isAssignableFrom(clazz)) {
                    entity = EntityType.IRON_GOLEM.a(world);
                } else if (Shulker.class.isAssignableFrom(clazz)) {
                    entity = EntityType.SHULKER.a(world);
                }
            } else if (Creeper.class.isAssignableFrom(clazz)) {
                entity = EntityType.CREEPER.a(world);
            } else if (Ghast.class.isAssignableFrom(clazz)) {
                entity = EntityType.GHAST.a(world);
            } else if (Pig.class.isAssignableFrom(clazz)) {
                entity = EntityType.PIG.a(world);
            } else if (Player.class.isAssignableFrom(clazz)) {
                // need a net server handler for this one
            } else if (Sheep.class.isAssignableFrom(clazz)) {
                entity = EntityType.SHEEP.a(world);
            } else if (AbstractHorse.class.isAssignableFrom(clazz)) {
                if (ChestedHorse.class.isAssignableFrom(clazz)) {
                    if (Donkey.class.isAssignableFrom(clazz)) {
                        entity = EntityType.DONKEY.a(world);
                    } else if (Mule.class.isAssignableFrom(clazz)) {
                        entity = EntityType.MULE.a(world);
                    } else if (Llama.class.isAssignableFrom(clazz)) {
                        if (TraderLlama.class.isAssignableFrom(clazz)) {
                            entity = EntityType.TRADER_LLAMA.a(world);
                        } else {
                            entity = EntityType.LLAMA.a(world);
                        }
                    }
                } else if (SkeletonHorse.class.isAssignableFrom(clazz)) {
                    entity = EntityType.SKELETON_HORSE.a(world);
                } else if (ZombieHorse.class.isAssignableFrom(clazz)) {
                    entity = EntityType.ZOMBIE_HORSE.a(world);
                } else {
                    entity = EntityType.HORSE.a(world);
                }
            } else if (Skeleton.class.isAssignableFrom(clazz)) {
                if (Stray.class.isAssignableFrom(clazz)){
                    entity = EntityType.STRAY.a(world);
                } else if (WitherSkeleton.class.isAssignableFrom(clazz)) {
                    entity = EntityType.WITHER_SKELETON.a(world);
                } else {
                    entity = EntityType.SKELETON.a(world);
                }
            } else if (Slime.class.isAssignableFrom(clazz)) {
                if (MagmaCube.class.isAssignableFrom(clazz)) {
                    entity = EntityType.MAGMA_CUBE.a(world);
                } else {
                    entity = EntityType.SLIME.a(world);
                }
            } else if (Spider.class.isAssignableFrom(clazz)) {
                if (CaveSpider.class.isAssignableFrom(clazz)) {
                    entity = EntityType.CAVE_SPIDER.a(world);
                } else {
                    entity = EntityType.SPIDER.a(world);
                }
            } else if (Squid.class.isAssignableFrom(clazz)) {
                entity = EntityType.SQUID.a(world);
            } else if (Tameable.class.isAssignableFrom(clazz)) {
                if (Wolf.class.isAssignableFrom(clazz)) {
                    entity = EntityType.WOLF.a(world);
                } else if (Parrot.class.isAssignableFrom(clazz)) {
                    entity = EntityType.PARROT.a(world);
                } else if (Cat.class.isAssignableFrom(clazz)) {
                    entity = EntityType.CAT.a(world);
                }
            } else if (PigZombie.class.isAssignableFrom(clazz)) {
                entity = EntityType.ZOMBIE_PIGMAN.a(world);
            } else if (Zombie.class.isAssignableFrom(clazz)) {
                if (Husk.class.isAssignableFrom(clazz)) {
                    entity = EntityType.HUSK.a(world);
                } else if (ZombieVillager.class.isAssignableFrom(clazz)) {
                    entity = EntityType.ZOMBIE_VILLAGER.a(world);
                } else if (Drowned.class.isAssignableFrom(clazz)) {
                    entity = EntityType.DROWNED.a(world);
                } else {
                    entity = new ZombieEntity(world);
                }
            } else if (Giant.class.isAssignableFrom(clazz)) {
                entity = EntityType.GIANT.a(world);
            } else if (Silverfish.class.isAssignableFrom(clazz)) {
                entity = EntityType.SILVERFISH.a(world);
            } else if (Enderman.class.isAssignableFrom(clazz)) {
                entity = EntityType.ENDERMAN.a(world);
            } else if (Blaze.class.isAssignableFrom(clazz)) {
                entity = EntityType.BLAZE.a(world);
            } else if (AbstractVillager.class.isAssignableFrom(clazz)) {
                if (Villager.class.isAssignableFrom(clazz)) {
                    entity = EntityType.VILLAGER.a(world);
                } else if (WanderingTrader.class.isAssignableFrom(clazz)) {
                    entity = EntityType.WANDERING_TRADER.a(world);
                }
            } else if (Witch.class.isAssignableFrom(clazz)) {
                entity = EntityType.WITCH.a(world);
            } else if (Wither.class.isAssignableFrom(clazz)) {
                entity = EntityType.WITHER.a(world);
            } else if (ComplexLivingEntity.class.isAssignableFrom(clazz)) {
                if (EnderDragon.class.isAssignableFrom(clazz)) {
                    entity = EntityType.ENDER_DRAGON.a(world);
                }
            } else if (Ambient.class.isAssignableFrom(clazz)) {
                if (Bat.class.isAssignableFrom(clazz)) {
                    entity = EntityType.BAT.a(world);
                }
            } else if (Rabbit.class.isAssignableFrom(clazz)) {
                entity = EntityType.RABBIT.a(world);
            } else if (Endermite.class.isAssignableFrom(clazz)) {
                entity = EntityType.ENDERMITE.a(world);
            } else if (Guardian.class.isAssignableFrom(clazz)) {
                if (ElderGuardian.class.isAssignableFrom(clazz)){
                    entity = EntityType.ELDER_GUARDIAN.a(world);
                } else {
                    entity = EntityType.GUARDIAN.a(world);
                }
            } else if (ArmorStand.class.isAssignableFrom(clazz)) {
                entity = new ArmorStandEntity(world, x, y, z);
            } else if (PolarBear.class.isAssignableFrom(clazz)) {
                entity = EntityType.POLAR_BEAR.a(world);
            } else if (Vex.class.isAssignableFrom(clazz)) {
                entity = EntityType.VEX.a(world);
            } else if (Illager.class.isAssignableFrom(clazz)) {
                if (Spellcaster.class.isAssignableFrom(clazz)) {
                    if (Evoker.class.isAssignableFrom(clazz)) {
                        entity = EntityType.EVOKER.a(world);
                    } else if (Illusioner.class.isAssignableFrom(clazz)) {
                        entity = EntityType.ILLUSIONER.a(world);
                    }
                } else if (Vindicator.class.isAssignableFrom(clazz)) {
                    entity = EntityType.VINDICATOR.a(world);
                } else if (Pillager.class.isAssignableFrom(clazz)) {
                    entity = EntityType.PILLAGER.a(world);
                }
            } else if (Turtle.class.isAssignableFrom(clazz)) {
                entity = EntityType.TURTLE.a(world);
            } else if (Phantom.class.isAssignableFrom(clazz)) {
                entity = EntityType.PHANTOM.a(world);
            } else if (Fish.class.isAssignableFrom(clazz)) {
                if (Cod.class.isAssignableFrom(clazz)) {
                    entity = EntityType.COD.a(world);
                } else if (PufferFish.class.isAssignableFrom(clazz)) {
                    entity = EntityType.PUFFERFISH.a(world);
                } else if (Salmon.class.isAssignableFrom(clazz)) {
                    entity = EntityType.SALMON.a(world);
                } else if (TropicalFish.class.isAssignableFrom(clazz)) {
                    entity = EntityType.TROPICAL_FISH.a(world);
                }
            } else if (Dolphin.class.isAssignableFrom(clazz)) {
                entity = EntityType.DOLPHIN.a(world);
            } else if (Ocelot.class.isAssignableFrom(clazz)) {
                entity = EntityType.OCELOT.a(world);
            } else if (Ravager.class.isAssignableFrom(clazz)) {
                entity = EntityType.RAVAGER.a(world);
            } else if (Panda.class.isAssignableFrom(clazz)) {
                entity = EntityType.PANDA.a(world);
            } else if (Fox.class.isAssignableFrom(clazz)) {
                entity = EntityType.FOX.a(world);
            } else if (Bee.class.isAssignableFrom(clazz)) {
                entity = EntityType.BEE.a(world);
            }

            if (entity != null) {
                entity.setLocation(x, y, z, yaw, pitch);
                entity.setHeadRotation(yaw); // SPIGOT-3587
            }
        } else if (Hanging.class.isAssignableFrom(clazz)) {
            BlockFace face = BlockFace.SELF;

            int width = 16; // 1 full block, also painting smallest size.
            int height = 16; // 1 full block, also painting smallest size.

            if (ItemFrame.class.isAssignableFrom(clazz)) {
                width = 12;
                height = 12;
            } else if (LeashHitch.class.isAssignableFrom(clazz)) {
                width = 9;
                height = 9;
            }

            BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
            final BlockPos pos = new BlockPos(x, y, z);
            for (BlockFace dir : faces) {
                BlockState nmsBlock = world.getType(pos.shift(CraftBlock.blockFaceToNotch(dir)));
                if (nmsBlock.getMaterial().isBuildable() || RedstoneDiodeBlock.isDiode(nmsBlock)) {
                    boolean taken = false;
                    AxisAlignedBB bb = (ItemFrame.class.isAssignableFrom(clazz))
                            ? ItemEntityFrame.calculateBoundingBox(null, pos, CraftBlock.blockFaceToNotch(dir).opposite(), width, height)
                            : HangingEntity.calculateBoundingBox(null, pos, CraftBlock.blockFaceToNotch(dir).opposite(), width, height);
                    List<net.minecraft.entity.Entity> list = (List<net.minecraft.entity.Entity>) world.getEntities(null, bb);
                    for (Iterator<net.minecraft.entity.Entity> it = list.iterator(); !taken && it.hasNext();) {
                        net.minecraft.entity.Entity e = it.next();
                        if (e instanceof HangingEntity) {
                            taken = true; // Hanging entities do not like hanging entities which intersect them.
                        }
                    }

                    if (!taken) {
                        face = dir;
                        break;
                    }
                }
            }

            if (LeashHitch.class.isAssignableFrom(clazz)) {
                entity = new LeashKnotEntity(world, new BlockPos(x, y, z));
                entity.attachedToPlayer = true;
            } else {
                // No valid face found
                Preconditions.checkArgument(face != BlockFace.SELF, "Cannot spawn hanging entity for %s at %s (no free face)", clazz.getName(), location);

                Direction dir = CraftBlock.blockFaceToNotch(face).opposite();
                if (Painting.class.isAssignableFrom(clazz)) {
                    entity = new PaintingEntity(world, new BlockPos(x, y, z), dir);
                } else if (ItemFrame.class.isAssignableFrom(clazz)) {
                    entity = new ItemEntityFrame(world, new BlockPos(x, y, z), dir);
                }
            }

            if (entity != null && !((HangingEntity) entity).survives()) {
                throw new IllegalArgumentException("Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
            }
        } else if (TNTPrimed.class.isAssignableFrom(clazz)) {
            entity = new TNTEntity(world, x, y, z, null);
        } else if (ExperienceOrb.class.isAssignableFrom(clazz)) {
            entity = new ExperienceOrbEntity(world, x, y, z, 0);
        } else if (LightningStrike.class.isAssignableFrom(clazz)) {
            entity = new LightningBoltEntity(world, x, y, z, false);
        } else if (Firework.class.isAssignableFrom(clazz)) {
            entity = new FireworkRocketEntity(world, x, y, z, net.minecraft.item.ItemStack.a);
        } else if (AreaEffectCloud.class.isAssignableFrom(clazz)) {
            entity = new AreaEffectCloudEntity(world, x, y, z);
        } else if (EvokerFangs.class.isAssignableFrom(clazz)) {
            entity = new EvokerFangsEntity(world, x, y, z, (float) Math.toRadians(yaw), 0, null);
        }

        if (entity != null) {
            return entity;
        }

        throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(net.minecraft.entity.Entity entity, SpawnReason reason) throws IllegalArgumentException {
        return addEntity(entity, reason, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T addEntity(net.minecraft.entity.Entity entity, SpawnReason reason, Consumer<T> function) throws IllegalArgumentException {
        Preconditions.checkArgument(entity != null, "Cannot spawn null entity");

        if (entity instanceof MobEntity) {
            ((MobEntity) entity).prepare(getHandle(), getHandle().getDamageScaler(new BlockPos(entity)), SpawnReason.COMMAND, (ILivingEntityData) null, null);
        }

        if (function != null) {
            function.accept((T) entity.getBukkitEntity());
        }

        world.addEntity(entity, reason);
        return (T) entity.getBukkitEntity();
    }

    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function, SpawnReason reason) throws IllegalArgumentException {
        net.minecraft.entity.Entity entity = createEntity(location, clazz);

        return addEntity(entity, reason, function);
    }

    @Override
    public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) {
        return CraftChunk.getEmptyChunkSnapshot(x, z, this, includeBiome, includeBiomeTempRain);
    }

    @Override
    public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
        world.setSpawnFlags(allowMonsters, allowAnimals);
    }

    @Override
    public boolean getAllowAnimals() {
        return world.getChunkProvider().allowAnimals;
    }

    @Override
    public boolean getAllowMonsters() {
        return world.getChunkProvider().allowMonsters;
    }

    @Override
    public int getMaxHeight() {
        return world.getBuildHeight();
    }

    @Override
    public int getSeaLevel() {
        return world.getSeaLevel();
    }

    @Override
    public boolean getKeepSpawnInMemory() {
        return world.keepSpawnInMemory;
    }

    @Override
    public void setKeepSpawnInMemory(boolean keepLoaded) {
        world.keepSpawnInMemory = keepLoaded;
        // Grab the worlds spawn chunk
        BlockPos chunkcoordinates = this.world.getSpawn();
        if (keepLoaded) {
            world.getChunkProvider().addTicket(TicketType.START, new ChunkPos(chunkcoordinates), 11, Unit.INSTANCE);
        } else {
            // TODO: doesn't work well if spawn changed....
            world.getChunkProvider().removeTicket(TicketType.START, new ChunkPos(chunkcoordinates), 11, Unit.INSTANCE);
        }
    }

    @Override
    public int hashCode() {
        return getUID().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final CraftWorld other = (CraftWorld) obj;

        return this.getUID() == other.getUID();
    }

    @Override
    public File getWorldFolder() {
        return world.getDataManager().getDirectory();
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);

        for (Player player : getPlayers()) {
            player.sendPluginMessage(source, channel, message);
        }
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        Set<String> result = new HashSet<String>();

        for (Player player : getPlayers()) {
            result.addAll(player.getListeningPluginChannels());
        }

        return result;
    }

    @Override
    public org.bukkit.WorldType getWorldType() {
        return org.bukkit.WorldType.getByName(world.getWorldData().getType().name());
    }

    @Override
    public boolean canGenerateStructures() {
        return world.getWorldData().shouldGenerateMapFeatures();
    }

    @Override
    public long getTicksPerAnimalSpawns() {
        return world.ticksPerAnimalSpawns;
    }

    @Override
    public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {
        world.ticksPerAnimalSpawns = ticksPerAnimalSpawns;
    }

    @Override
    public long getTicksPerMonsterSpawns() {
        return world.ticksPerMonsterSpawns;
    }

    @Override
    public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {
        world.ticksPerMonsterSpawns = ticksPerMonsterSpawns;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        server.getWorldMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return server.getWorldMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return server.getWorldMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        server.getWorldMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public int getMonsterSpawnLimit() {
        if (monsterSpawn < 0) {
            return server.getMonsterSpawnLimit();
        }

        return monsterSpawn;
    }

    @Override
    public void setMonsterSpawnLimit(int limit) {
        monsterSpawn = limit;
    }

    @Override
    public int getAnimalSpawnLimit() {
        if (animalSpawn < 0) {
            return server.getAnimalSpawnLimit();
        }

        return animalSpawn;
    }

    @Override
    public void setAnimalSpawnLimit(int limit) {
        animalSpawn = limit;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        if (waterAnimalSpawn < 0) {
            return server.getWaterAnimalSpawnLimit();
        }

        return waterAnimalSpawn;
    }

    @Override
    public void setWaterAnimalSpawnLimit(int limit) {
        waterAnimalSpawn = limit;
    }

    @Override
    public int getAmbientSpawnLimit() {
        if (ambientSpawn < 0) {
            return server.getAmbientSpawnLimit();
        }

        return ambientSpawn;
    }

    @Override
    public void setAmbientSpawnLimit(int limit) {
        ambientSpawn = limit;
    }

    @Override
    public void playSound(Location loc, Sound sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, String sound, float volume, float pitch) {
        playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        getHandle().playSound(null, x, y, z, CraftSound.getSoundEffect(CraftSound.getSound(sound)), SoundCategory.valueOf(category.name()), volume, pitch);
    }

    @Override
    public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (loc == null || sound == null || category == null) return;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        SPlaySoundPacket packet = new SPlaySoundPacket(new ResourceLocation(sound), SoundCategory.valueOf(category.name()), new Vec3d(x, y, z), volume, pitch);
        world.getMinecraftServer().getPlayerList().sendPacketNearby(null, x, y, z, volume > 1.0F ? 16.0F * volume : 16.0D, this.world.getDimension().getDimensionManager(), packet);
    }

    private static Map<String, GameRules.GameRuleKey<?>> gamerules;
    public static synchronized Map<String, GameRules.GameRuleKey<?>> getGameRulesNMS() {
        if (gamerules != null) {
            return gamerules;
        }

        Map<String, GameRules.GameRuleKey<?>> gamerules = new HashMap<>();
        GameRules.a(new GameRules.GameRuleVisitor() {
            @Override
            public <T extends GameRules.GameRuleValue<T>> void a(GameRules.GameRuleKey<T> gamerules_gamerulekey, GameRules.GameRuleDefinition<T> gamerules_gameruledefinition) {
                gamerules.put(gamerules_gamerulekey.a(), gamerules_gamerulekey);
            }
        });

        return CraftWorld.gamerules = gamerules;
    }

    private static Map<String, GameRules.GameRuleDefinition<?>> gameruleDefinitions;
    public static synchronized Map<String, GameRules.GameRuleDefinition<?>> getGameRuleDefinitions() {
        if (gameruleDefinitions != null) {
            return gameruleDefinitions;
        }

        Map<String, GameRules.GameRuleDefinition<?>> gameruleDefinitions = new HashMap<>();
        GameRules.a(new GameRules.GameRuleVisitor() {
            @Override
            public <T extends GameRules.GameRuleValue<T>> void a(GameRules.GameRuleKey<T> gamerules_gamerulekey, GameRules.GameRuleDefinition<T> gamerules_gameruledefinition) {
                gameruleDefinitions.put(gamerules_gamerulekey.a(), gamerules_gameruledefinition);
            }
        });

        return CraftWorld.gameruleDefinitions = gameruleDefinitions;
    }

    @Override
    public String getGameRuleValue(String rule) {
        // In method contract for some reason
        if (rule == null) {
            return null;
        }

        GameRules.GameRuleValue<?> value = getHandle().getGameRules().get(getGameRulesNMS().get(rule));
        return value != null ? value.toString() : "";
    }

    @Override
    public boolean setGameRuleValue(String rule, String value) {
        // No null values allowed
        if (rule == null || value == null) return false;

        if (!isGameRule(rule)) return false;

        GameRules.GameRuleValue<?> handle = getHandle().getGameRules().get(getGameRulesNMS().get(rule));
        handle.setValue(value);
        handle.onChange(getHandle().getMinecraftServer());
        return true;
    }

    @Override
    public String[] getGameRules() {
        return getGameRulesNMS().keySet().toArray(new String[getGameRulesNMS().size()]);
    }

    @Override
    public boolean isGameRule(String rule) {
        Validate.isTrue(rule != null && !rule.isEmpty(), "Rule cannot be null nor empty");
        return getGameRulesNMS().containsKey(rule);
    }

    @Override
    public <T> T getGameRuleValue(GameRule<T> rule) {
        Validate.notNull(rule, "GameRule cannot be null");
        return convert(rule, getHandle().getGameRules().get(getGameRulesNMS().get(rule.getName())));
    }

    @Override
    public <T> T getGameRuleDefault(GameRule<T> rule) {
        Validate.notNull(rule, "GameRule cannot be null");
        return convert(rule, getGameRuleDefinitions().get(rule.getName()).getValue());
    }

    @Override
    public <T> boolean setGameRule(GameRule<T> rule, T newValue) {
        Validate.notNull(rule, "GameRule cannot be null");
        Validate.notNull(newValue, "GameRule value cannot be null");

        if (!isGameRule(rule.getName())) return false;

        GameRules.GameRuleValue<?> handle = getHandle().getGameRules().get(getGameRulesNMS().get(rule.getName()));
        handle.setValue(newValue.toString());
        handle.onChange(getHandle().getMinecraftServer());
        return true;
    }

    private <T> T convert(GameRule<T> rule, GameRules.GameRuleValue<?> value) {
        if (value == null) {
            return null;
        }

        if (value instanceof GameRules.GameRuleBoolean) {
            return rule.getType().cast(((GameRules.GameRuleBoolean) value).a());
        } else if (value instanceof GameRules.GameRuleInt) {
            return rule.getType().cast(value.getIntValue());
        } else {
            throw new IllegalArgumentException("Invalid GameRule type (" + value + ") for GameRule " + rule.getName());
        }
    }

    @Override
    public WorldBorder getWorldBorder() {
        if (this.worldBorder == null) {
            this.worldBorder = new CraftWorldBorder(this);
        }

        return this.worldBorder;
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count) {
        spawnParticle(particle, x, y, z, count, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
        spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra);
    }

    @Override
    public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, false);
    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data, force);
    }

    @Override
    public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        if (data != null && !particle.getDataType().isInstance(data)) {
            throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
        }
        getHandle().sendParticleTypes(
                null, // Sender
                CraftParticle.toNMS(particle, data), // Particle
                x, y, z, // Position
                count,  // Count
                offsetX, offsetY, offsetZ, // Random offset
                extra, // Speed?
                force
        );

    }

    @Override
    public Location locateNearestStructure(Location origin, StructureType structureType, int radius, boolean findUnexplored) {
        BlockPos originPos = new BlockPos(origin.getX(), origin.getY(), origin.getZ());
        BlockPos nearest = getHandle().getChunkProvider().getChunkGenerator().findNearestMapFeature(getHandle(), structureType.getName(), originPos, radius, findUnexplored);
        return (nearest == null) ? null : new Location(this, nearest.getX(), nearest.getY(), nearest.getZ());
    }

    @Override
    public Raid locateNearestRaid(Location location, int radius) {
        Validate.notNull(location, "Location cannot be null");
        Validate.isTrue(radius >= 0, "Radius cannot be negative");

        PersistentRaid persistentRaid = world.getPersistentRaid();
        net.minecraft.world.raid.Raid raid = persistentRaid.getNearbyRaid(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), radius * radius);
        return (raid == null) ? null : new CraftRaid(raid);
    }

    @Override
    public List<Raid> getRaids() {
        PersistentRaid persistentRaid = world.getPersistentRaid();
        return persistentRaid.raids.values().stream().map(CraftRaid::new).collect(Collectors.toList());
    }
}