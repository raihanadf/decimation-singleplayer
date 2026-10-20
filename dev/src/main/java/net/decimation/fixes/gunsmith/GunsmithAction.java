package net.decimation.fixes.gunsmith;

/**
 * A click in the Gunsmith screen's list, sent to the server as a container button id (Container.enchantItem, the way
 * vanilla's enchanting table sends its buttons), so no network channel of our own is needed. Fit: 1000 + slot * 100 +
 * inventory index (0 .. 35); take off: 2000 + slot. The server decodes and checks every id. Plain Java.
 */
public final class GunsmithAction
{
    private static final int FIT = 1000, TAKE_OFF = 2000, SLOTS = 100, INVENTORY = 36;

    public final boolean takeOff;
    public final GunSlot slot;
    public final int invIndex;

    private GunsmithAction(boolean takeOff, GunSlot slot, int invIndex)
    {
        this.takeOff = takeOff;
        this.slot = slot;
        this.invIndex = invIndex;
    }

    /** Fit one item from player inventory index invIndex (0 .. 35) into slot s. */
    public static int fit(GunSlot s, int invIndex)
    {
        return FIT + s.ordinal() * SLOTS + invIndex;
    }

    /** Take the part off slot s, back into the inventory. */
    public static int takeOff(GunSlot s)
    {
        return TAKE_OFF + s.ordinal();
    }

    /** The action behind a button id, or null for anything that is not one (or takes off a used up part). */
    public static GunsmithAction decode(int id)
    {
        GunSlot[] slots = GunSlot.values();
        if (id >= FIT && id < FIT + slots.length * SLOTS)
        {
            int slot = (id - FIT) / SLOTS, inv = (id - FIT) % SLOTS;
            return inv < INVENTORY ? new GunsmithAction(false, slots[slot], inv) : null;
        }
        if (id >= TAKE_OFF && id < TAKE_OFF + slots.length && !slots[id - TAKE_OFF].usedUp())
        {
            return new GunsmithAction(true, slots[id - TAKE_OFF], -1);
        }
        return null;
    }
}
