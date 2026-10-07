package com.sofe.entity.army;

import com.sofe.entity.summon.Ally;
import com.sofe.world.region.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * A soldier of an empire's army (docs/Ejercitos.md): a soldier, an archer or a captain, dressed in their empire's
 * colors (textures/entity/npc/&lt;empire&gt;_&lt;rank&gt;.png, scripts/make_npc_skins.py). A garrison soldier keeps
 * to their district and fights the Void and the monsters that come into it, never a boss (that fight is the
 * Bearers'); a soldier hired from a captain follows a Bearer and fights what they fight. Talking to a captain
 * opens their dialogue: hire a soldier or an archer, or bring back the fallen.
 */
public class SoldierEntity extends PathfinderMob implements RangedAttackMob, Ally {
    public enum Rank {
        SOLDIER(30, 4, 0.30), ARCHER(24, 3, 0.30), CAPTAIN(50, 6, 0.30);

        public final double health, attack, speed;

        Rank(double health, double attack, double speed) {
            this.health = health;
            this.attack = attack;
            this.speed = speed;
        }

        public String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Rank byId(String id) {
            for (Rank r : values()) if (r.id().equals(id)) return r;
            return SOLDIER;
        }
    }

    /** How far from their post a garrison soldier goes, and how far they see a foe. */
    private static final int POST_RADIUS = 16, SIGHT = 16;
    private static final EntityDataAccessor<String> EMPIRE = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> RANK = SynchedEntityData.defineId(SoldierEntity.class, EntityDataSerializers.STRING);

    private UUID owner;
    private LivingEntity commanded;
    private int commandTicks;
    /** A hired soldier's place in their Bearer's company (Army.Company). */
    private int companySlot = -1;
    /** Made when first needed: Minecraft registers the goals from inside the constructor, before the fields are set. */
    private RangedBowAttackGoal<SoldierEntity> bowGoal;
    private MeleeAttackGoal meleeGoal;

    public SoldierEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, Rank.SOLDIER.health).add(Attributes.ATTACK_DAMAGE, Rank.SOLDIER.attack)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 24).add(Attributes.ARMOR, 4);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EMPIRE, Region.SULTHARI.id());
        entityData.define(RANK, Rank.SOLDIER.id());
    }

    /** Dresses and arms the soldier for their empire and rank; their health and blows follow the rank. */
    public void enlist(Region empire, Rank rank) {
        entityData.set(EMPIRE, empire.id());
        entityData.set(RANK, rank.id());
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(rank.health);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(rank.attack);
        setHealth(getMaxHealth());
        setItemSlot(EquipmentSlot.MAINHAND, weapon(empire, rank));
        setDropChance(EquipmentSlot.MAINHAND, 0f);
        setCustomName(net.minecraft.network.chat.Component.translatable("entity.sofe.soldier." + empire.id() + "." + rank.id()));
        reassessWeaponGoal();
    }

    /** The weapon each rank of each empire carries: archers a bow, the rest their empire's brass blade. */
    static ItemStack weapon(Region empire, Rank rank) {
        if (rank == Rank.ARCHER) return new ItemStack(Items.BOW);
        String id = switch (empire) {
            case NORDRATH -> rank == Rank.CAPTAIN ? "sofe:glacial_iron_sword" : "minecraft:iron_axe";
            case KHEMET -> rank == Rank.CAPTAIN ? "sofe:khopesh" : "sofe:brass_scimitar";
            case AUREUM -> rank == Rank.CAPTAIN ? "sofe:brass_longsword" : "minecraft:iron_sword";
            case PARSIVAN -> rank == Rank.CAPTAIN ? "sofe:brass_scimitar" : "minecraft:iron_sword";
            default -> rank == Rank.CAPTAIN ? "sofe:brass_scimitar" : "sofe:brass_longsword";
        };
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return new ItemStack(item == null || item == Items.AIR ? Items.IRON_SWORD : item);
    }

    public Region empire() {
        return Region.byId(entityData.get(EMPIRE)).orElse(Region.SULTHARI);
    }

    public Rank rank() {
        return Rank.byId(entityData.get(RANK));
    }

    /** The skin: textures/entity/npc/&lt;empire&gt;_&lt;rank&gt;.png. */
    public String skinId() {
        return entityData.get(EMPIRE) + "_" + entityData.get(RANK);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new Ally.FollowOwner<>(this));
        goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.8));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6, 0.01f));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new Ally.OwnersFoe<>(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this, SoldierEntity.class, Player.class) {
            @Override
            public boolean canUse() {
                return super.canUse() && !(getLastHurtByMob() instanceof Player) && !(getLastHurtByMob() instanceof Ally);
            }
        });
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::isFoe));
        reassessWeaponGoal();
    }

    /** The Void and the monsters; a garrison leaves the bosses to the Bearers. */
    private boolean isFoe(LivingEntity e) {
        if (!(e instanceof Enemy) || e instanceof Ally) return false;
        if (owner == null && e instanceof com.sofe.entity.boss.SoFEBossEntity) return false;
        return distanceToSqr(e) <= SIGHT * SIGHT;
    }

    /** An archer shoots, the others close in with the blade. */
    private void reassessWeaponGoal() {
        if (level() == null || level().isClientSide) return;
        if (bowGoal == null) {
            bowGoal = new RangedBowAttackGoal<>(this, 1.0, 25, 15f);
            meleeGoal = new MeleeAttackGoal(this, 1.15, true);
        }
        goalSelector.removeGoal(bowGoal);
        goalSelector.removeGoal(meleeGoal);
        if (getMainHandItem().is(Items.BOW)) goalSelector.addGoal(2, bowGoal);
        else goalSelector.addGoal(2, meleeGoal);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        ItemStack arrowStack = new ItemStack(Items.ARROW);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, arrowStack, power);
        arrow.setBaseDamage(rank().attack * 0.6);
        double dx = target.getX() - getX(), dz = target.getZ() - getZ();
        double dy = target.getY(0.3333) - arrow.getY();
        arrow.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 1.6f, 6f);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        playSound(SoundEvents.SKELETON_SHOOT, 1f, 1f / (getRandom().nextFloat() * 0.4f + 0.8f));
        level().addFreshEntity(arrow);
    }

    /** A soldier's arrows and blows never harm a Bearer, a citizen or another soldier. */
    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Player) && !(target instanceof Ally) && !(target instanceof com.sofe.entity.npc.StoryNpcEntity) && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player || source.getEntity() instanceof Ally) return false; // no friendly blows
        return super.hurt(source, amount);
    }

    /** Talking to a captain: hire soldiers or bring back the fallen (data/sofe/dialogue/army). */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (rank() != Rank.CAPTAIN || owner != null || hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
        if (player instanceof ServerPlayer server) {
            getNavigation().stop();
            getLookControl().setLookAt(player);
            com.sofe.quest.DialogueService.open(server, "sofe:army/" + empire().id() + "_captain", getId());
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide && owner != null) Army.onHiredFell(this);
    }

    // --- a garrison's post, a hired soldier's Bearer

    public void post(BlockPos at) {
        restrictTo(at, POST_RADIUS);
    }

    public void hire(ServerPlayer bearer, int slot) {
        owner = bearer.getUUID();
        companySlot = slot;
        clearRestriction();
    }

    public int companySlot() {
        return companySlot;
    }

    @Override
    public UUID owner() {
        return owner;
    }

    @Override
    public void command(LivingEntity target, int ticks) {
        commanded = target;
        commandTicks = ticks;
    }

    @Override
    public LivingEntity commanded() {
        return commandTicks > 0 && commanded != null && commanded.isAlive() ? commanded : null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (commandTicks > 0) commandTicks--;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("empire", entityData.get(EMPIRE));
        tag.putString("rank", entityData.get(RANK));
        if (owner != null) tag.putUUID("owner", owner);
        tag.putInt("company_slot", companySlot);
        if (hasRestriction()) tag.put("post", NbtUtils.writeBlockPos(getRestrictCenter()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(EMPIRE, tag.getString("empire"));
        entityData.set(RANK, tag.getString("rank"));
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        companySlot = tag.contains("company_slot") ? tag.getInt("company_slot") : -1;
        if (tag.contains("post")) post(NbtUtils.readBlockPos(tag.getCompound("post")));
        reassessWeaponGoal();
    }
}
