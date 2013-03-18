package de.secretcraft.nospamlight.listener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.secretcraft.nospamlight.Blacklist;
import de.secretcraft.nospamlight.PlayerSpamData;
import de.secretcraft.nospamlight.SpamRuleViolation;

public class ChatListener implements Listener {
	private ConcurrentHashMap<String, PlayerSpamData> spamDataMap = new ConcurrentHashMap<String, PlayerSpamData>();
	private JavaPlugin plugin = null;
	private Blacklist blacklist = null;
	private int warningsBeforeKick = 0;
	
	private String KICK_MESSAGE = null;
	private String UPPERCASE_WARNING = null;
	private String BLACKLIST_WARNING = null;

	public ChatListener(JavaPlugin plugin) {
		this.plugin = plugin;
		this.load();
	}
	
	public void load() {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		try {
			config.load("./plugins/NoSpamLight/config.yml");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		warningsBeforeKick = config.getInt("warningsBeforeKick");
		KICK_MESSAGE = ChatColor.translateAlternateColorCodes('&', config.getString("KICK_MESSAGE"));
		UPPERCASE_WARNING = ChatColor.translateAlternateColorCodes('&', config.getString("UPPERCASE_WARNING"));
		BLACKLIST_WARNING = ChatColor.translateAlternateColorCodes('&', config.getString("BLACKLIST_WARNING"));
		
		PlayerSpamData.milliSecondsToRemoveWarnings = config.getLong("milliSecondsToRemoveWarnings");
		PlayerSpamData.minDelayBetweenMessages = config.getLong("minDelayBetweenMessages");
		PlayerSpamData.minDelayBetweenSameMessages = config.getLong("minDelayBetweenSameMessages");
		
		PlayerSpamData.MIN_WAIT_BETWEEN_MESSAGES =
				ChatColor.translateAlternateColorCodes('&', config.getString("MIN_WAIT_BETWEEN_MESSAGES"));
		PlayerSpamData.MIN_WAIT_BETWEEN_SAME_MESSAGES =
				ChatColor.translateAlternateColorCodes('&', config.getString("MIN_WAIT_BETWEEN_SAME_MESSAGES"));
		
		blacklist = new Blacklist(plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;
		
		String msg = event.getMessage();

		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if (player.hasPermission("nospamlight.exception") || player.isOp()) {
			return;
		}

		PlayerSpamData spamData = spamDataMap.get(playerName);
		if(spamData == null) {
			spamData = new PlayerSpamData();
			spamDataMap.put(playerName, spamData);
		}
		
		try {
			spamData.timeStampCheck(msg);

			if (msg.length() > 6) {
				spamData.timeSameMessageCheck(msg);
			}

			// check for UPPER case
			if (msg.length() > 6) {
				int capsCount = 0;
				for (int i = 0; i < msg.length(); i++) {
					if (msg.charAt(i) <= 'Z' && msg.charAt(i) >= 'A')
						capsCount++;
				}
				if (capsCount > msg.length() / 2) {
					throw new SpamRuleViolation(UPPERCASE_WARNING);
				}
			}

			// check the blacklist
			String unallowedWord = blacklist.getBlacklistedWord(msg);
			if (unallowedWord != null) {
				throw SpamRuleViolation.create(BLACKLIST_WARNING, unallowedWord);
			}
			
			// important, do not remove
			spamData.setLastMessage(msg);
		} catch (SpamRuleViolation e) {
			if(spamData.warn() > warningsBeforeKick) {
				kickPlayerSync(playerName, KICK_MESSAGE);
			} else {
				sendMessageSync(playerName, e.getMessage());
			}
			event.setCancelled(true);
			return;
		}
	}

	private void kickPlayerSync(final String player, final String msg) {
		spamDataMap.remove(player);
		plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						try {
							Bukkit.getPlayerExact(player).kickPlayer(msg);
						} catch(Exception e) {}
					}
				}, 0L);
	}
	
	private void sendMessageSync(final String player, final String msg) {
		plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						try {
							Bukkit.getPlayerExact(player).sendMessage(msg);
						} catch(Exception e) {}
					}
				}, 0L);
	}
}
