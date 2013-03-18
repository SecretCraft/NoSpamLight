package de.secretcraft.nospamlight;

public class SpamRuleViolation extends Exception {
	private static final long serialVersionUID = 1163426561911110785L;

	public SpamRuleViolation(String arg0) {
		super(arg0);
	}

	public static SpamRuleViolation create(String format, Object ... args) {
		try {
			return new SpamRuleViolation(String.format(format, args));
		} catch(Exception e) {
			return new SpamRuleViolation(format);
		}
	}
}
