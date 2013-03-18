package de.secretcraft.nospamlight;

import java.util.HashSet;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Blacklist {
	private JavaPlugin plugin = null;
	private HashSet<String> blacklist = new HashSet<String>();

	public Blacklist(JavaPlugin plugin) {
		this.plugin = plugin;
		this.load();
	}

	public void load() {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		blacklist.clear();
		blacklist.addAll(config.getStringList("blacklist"));
	}

	/**
	 * @param message
	 * @return the first black listed word in the message.<br>
	 *         <code>null</code> if no black listed word was found.
	 */
	public String getBlacklistedWord(String message) {
		String msg = getOnlyLetters(message);
		for (String str : blacklist) {
			if (msg.contains(str))
				return str;
		}
		return null;
	}

	private String getOnlyLetters(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == ' ') {
				sb.append(Character.toLowerCase(c));
			}
		}
		return sb.toString();
	}
}
