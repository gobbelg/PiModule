package gov.va.tvhs.grecc.PiModule.phrasecontext;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Stores information about phrase strings that act as inhibitors or promoters
 * of the concept mapping of other phrases in a sentence. The information stored
 * includes the phrase itself and how far backward or forward its influence is
 * within an array of strings representing a sentence.
 *
 * @author gobbelgt - Jul 25, 2017
 *
 */
public class MappingModifierPhrase extends PhraseType
{
	private static final Logger LOGGER = Logger.getLogger(MappingModifierPhrase.class);
	private static final long serialVersionUID = -2000133615936070747L;
	static
	{
		LOGGER.setLevel(Level.DEBUG);
	}

	public final int backwardWindow;
	public final int forwardWindow;

	public MappingModifierPhrase(String phrase, int forwardWindow, int backwardWindow)
	{
		super(phrase);
		this.forwardWindow = forwardWindow;
		this.backwardWindow = backwardWindow;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).append("Phrase", phrase).append("ForwardWindow", forwardWindow)
				.append("BackwardWindow", backwardWindow).toString();
	}

}
