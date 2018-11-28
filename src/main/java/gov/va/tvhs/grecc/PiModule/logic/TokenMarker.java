package gov.va.tvhs.grecc.PiModule.logic;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gov.va.tvhs.grecc.PiModule.core.Constants;
import gov.va.tvhs.grecc.PiModule.datastructure.Pair;
import gov.va.tvhs.grecc.PiModule.phrasecontext.MappingModifierPhrase;


/**
 * Marks tokens that are promoted or inhibited given an array of ContextMark
 * instances
 *
 * @author Glenn Gobbel - Jul 17, 2014
 *
 */
public class TokenMarker implements Serializable
{
	private static Logger LOGGER = Logger.getLogger(TokenMarker.class);
	private static final long serialVersionUID = 8741921004650804483L;

	public TokenMarker()
	{
		LOGGER.setLevel(Level.INFO);
	}

	private int findLastWordString(String[] tokenStrings)
	{
		int curIndex = tokenStrings.length - 1;

		while ((curIndex >= 0) && !tokenStrings[curIndex].matches("[A-Za-z0-9]+"))
		{
			--curIndex;
		}

		return curIndex;
	}

	/**
	 * Determines whether the strings in an array, tokenStrings, are promoted or
	 * inhibited based on the promoterInhibitorMarks array. This method distributes
	 * the impact of promotion and inhibition across the tokenStrings array
	 * according to which tokens are marked as promoters or inhibitors and the
	 * window of influence.
	 * <p>
	 * The results for both promotion and inhibition are returned in a pair of
	 * String arrays.
	 *
	 * @param tokenStrings
	 *            - Array of String objects, typically corresponding to a sentence.
	 *            Assumes that the Strings are in the order found in the sentence
	 *            with tokenStrings[0] being the string found in the first token
	 *            within the sentence.
	 * @param promoterInhibitorMarks
	 *            - Array of ContextMark enum values indicating whether a particular
	 *            token within the tokenString array is a promoter or inhibitor
	 * @param mappingModifiers
	 *            - Array of MappingModifierPhrase instances. Each index in the
	 *            array corresponds to an index in the tokenStrings array and, if
	 *            the index corresponds to a promoter or inhibitor, it contains the
	 *            modifier phrase itself and the forward and backward windows of
	 *            influence of the phrase.
	 * @return A pair of arrays of String objects indicating whether the String
	 *         instances in the tokenStrings array are inhibited or promoted. The
	 *         left side of pair contains an array indicating which token strings
	 *         are promoted, and right side contains an array indicating which token
	 *         strings are inhibited
	 */
	public Pair<String[], String[]> getContext(String[] tokenStrings, Constants.ContextMark[] promoterInhibitorMarks,
			MappingModifierPhrase[] mappingModifiers)
	{
		String[] promotedTags = new String[tokenStrings.length];
		String[] inhibitedTags = new String[tokenStrings.length];
		Arrays.fill(promotedTags, Constants.ContextMark.NONE.toString());
		Arrays.fill(inhibitedTags, Constants.ContextMark.NONE.toString());

		ForwardLogicAnalyzer forwardAnalyzer = new ForwardLogicAnalyzer(mappingModifiers);

		forwardAnalyzer.reset(promoterInhibitorMarks);
		for (int i = 0; i < tokenStrings.length; i++)
		{
			forwardAnalyzer.updatePromotingStatus();

			if (forwardAnalyzer.isMarking)
			{
				promotedTags[i] = Constants.ContextMark.PROMOTED.toString();
			}
		}

		forwardAnalyzer.reset(promoterInhibitorMarks);
		for (int i = 0; i < tokenStrings.length; i++)
		{
			forwardAnalyzer.updateInhibitingStatus();

			if (forwardAnalyzer.isMarking)
			{
				inhibitedTags[i] = Constants.ContextMark.INHIBITED.toString();
			}
		}

		int tokenIndex;
		BackwardLogicAnalyzer backwardAnalyzer = new BackwardLogicAnalyzer(mappingModifiers);

		if ((tokenIndex = findLastWordString(tokenStrings)) >= 0)
		{
			backwardAnalyzer.reset(tokenIndex, promotedTags);

			for (; tokenIndex > -1; tokenIndex--)
			{
				backwardAnalyzer.updatePromotingStatus();

				if (backwardAnalyzer.isMarking)
				{
					promotedTags[tokenIndex] = Constants.ContextMark.PROMOTED.toString();
				}
			}
		}

		if ((tokenIndex = findLastWordString(tokenStrings)) >= 0)
		{
			backwardAnalyzer.reset(tokenIndex, promotedTags);

			for (; tokenIndex > -1; tokenIndex--)
			{
				backwardAnalyzer.updateInhibitingStatus();

				if (backwardAnalyzer.isMarking)
				{
					inhibitedTags[tokenIndex] = Constants.ContextMark.INHIBITED.toString();
				}
			}
		}

		return new Pair<String[], String[]>(promotedTags, inhibitedTags);
	}
}
