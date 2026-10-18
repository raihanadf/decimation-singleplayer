package net.decimation.fixes.hosting;

import java.util.List;

/**
 * The texts CommunityList draws on Decimation's Community page (plain Java: tests run without Minecraft's language
 * files): the player count (user, 18 Oktober 2026: players only, no world count) and the placeholder of an empty list.
 */
final class CommunityText
{
    static String headline(List<ListEntry> shown)
    {
        int players = 0;
        for (ListEntry w : shown)
        {
            players += w.players + 1; // the host plays too
        }
        return "§a● §fPlayers Online: §a" + players;
    }

    static String[] empty(List<ListEntry> shown)
    {
        if (!shown.isEmpty())
        {
            return new String[0];
        }
        return new String[] {"§fNo worlds online right now",
                             "§7Host yours: pause menu, Host Online",
                             "§7and it shows up here for everyone"};
    }
}
