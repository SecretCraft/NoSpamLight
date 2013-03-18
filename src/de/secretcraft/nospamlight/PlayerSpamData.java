package de.secretcraft.nospamlight;

import org.bukkit.ChatColor;

public class PlayerSpamData {
	public long lastMessageSendTime = 0;
	public String lastMessageSend = "";

	private long lastWarningTime = 0;
	private int numberOfWarnings = 0;

	public void timeStampCheck(String message) throws SpamRuleViolation {
		long timeDiff = System.currentTimeMillis() - lastMessageSendTime;
		if (timeDiff < minDelayBetweenMessages) {
			throw SpamRuleViolation.create(MIN_WAIT_BETWEEN_MESSAGES,
					minDelayBetweenMessages / 1000D);
		}
	}
	
	public void timeSameMessageCheck(String message) throws SpamRuleViolation {
		long timeDiff = System.currentTimeMillis() - lastMessageSendTime;
		if (lastMessageSend.equals(message)) {
			if (timeDiff < minDelayBetweenSameMessages * 1000) {
				throw SpamRuleViolation.create(MIN_WAIT_BETWEEN_SAME_MESSAGES,
						minDelayBetweenSameMessages);
			}
		}
	}

	public void setLastMessage(String message) {
		lastMessageSend = String.valueOf(message);
		lastMessageSendTime = System.currentTimeMillis();
	}

	/**
	 * Adds a warning to the player.
	 * 
	 * @return The number of warnings the player has currently.
	 */
	public int warn() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastWarningTime > milliSecondsToRemoveWarnings) {
			numberOfWarnings = 0;
		}
		lastWarningTime = currentTime;
		return ++numberOfWarnings;
	}

	public static long milliSecondsToRemoveWarnings = 1000 * 60 * 10;
	public static long minDelayBetweenMessages = 800; // in milliseconds
	public static long minDelayBetweenSameMessages = 60 * 3; // in seconds

	public static String MIN_WAIT_BETWEEN_SAME_MESSAGES = ChatColor.RED
			+ "You have to wait %d seconds to write the same text again.";

	public static String MIN_WAIT_BETWEEN_MESSAGES = ChatColor.RED
			+ "You have to wait %.2f seconds between each message.";
}
