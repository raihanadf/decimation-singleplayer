package net.decimation.worldgen.devtest;

import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * Driving a Decimation vehicle (docs/decimation_code/vehicles.md): a Hummer in the arena, the player in its driver seat
 * in survival with a jerry can in the hotbar. The client holds "forward" the way the keys do (a MovementInput that
 * always pushes forward, so its input packet carries it); the server's VehicleEntity.onUpdate does the driving.
 * Checks: the seat takes the player, the engine runs with fuel, the vehicle moves, fuel is used; then with the can
 * gone the engine stops and the vehicle coasts to a stop. Records the coast and the despawn countdown the HUD shows.
 */
public class DriveTest extends DevTestMode
{
    public String name() { return "drive"; }

    private static final int DRIVE = 40;
    private volatile int ticks, phase;   // 1 mounted, 2 driving with fuel, 3 driving without, 4 done
    private volatile boolean push;
    private MovementInput keys;
    private Entity hummer;
    private double startX, startZ, midX, midZ;
    private int fuelBefore, phaseAt;
    private WorldSettings.GameType modeBefore;

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            return true;
        }
        if (push && keys == null)
        {
            keys = mc.thePlayer.movementInput;
            mc.thePlayer.movementInput = new MovementInput()
            {
                @Override
                public void updatePlayerMoveState()
                {
                    moveForward = 1f;
                    moveStrafe = 0f;
                }
            };
        }
        if (!push && keys != null)
        {
            mc.thePlayer.movementInput = keys;
            keys = null;
        }
        return phase < 4;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null)
        {
            return;
        }
        World w = p.worldObj;
        if (t == 2)
        {
            DevTestArena.build(w, p);
            w.setWorldTime(6000);
            modeBefore = p.theItemInWorldManager.getGameType();
            p.setGameType(WorldSettings.GameType.SURVIVAL);
            p.capabilities.disableDamage = true;
            p.inventory.setInventorySlotContents(0, null);
            p.inventory.setInventorySlotContents(1, new ItemStack(Deci.jerryCan()));
            fuelBefore = Deci.itemAmount(p.inventory.getStackInSlot(1));
            hummer = Deci.newHummer(w, DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5);
            DevTestResults.check(name(), "hummer spawns", "", w.spawnEntityInWorld(hummer), "spawnEntityInWorld true");
            DevTestResults.value(name(), "fuel in the jerry can", String.valueOf(fuelBefore));
        }
        if (hummer == null)
        {
            return;
        }
        Entity seat = Deci.vehicleDriverSeat(hummer);
        if (phase == 0 && t >= 20 && seat != null)
        {
            p.mountEntity(seat);
            phase = 1;
            phaseAt = t;
        }
        if (phase == 0 && t > 120)
        {
            DevTestResults.check(name(), "driver seat", "none", false, "a driver seat after the first ticks");
            finish(p);
            return;
        }
        if (phase == 1 && t - phaseAt == 10)
        {
            DevTestResults.check(name(), "player in the driver seat", "", p.ridingEntity == seat && seat != null, "riding the driver seat");
            DevTestResults.value(name(), "despawn countdown shown", Deci.vehicleDespawnSecondsLeft(hummer) + " s");
            startX = hummer.posX;
            startZ = hummer.posZ;
            push = true;
            phase = 2;
            phaseAt = t;
        }
        if (phase == 2 && t - phaseAt == DRIVE)
        {
            double moved = Math.hypot(hummer.posX - startX, hummer.posZ - startZ);
            int fuel = Deci.itemAmount(p.inventory.getStackInSlot(1));
            DevTestResults.check(name(), "engine on with a jerry can", "", Deci.vehicleEngineOn(hummer), "engine on");
            DevTestResults.check(name(), "drives forward", String.format("%.1f blocks, speed %.2f", moved, Deci.vehicleSpeed(hummer)),
                                 moved > 3, "> 3 blocks in " + DRIVE + " ticks");
            DevTestResults.check(name(), "fuel used while driving", fuelBefore + " -> " + fuel, fuel < fuelBefore, "less fuel");
            p.inventory.setInventorySlotContents(1, null);
            midX = hummer.posX;
            midZ = hummer.posZ;
            phase = 3;
            phaseAt = t;
        }
        if (phase == 3 && t - phaseAt == DRIVE - 10)
        {
            startX = hummer.posX; // Decimation lets it coast: speed drains 0.05 a tick while speed / 10 is ADDED to the motion
            startZ = hummer.posZ;
        }
        if (phase == 3 && t - phaseAt == DRIVE)
        {
            double coast = Math.hypot(hummer.posX - midX, hummer.posZ - midZ);
            double last = Math.hypot(hummer.posX - startX, hummer.posZ - startZ);
            DevTestResults.check(name(), "no fuel: engine off", "", !Deci.vehicleEngineOn(hummer), "engine off");
            DevTestResults.value(name(), "no fuel: coasts before stopping", String.format("%.1f blocks", coast));
            DevTestResults.check(name(), "no fuel: comes to a stop", String.format("speed %.2f, %.2f blocks in the last 10 ticks",
                                 Deci.vehicleSpeed(hummer), last), Deci.vehicleSpeed(hummer) == 0 && last < 0.5, "speed 0, standing");
            finish(p);
        }
    }

    private void finish(EntityPlayerMP p)
    {
        push = false;
        if (p.ridingEntity != null)
        {
            p.mountEntity(null);
        }
        if (hummer != null)
        {
            hummer.setDead();
        }
        if (modeBefore != null)
        {
            p.setGameType(modeBefore);
        }
        phase = 4;
    }
}
