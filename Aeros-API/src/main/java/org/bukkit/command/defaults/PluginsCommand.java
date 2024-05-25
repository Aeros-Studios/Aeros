package org.bukkit.command.defaults;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import dev.aeros.jar.AerosAPI;

public class PluginsCommand extends BukkitCommand {
    public PluginsCommand(String name) {
        super(name);
        this.description = "Gets a list of plugins running on the server";
        this.usageMessage = "/plugins";
        this.setPermission("bukkit.command.plugins");
        this.setAliases(Arrays.asList("pl"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

        int contador = 0;

        sender.sendMessage(AerosAPI.Separador);
        if (plugins.length > 0) {
            sender.sendMessage(AerosAPI.Cor_Texto + "The server got " + AerosAPI.Cor_Primario + plugins.length + AerosAPI.Cor_Texto + " plugins.");
            for (Plugin plugin : plugins) {
                sender.sendMessage(
                        ChatColor.DARK_GRAY.toString() + (contador + 1 == plugins.length ? "  └ " : "  ├ ") +
                                (plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + plugin.getName() +
                                AerosAPI.Cor_Texto + " (" + AerosAPI.Cor_Primario + "v" + AerosAPI.Cor_Secundario + plugin.getDescription().getVersion() + AerosAPI.Cor_Texto + ")"
                );
                contador++;
            }
        } else {
            sender.sendMessage(AerosAPI.Cor_Texto + "Could not find any " + AerosAPI.Cor_Primario + "loaded" + AerosAPI.Cor_Secundario + " plugin" + AerosAPI.Cor_Texto + " on this server.");
        }

        sender.sendMessage(AerosAPI.Separador);
        return true;
    }

    // Spigot Start
    @Override
    public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException
    {
        return java.util.Collections.emptyList();
    }
    // Spigot End
}
