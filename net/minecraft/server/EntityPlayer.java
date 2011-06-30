package net.minecraft.server;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class EntityPlayer extends EntityHuman implements ICrafting {

    public NetServerHandler netServerHandler;
    public MinecraftServer b;
    public ItemInWorldManager itemInWorldManager;
    public double d;
    public double e;
    public List chunkCoordIntPairQueue = new LinkedList();
    public Set playerChunkCoordIntPairs = new HashSet();
    private int bL = -99999999;
    private int bM = 60;
    private ItemStack[] bN = new ItemStack[] { null, null, null, null, null};
    private int bO = 0;
    public boolean h;

    public EntityPlayer(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager) {
        super(world);
        iteminworldmanager.player = this;
        this.itemInWorldManager = iteminworldmanager;
        ChunkCoordinates chunkcoordinates = world.getSpawn();
        int i = chunkcoordinates.x;
        int j = chunkcoordinates.z;
        int k = chunkcoordinates.y;

        if (!world.worldProvider.e) {
            i += this.random.nextInt(20) - 10;
            k = world.f(i, j);
            j += this.random.nextInt(20) - 10;
        }

        this.setPositionRotation((double) i + 0.5D, (double) k, (double) j + 0.5D, 0.0F, 0.0F);
        this.b = minecraftserver;
        this.bs = 0.0F;
        this.name = s;
        this.height = 0.0F;
    }

    public void spawnIn(World world) {
        super.spawnIn(world);
        this.itemInWorldManager = new ItemInWorldManager((WorldServer) world);
        this.itemInWorldManager.player = this;
    }

    public void syncInventory() {
        this.activeContainer.a((ICrafting) this);
    }

    public ItemStack[] getEquipment() {
        return this.bN;
    }

    protected void s() {
        this.height = 0.0F;
    }

    public float t() {
        return 1.62F;
    }

    public void m_() {
        this.itemInWorldManager.a();
        --this.bM;
        this.activeContainer.a();

        for (int i = 0; i < 5; ++i) {
            ItemStack itemstack = this.c_(i);

            if (itemstack != this.bN[i]) {
                this.b.getTracker(this.dimension).a(this, new Packet5EntityEquipment(this.id, i, itemstack));
                this.bN[i] = itemstack;
            }
        }
    }

    public ItemStack c_(int i) {
        return i == 0 ? this.inventory.getItemInHand() : this.inventory.armor[i - 1];
    }

    public void die(Entity entity) {
        this.inventory.h();
    }

    public boolean damageEntity(Entity entity, int i) {
        if (this.bM > 0) {
            return false;
        } else {
            if (!this.b.pvpMode) {
                if (entity instanceof EntityHuman) {
                    return false;
                }

                if (entity instanceof EntityArrow) {
                    EntityArrow entityarrow = (EntityArrow) entity;

                    if (entityarrow.shooter instanceof EntityHuman) {
                        return false;
                    }
                }
            }

            return super.damageEntity(entity, i);
        }
    }

    protected boolean j_() {
        return this.b.pvpMode;
    }

    public void b(int i) {
        super.b(i);
    }

    public void a(boolean flag) {
        super.m_();

        for (int i = 0; i < this.inventory.getSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);

            if (itemstack != null && Item.byId[itemstack.id].b() && this.netServerHandler.b() <= 2) {
                Packet packet = ((ItemWorldMapBase) Item.byId[itemstack.id]).b(itemstack, this.world, this);

                if (packet != null) {
                    this.netServerHandler.sendPacket(packet);
                }
            }
        }

        if (flag && !this.chunkCoordIntPairQueue.isEmpty()) {
            ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) this.chunkCoordIntPairQueue.get(0);

            if (chunkcoordintpair != null) {
                boolean flag1 = false;

                if (this.netServerHandler.b() < 4) {
                    flag1 = true;
                }

                if (flag1) {
                    WorldServer worldserver = this.b.getWorldServer(this.dimension);

                    this.chunkCoordIntPairQueue.remove(chunkcoordintpair);
                    this.netServerHandler.sendPacket(new Packet51MapChunk(chunkcoordintpair.x * 16, 0, chunkcoordintpair.z * 16, 16, 128, 16, worldserver));
                    List list = worldserver.getTileEntities(chunkcoordintpair.x * 16, 0, chunkcoordintpair.z * 16, chunkcoordintpair.x * 16 + 16, 128, chunkcoordintpair.z * 16 + 16);

                    for (int j = 0; j < list.size(); ++j) {
                        this.a((TileEntity) list.get(j));
                    }
                }
            }
        }

        if (this.E) {
            if (this.b.propertyManager.getBoolean("allow-nether", true)) {
                if (this.vehicle != null) {
                    this.mount(this.vehicle);
                } else {
                    this.F += 0.0125F;
                    if (this.F >= 1.0F) {
                        this.F = 1.0F;
                        this.D = 10;
                        this.b.serverConfigurationManager.f(this);
                    }
                }

                this.E = false;
            }
        } else {
            if (this.F > 0.0F) {
                this.F -= 0.05F;
            }

            if (this.F < 0.0F) {
                this.F = 0.0F;
            }
        }

        if (this.D > 0) {
            --this.D;
        }

        if (this.health != this.bL) {
            this.netServerHandler.sendPacket(new Packet8UpdateHealth(this.health));
            this.bL = this.health;
        }
    }

    private void a(TileEntity tileentity) {
        if (tileentity != null) {
            Packet packet = tileentity.f();

            if (packet != null) {
                this.netServerHandler.sendPacket(packet);
            }
        }
    }

    public void v() {
        super.v();
    }

    public void receive(Entity entity, int i) {
        if (!entity.dead) {
            EntityTracker entitytracker = this.b.getTracker(this.dimension);

            if (entity instanceof EntityItem) {
                entitytracker.a(entity, new Packet22Collect(entity.id, this.id));
            }

            if (entity instanceof EntityArrow) {
                entitytracker.a(entity, new Packet22Collect(entity.id, this.id));
            }
        }

        super.receive(entity, i);
        this.activeContainer.a();
    }

    public void w() {
        if (!this.p) {
            this.q = -1;
            this.p = true;
            EntityTracker entitytracker = this.b.getTracker(this.dimension);

            entitytracker.a(this, new Packet18ArmAnimation(this, 1));
        }
    }

    public void x() {}

    public EnumBedError a(int i, int j, int k) {
        EnumBedError enumbederror = super.a(i, j, k);

        if (enumbederror == EnumBedError.OK) {
            EntityTracker entitytracker = this.b.getTracker(this.dimension);
            Packet17 packet17 = new Packet17(this, 0, i, j, k);

            entitytracker.a(this, packet17);
            this.netServerHandler.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            this.netServerHandler.sendPacket(packet17);
        }

        return enumbederror;
    }

    public void a(boolean flag, boolean flag1, boolean flag2) {
        if (this.isSleeping()) {
            EntityTracker entitytracker = this.b.getTracker(this.dimension);

            entitytracker.sendPacketToEntity(this, new Packet18ArmAnimation(this, 3));
        }

        super.a(flag, flag1, flag2);
        if (this.netServerHandler != null) {
            this.netServerHandler.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        }
    }

    public void mount(Entity entity) {
        super.mount(entity);
        this.netServerHandler.sendPacket(new Packet39AttachEntity(this, this.vehicle));
        this.netServerHandler.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
    }

    protected void a(double d0, boolean flag) {}

    public void b(double d0, boolean flag) {
        super.a(d0, flag);
    }

    private void ai() {
        this.bO = this.bO % 100 + 1;
    }

    public void b(int i, int j, int k) {
        this.ai();
        this.netServerHandler.sendPacket(new Packet100OpenWindow(this.bO, 1, "Crafting", 9));
        this.activeContainer = new ContainerWorkbench(this.inventory, this.world, i, j, k);
        this.activeContainer.windowId = this.bO;
        this.activeContainer.a((ICrafting) this);
    }

    public void a(IInventory iinventory) {
        this.ai();
        this.netServerHandler.sendPacket(new Packet100OpenWindow(this.bO, 0, iinventory.getName(), iinventory.getSize()));
        this.activeContainer = new ContainerChest(this.inventory, iinventory);
        this.activeContainer.windowId = this.bO;
        this.activeContainer.a((ICrafting) this);
    }

    public void a(TileEntityFurnace tileentityfurnace) {
        this.ai();
        this.netServerHandler.sendPacket(new Packet100OpenWindow(this.bO, 2, tileentityfurnace.getName(), tileentityfurnace.getSize()));
        this.activeContainer = new ContainerFurnace(this.inventory, tileentityfurnace);
        this.activeContainer.windowId = this.bO;
        this.activeContainer.a((ICrafting) this);
    }

    public void a(TileEntityDispenser tileentitydispenser) {
        this.ai();
        this.netServerHandler.sendPacket(new Packet100OpenWindow(this.bO, 3, tileentitydispenser.getName(), tileentitydispenser.getSize()));
        this.activeContainer = new ContainerDispenser(this.inventory, tileentitydispenser);
        this.activeContainer.windowId = this.bO;
        this.activeContainer.a((ICrafting) this);
    }

    public void a(Container container, int i, ItemStack itemstack) {
        if (!(container.b(i) instanceof SlotResult)) {
            if (!this.h) {
                this.netServerHandler.sendPacket(new Packet103SetSlot(container.windowId, i, itemstack));
            }
        }
    }

    public void updateInventory(Container container) {
        this.a(container, container.b());
    }

    public void a(Container container, List list) {
        this.netServerHandler.sendPacket(new Packet104WindowItems(container.windowId, list));
        this.netServerHandler.sendPacket(new Packet103SetSlot(-1, -1, this.inventory.j()));
    }

    public void a(Container container, int i, int j) {
        this.netServerHandler.sendPacket(new Packet105CraftProgressBar(container.windowId, i, j));
    }

    public void a(ItemStack itemstack) {}

    public void y() {
        this.netServerHandler.sendPacket(new Packet101CloseWindow(this.activeContainer.windowId));
        this.A();
    }

    public void z() {
        if (!this.h) {
            this.netServerHandler.sendPacket(new Packet103SetSlot(-1, -1, this.inventory.j()));
        }
    }

    public void A() {
        this.activeContainer.a((EntityHuman) this);
        this.activeContainer = this.defaultContainer;
    }

    public void a(float f, float f1, boolean flag, boolean flag1, float f2, float f3) {
        this.az = f;
        this.aA = f1;
        this.aC = flag;
        this.setSneak(flag1);
        this.pitch = f2;
        this.yaw = f3;
    }

    public void a(Statistic statistic, int i) {
        if (statistic != null) {
            if (!statistic.g) {
                while (i > 100) {
                    this.netServerHandler.sendPacket(new Packet200Statistic(statistic.e, 100));
                    i -= 100;
                }

                this.netServerHandler.sendPacket(new Packet200Statistic(statistic.e, i));
            }
        }
    }

    public void B() {
        if (this.vehicle != null) {
            this.mount(this.vehicle);
        }

        if (this.passenger != null) {
            this.passenger.mount(this);
        }

        if (this.sleeping) {
            this.a(true, false, false);
        }
    }

    public void C() {
        this.bL = -99999999;
    }

    public void a(String s) {
        StatisticStorage statisticstorage = StatisticStorage.a();
        String s1 = statisticstorage.a(s);

        this.netServerHandler.sendPacket(new Packet3Chat(s1));
    }
}
