package gov.va.tvhs.grecc.PiModule.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gov.va.tvhs.grecc.PiModule.datastructure.Pair;
import gov.va.tvhs.grecc.PiModule.logic.TokenMarker;
import gov.va.tvhs.grecc.PiModule.phrasecontext.MappingModifierPhrase;
import gov.va.tvhs.grecc.PiModule.phrasecontext.PhraseMapper;

/**
 * Stores promoter, inhibitor, and concept phrase information and determines the
 * concepts contains within arrays of strings representing sentences
 *
 * @author gobbelgt - Jul 25, 2017
 *
 */
public class TokenContextAnalyzer implements Serializable
{
	private static final Logger LOGGER = Logger.getLogger(TokenContextAnalyzer.class);
	private static final long serialVersionUID = -8732239638154877596L;

	static
	{
		LOGGER.setLevel(Level.DEBUG);
	}

	private PhraseMapper phraseContextMapper = null;

	private final TokenMarker tokenContextMarker;

	public TokenContextAnalyzer(String dictionaryPath)
	{
		try
		{
			phraseContextMapper = new PhraseMapper(dictionaryPath);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		tokenContextMarker = new TokenMarker();
	}
	
	public TokenContextAnalyzer(BufferedReader br)
	{
		phraseContextMapper = new PhraseMapper(br);
		tokenContextMarker = new TokenMarker();
	}

	public PhraseMapper getPhraseMapper()
	{
		return phraseContextMapper;
	}

	/**
	 * Takes an array of strings that represent a sentence or phrase and produces a
	 * list of tags that represent the context of each of the strings based on the
	 * other strings in that sentence. The method assumes that the strings in the
	 * tokenStrings array are in their natural order (i.e., the order they appear in
	 * the sentence from first to last). The concepts are returned as a collection
	 * of TokenStringTag instances.
	 *
	 * @param tokenStrings
	 *            - An array of String instances representing the tokens in the
	 *            sentence to be analyzed for concepts. The method assumes that the
	 *            strings are in the same order as found in an English sentence with
	 *            tokenString[0] being the first string in the sentence
	 * @return Collection of TokenStringTag instances
	 */
	public Collection<TokenStringTag> getTokenStringTags(String[] tokenStrings)
	{
		Collection<TokenStringTag> tokenStringTags = new ArrayList<TokenStringTag>(tokenStrings.length);

		boolean hasAssignedConcept[] = new boolean[tokenStrings.length];
		Arrays.fill(hasAssignedConcept, false);

		/*
		 * We want to know the order of concepts as this establishes precedence in terms
		 * of labeling phrases as dictionary-based annotations. Once assigned, the fact
		 * that a token has been assigned is stored in the array 'hasAssignedConcept'.
		 */
		List<String> concepts = new ArrayList<String>(phraseContextMapper.getConceptList());
		Collections.sort(concepts);

		/*
		 * We're iterating multiple times through each sentence, once for every concept.
		 * It might be possible to increase the efficiency of this using a single
		 * PhraseTree instance to map all phrases in sentence to concepts and their
		 * inhibitors and modifiers.
		 */
		for (String conceptName : concepts)
		{
			/*
			 * Keeps track of which tokens in an array of token strings are part of
			 * inhibitors or promoter phrases and their windows of influence
			 */
			MappingModifierPhrase[] mappringModifiers = new MappingModifierPhrase[tokenStrings.length];

			/*
			 * After calling getContextMarks() below, the array of marks, this.contextMarks,
			 * is equal in length to the tokenStrings array, and every mark indicates if the
			 * corresponding token string at the same index is a promoter or a inhibitor of
			 * the concept being currently analyzed, conceptName. The modifierWindows array,
			 * passed as a parameter, stores the range of influence, upstream and downstream
			 * separately, for each token.
			 *
			 * Note that the impact of the promoter or inhibitor is not distributed in this
			 * call but in the method getContext().
			 */
			Constants.ContextMark[] contextMarks = phraseContextMapper.getContextMarks(tokenStrings, mappringModifiers,
					conceptName);

			/*
			 * The call to getContextMarks above determined if a token string was part of a
			 * promoter or inhibitor and, if so, the window of influence.
			 *
			 * This next call determines if that string and those in the window of influence
			 * are actually promoted or inhibited. This is returned in a pair of string
			 * arrays that indicate, for each string in tokenStrings array, whether the
			 * corresponding string in tokensStrings is promoted or inhibited with respect
			 * to the concept, conceptName.
			 */
			Pair<String[], String[]> promotedInhibitedTokens = tokenContextMarker.getContext(tokenStrings, contextMarks,
					mappringModifiers);

			String[] promotedTokens = promotedInhibitedTokens.left;
			String[] inhibitedTokens = promotedInhibitedTokens.right;

			List<TokenStringTag> conceptSpecificStringTags = phraseContextMapper.getTokenStringTags(tokenStrings,
					conceptName, promotedTokens, inhibitedTokens, hasAssignedConcept);

			tokenStringTags.addAll(conceptSpecificStringTags);
		}

		return tokenStringTags;
	}

}
