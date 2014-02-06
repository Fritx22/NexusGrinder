package net.coasterman10.Annihilation;

import org.bukkit.ChatColor;

/**
 * Created by morpig on 2/6/14.
 */
public class Translation {

    public static String _ (String id) {
        return ChatColor.stripColor(Annihilation.messages.get(id));
    }

}
