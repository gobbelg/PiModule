package gov.va.tvhs.grecc.PiModule.phrasecontext;

import java.io.Serializable;

/**
 * Abstract class that contains a field that stores phrase strings that may be a
 * field in subclasses, such as PhraseModifiere and MappedPhrase instances.
 *
 * @author gobbelgt - Jul 25, 2017
 *
 */
public abstract class PhraseType implements Serializable
{
	private static final long serialVersionUID = -1807663106385438560L;
	public final String phrase;

	protected PhraseType(String phrase)
	{
		this.phrase = phrase;
	}
}
