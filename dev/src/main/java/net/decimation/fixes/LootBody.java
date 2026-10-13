package net.decimation.fixes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

/**
 * A lootable body (docs/loot_levels.md section 4, docs/systems_plan.md 2.3): an entity holding an
 * 18 slot inventory; a right click opens Decimation's loot screen on it (Loot.open). It goes when
 * emptied or after LIFETIME ticks. Spike version: no model of its own yet (drawn as the default box).
 */
public class LootBody extends Entity
{
    public static final int SLOTS = 18;
    /** 5 minutes. */
    public static final int LIFETIME = 6000;

    public final InventoryBasic inventory = new InventoryBasic("Loot", true, SLOTS);
    private int age;

    public LootBody(World world)
    {
        super(world);
        setSize(0.9f, 0.4f);
    }

    protected void entityInit()
    {
    }

    public boolean canBeCollidedWith()
    {
        return !isDead;
    }

    public boolean interactFirst(EntityPlayer player)
    {
        if (!worldObj.isRemote && player instanceof EntityPlayerMP)
        {
            Loot.open((EntityPlayerMP) player, inventory);
        }
        return true;
    }

    /** Stacks still in the body. */
    public int stacks()
    {
        int n = 0;
        for (int i = 0; i < SLOTS; i++)
        {
            n += inventory.getStackInSlot(i) != null ? 1 : 0;
        }
        return n;
    }

    public void onUpdate()
    {
        super.onUpdate();
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        motionY -= 0.04;
        moveEntity(0, motionY, 0);
        motionY *= 0.98;
        if (onGround)
        {
            motionY = 0;
        }
        if (!worldObj.isRemote && (++age > LIFETIME || age % 20 == 0 && stacks() == 0))
        {
            setDead();
        }
    }

    protected void readEntityFromNBT(NBTTagCompound tag)
    {
        age = tag.getInteger("Age");
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound t = list.getCompoundTagAt(i);
            int slot = t.getByte("Slot") & 255;
            if (slot < SLOTS)
            {
                inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(t));
            }
        }
    }

    protected void writeEntityToNBT(NBTTagCompound tag)
    {
        tag.setInteger("Age", age);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SLOTS; i++)
        {
            ItemStack s = inventory.getStackInSlot(i);
            if (s != null)
            {
                NBTTagCompound t = new NBTTagCompound();
                t.setByte("Slot", (byte) i);
                s.writeToNBT(t);
                list.appendTag(t);
            }
        }
        tag.setTag("Items", list);
    }
}
