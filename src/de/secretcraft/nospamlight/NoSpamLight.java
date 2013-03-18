package de.secretcraft.nospamlight;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import de.secretcraft.nospamlight.listener.ChatListener;

public class NoSpamLight extends JavaPlugin {
	private ChatListener spamGuard = null;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		spamGuard = new ChatListener(this);
		getServer().getPluginManager().registerEvents(spamGuard, this);
	}

	@Override
	public void onDisable() {

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
		if(args.length < 1)
			return false;
		if(args[0].equals("reload")) {
			if(!hasCommandPermission(sender, "reload"))
				sender.sendMessage(ChatColor.DARK_RED + "You don't have permission.");
			spamGuard.load();
			sender.sendMessage(ChatColor.DARK_AQUA + "[NoSpamLight] loaded config.");
			return true;
		}
		return false;
	}
	
	private boolean hasCommandPermission(CommandSender sender, String cmd) {
		return sender.hasPermission("nospamlight.command." + cmd) || sender.isOp();
	}
}
