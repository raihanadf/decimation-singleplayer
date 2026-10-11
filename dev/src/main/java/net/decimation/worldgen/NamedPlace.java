package net.decimation.worldgen;

/**
 * A plan with a place name (docs/zones.md "Place names"): when its first slice is written, Slices
 * stores its box with this name in StructureData, and PlaceNames sends it to players as a
 * Decimation turf ("ENTERING LOCATION ...").
 */
public interface NamedPlace
{
    /** The name as shown (spaces allowed; sent with underscores). */
    String placeName();

    /** Who holds it ("unknown" shows no "controlled by"). */
    String placeOwner();
}
