package gov.va.tvhs.grecc.PiModule.phrasecontext;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Stores the information about phrases that map to a concept including the
 * phrase, whether it requires promotion, and whether it can be inhibited.
 *
 * @author Glenn Gobbel - July 25, 2017
 */
public class MappedPhrase extends PhraseType
{
	private static final Logger LOGGER = Logger.getLogger(MappedPhrase.class);
	/**
	 *
	 */
	private static final long serialVersionUID = 6387245961155967093L;
	static
	{
		LOGGER.setLevel(Level.DEBUG);
	}

	public final boolean inhibitable;

	/*
	 * Indicates whether the specific concept phrase if found and properly
	 * facilitated and not blocked should override any overlapping annotations by
	 * probabilistic methods
	 */
	public final boolean overrides;

	public final boolean requiresPromotion;

	public MappedPhrase(String phrase, boolean requiresPromotion, boolean inhibitable, boolean overrides)
	{
		super(phrase);
		this.requiresPromotion = requiresPromotion;
		this.inhibitable = inhibitable;
		this.overrides = overrides;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).append("Phrase", phrase).append("RequiresFacilitation", requiresPromotion)
				.append("Blockable", inhibitable).append("OverridesRaptat", overrides).toString();
	}
}
