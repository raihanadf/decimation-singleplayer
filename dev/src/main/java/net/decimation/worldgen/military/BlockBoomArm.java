package net.decimation.worldgen.military;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Toll gate barrier arm (deciworldgen:boom_barrier; user, 14 Oktober 2026: the wasteland gates "use toll
 * gate"): a thin red and white striped bar, one per cell. Decimation has no such prop. Metadata 0 = lying
 * along x, 1 = along z (both at waist height), 2 = standing up (an arm raised). A plain block (no tile
 * entity): drawn by the standard renderer from its bounds.
 */
public final class BlockBoomArm extends Block
{
    public static BlockBoomArm instance;

    @SideOnly(Side.CLIENT)
    private IIcon up;

    public BlockBoomArm()
    {
        super(Material.iron);
        setHardness(2.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("deciworldgen.boom_barrier");
        setBlockTextureName("deciworldgen:boom_barrier");
        setCreativeTab(CreativeTabs.tabDecorations);
        setLightOpacity(0);
    }

    private void bounds(int meta)
    {
        switch (meta)
        {
            case 1:  setBlockBounds(0.4F, 0.62F, 0.0F, 0.6F, 0.82F, 1.0F); break;
            case 2:  setBlockBounds(0.4F, 0.0F, 0.4F, 0.6F, 1.0F, 0.6F); break;
            default: setBlockBounds(0.0F, 0.62F, 0.4F, 1.0F, 0.82F, 0.6F); break;
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess w, int x, int y, int z)
    {
        bounds(w.getBlockMetadata(x, y, z));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World w, int x, int y, int z)
    {
        bounds(w.getBlockMetadata(x, y, z));
        return super.getCollisionBoundingBoxFromPool(w, x, y, z);
    }

    @Override
    public void setBlockBoundsForItemRender()
    {
        bounds(0);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {
        blockIcon = reg.registerIcon("deciworldgen:boom_barrier");
        up = reg.registerIcon("deciworldgen:boom_barrier_up");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        return meta == 2 && side > 1 ? up : blockIcon;
    }
}
