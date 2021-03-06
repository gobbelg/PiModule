package gov.va.tvhs.grecc.PiModule.logic;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gov.va.tvhs.grecc.PiModule.core.Constants;
import gov.va.tvhs.grecc.PiModule.phrasecontext.MappingModifierPhrase;

/**
 * Used by TokenMarker instances to determine which tokens upstream from a given
 * token (specified by the index of the token within the array of tokens
 * corresponding to a sentence) are either inhibited or promoted with respect to
 * a given phrase mapping to a particular concept.
 * <p>
 * Because the windows of influence extend forward to subsequent tokens and
 * backward to preceding tokens from a given promoter or inhibitor phrase, we
 * have both ForwardLogicAnalyzer instances and BackwardLogicAnalyzer instance.
 *
 * @author gobbelgt - Jul 25, 2017
 *
 */
public class BackwardLogicAnalyzer extends LogicAnalyzer
{
	private static final Logger LOGGER = Logger.getLogger(BackwardLogicAnalyzer.class);
	private static final long serialVersionUID = -7087551823827566175L;
	static
	{
		LOGGER.setLevel(Level.DEBUG);
	}
	String[] result;

	MappingModifierPhrase[] windows;
	private int inhibitorWindowRemaining = 0;

	private int promoterWindowRemaining = 0;

	public BackwardLogicAnalyzer(MappingModifierPhrase[] windows)
	{
		this.windows = windows;
	}

	public void reset(int theIndex, String[] theMarks)
	{
		currentIndex = theIndex;
		result = theMarks;
	}

	public void updateInhibitingStatus()
	{
		Constants.ContextMark curMark = boundaryMarks[currentIndex];
		isMarking = false;

		if (curMark == Constants.ContextMark.INHIBITOR)
		{
			inhibitorWindowRemaining = windows[currentIndex].backwardWindow;
			isMarking = true;
		}
		else if (inhibitorWindowRemaining > 0)
		{
			isMarking = true;
			inhibitorWindowRemaining--;
		}

		--currentIndex;
	}

	public void updatePromotingStatus()
	{
		Constants.ContextMark curMark = boundaryMarks[currentIndex];
		isMarking = false;

		if (curMark == Constants.ContextMark.PROMOTER)
		{
			promoterWindowRemaining = windows[currentIndex].backwardWindow;
			isMarking = true;
		}
		else if (promoterWindowRemaining > 0)
		{
			isMarking = true;
			promoterWindowRemaining--;
		}

		--currentIndex;
	}

}
