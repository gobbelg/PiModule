package gov.va.tvhs.grecc.PiModule.core;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Object used to store the mapping of a phrase string within a sentence to a
 * concept and where the phrase is found in terms of sentence tokens. Note that
 * sentence tokens are zero-indexed and the startStringIndex is inclusive but
 * the endStringIndex field is non-inclusive. For example, if the
 * startStringIndex is 0 and the endStringIndex is 2, only tokens at indices 0
 * and 1 (the first and second tokens in the sentence) are included in the
 * phrase.
 *
 * @author gobbelgt
 *
 */
public class TokenStringTag implements Serializable
{
	private static final long serialVersionUID = -6128213618753491952L;
	private static final Logger LOGGER = Logger.getLogger(TokenStringTag.class);
	static
	{
		LOGGER.setLevel(Level.DEBUG);
	}
	private final String conceptName;
	private final String phrase;

	private final int startStringIndex;
	private final int endStringIndex;

	public TokenStringTag(String conceptName, String phrase, int startStringIndex, int endStringIndex)
	{
		this.conceptName = conceptName;
		this.phrase = phrase;
		this.startStringIndex = startStringIndex;
		this.endStringIndex = endStringIndex;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).append("ConceptName", conceptName).append("Phrase", phrase)
				.append("StartIndex", startStringIndex).append("EndIndex", endStringIndex).toString();
	}

}
