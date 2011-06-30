package net.minecraft.server;

public class EntityZombie extends EntityMonster {

    public EntityZombie(World world) {
        super(world);
        this.texture = "/mob/zombie.png";
        this.aE = 0.5F;
        this.damage = 5;
    }

    public void v() {
        if (this.world.d()) {
            float f = this.c(1.0F);

            if (f > 0.5F && this.world.isChunkLoaded(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ)) && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
                this.fireTicks = 300;
            }
        }

        super.v();
    }

    protected String g() {
        return "mob.zombie";
    }

    protected String h() {
        return "mob.zombiehurt";
    }

    protected String i() {
        return "mob.zombiedeath";
    }

    protected int j() {
        return Item.FEATHER.id;
    }
}
