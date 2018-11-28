package gov.va.tvhs.grecc.PiModule.phrasecontext;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gov.va.tvhs.grecc.PiModule.core.Constants;
import gov.va.tvhs.grecc.PiModule.core.TokenStringTag;
import gov.va.tvhs.grecc.PiModule.datastructure.Pair;
import gov.va.tvhs.grecc.PiModule.datastructure.PhraseTree;

/**
 * Stores the mapping between phrases and concepts, phrases and inhibitors, and
 * phrases and promoters.
 *
 * @author gobbelgt - Jul 25, 2017
 *
 */
public class PhraseMapper implements Serializable
{
	private static final Logger LOGGER = Logger.getLogger(PhraseMapper.class);;

	private static final long serialVersionUID = -3167090984663364917L;

	static
	{
		LOGGER.setLevel(Level.DEBUG);
	};

	private static List<String[]> buildList(String[] thePhrases)
	{
		List<String[]> theList = new ArrayList<String[]>();
		for (String curPhrase : thePhrases)
		{
			theList.add(curPhrase.split(" "));
		}
		return theList;
	}

	/*
	 * Maps a concept name to the inhibitors of that concept
	 */
	private final Map<String, Map<String, MappingModifierPhrase>> conceptNameToInhibitorsMap = new HashMap<String, Map<String, MappingModifierPhrase>>();

	/*
	 * Maps a concept name to promoters of that concept
	 */
	private final Map<String, Map<String, MappingModifierPhrase>> conceptNameToPromotersMap = new HashMap<String, Map<String, MappingModifierPhrase>>();

	/*
	 * Maps a concept name to phrases mapping to that concept stored as MappedPhrase
	 * instances
	 */
	private final Map<String, Map<String, MappedPhrase>> conceptNameToPhrasesMap = new HashMap<String, Map<String, MappedPhrase>>();

	/*
	 * Maps a concept name to all phrase mapping to that concept where the phrases
	 * are stored in phraseTrees for fast determination of longest mapping phrase
	 * found for a concept within a given sentence
	 */
	private final Map<String, PhraseTree> phraseTrees = new HashMap<String, PhraseTree>();
	
	public PhraseMapper(String filePath) throws FileNotFoundException
	{
		this(new BufferedReader(new FileReader(filePath)));
	}

	/**
	 * Constructor for the PhraseMapper.
	 *
	 * @param configFilePath
	 *            - Full path to the configuration file that indicates the phrases
	 *            corresponding to concepts, promoters, and inhibitors.
	 */
	public PhraseMapper(BufferedReader br)
	{
		readConfigFile(br, conceptNameToPromotersMap, conceptNameToInhibitorsMap, conceptNameToPhrasesMap);

		for (String conceptName : conceptNameToPhrasesMap.keySet())
		{
			/* Get the dictionary phrases mapping to this concept */
			Map<String, MappedPhrase> conceptPhrases = conceptNameToPhrasesMap.get(conceptName);

			/* Get the promoters associated with this concept */
			Map<String, MappingModifierPhrase> promoters;
			if ((promoters = conceptNameToPromotersMap.get(conceptName)) == null)
			{
				promoters = new HashMap<String, MappingModifierPhrase>();
			}

			/* Get the inhibitors associated with this concept */
			Map<String, MappingModifierPhrase> inhibitors;
			if ((inhibitors = conceptNameToInhibitorsMap.get(conceptName)) == null)
			{
				inhibitors = new HashMap<String, MappingModifierPhrase>();
			}

			/*
			 * Create a phrase tree for this specific concept in which all the text phrases
			 * mapping to the concept and their associated promoters and inhibitors are put
			 * into a single PhraseTree object. The end of the phrase is marked by an
			 * indicator telling whether it is a mapped phrase, promoter, or inhibitor.
			 */
			PhraseTree contextPhraseTree = initializeContextPhrases(conceptPhrases, promoters, inhibitors);

			/* Store the PhraseTree object for concept "conceptName" in a map */
			phraseTrees.put(conceptName, contextPhraseTree);
		}

	}

	public Set<String> getConceptList()
	{
		return conceptNameToPhrasesMap.keySet();
	}

	/**
	 * This method creates an array of Mark objects that determines whether the
	 * strings in the tokenStrings array are part of a promoter or inhibitor phrase
	 * for the concept, conceptName. If a tokenString is part of a promoter or
	 * inhibitor, the ConceptPhraseModifier object at the same index in the windows
	 * array as the string is set to the ConceptPhraseModifier object corresponding
	 * to the promoter or inhibitor.
	 *
	 * @param tokenStrings
	 *            - Array of String objects, typically corresponding to a sentence.
	 *            Assumes that the Strings are in the order found in the sentence
	 *            with tokenStrings[0] being the string found in the first token
	 *            within the sentence.
	 * @param mappingModifiers
	 *            - Array of MappingModifierPhrase objects. The objects in the array
	 *            are modified by the method to contains the MappingModifierPhrase
	 *            instances at the indices containing a promoter or inhibitor
	 *            phrase.
	 * @param conceptName
	 *            - String corresponding to the concept for which promoter and
	 *            inhibitor phrase are being identified.
	 * @return - Array of ContextMark enum value instances. The instances are set to
	 *         ContextMark.PROMOTER or ContextMark.INHIBITOR if the String object at
	 *         a given index within the tokenStrings array is part of a promoter or
	 *         inhibitor phrase, respectively.
	 */
	public Constants.ContextMark[] getContextMarks(String[] tokenStrings, MappingModifierPhrase[] mappingModifiers,
			String conceptName)
	{
		PhraseTree phraseTree = phraseTrees.get(conceptName);
		Map<String, MappingModifierPhrase> promoterMap = conceptNameToPromotersMap.get(conceptName);
		Map<String, MappingModifierPhrase> inhibitorMap = conceptNameToInhibitorsMap.get(conceptName);

		int tokenListSize = tokenStrings.length;
		Constants.ContextMark[] contextMarks = new Constants.ContextMark[tokenListSize];
		Arrays.fill(contextMarks, Constants.ContextMark.NONE);

		/* Find inhibitors first - promoters should not overwrite inhibitors */
		setContextMarks(tokenStrings, inhibitorMap, phraseTree, contextMarks, mappingModifiers,
				Constants.ContextMark.INHIBITOR);
		setContextMarks(tokenStrings, promoterMap, phraseTree, contextMarks, mappingModifiers,
				Constants.ContextMark.PROMOTER);

		return contextMarks;
	}

	public List<TokenStringTag> getTokenStringTags(String[] tokenStrings, String conceptName, String[] promotedTokens,
			String[] inhibitedTokens, boolean[] hasAssignedConcept)
	{
		List<TokenStringTag> tokenStringTags = new ArrayList<TokenStringTag>(tokenStrings.length);
		MappedPhrase conceptPhrase;

		int tokenIndex = 0;
		while (tokenIndex < tokenStrings.length)
		{
			boolean assignConcept = false;
			StringBuilder sb = new StringBuilder();

			/*
			 * Only consider GenMarks that have not already been tagged and therefore are
			 * currently marked as NONE
			 */
			int foundPhraseLength = phraseTrees.get(conceptName).phraseLengthAtIndexIgnoreCase(tokenStrings, tokenIndex,
					Constants.ContextMark.CONCEPTWORD);

			/*
			 * Glenn comment - 10/22/15 - foundPhraseLength > 0 indicates that there is a
			 * phrase mapping to concept 'conceptName' at index tokenIndex
			 */
			if (foundPhraseLength > 0)
			{
				sb.append(tokenStrings[tokenIndex]);

				boolean inhibited = inhibitedTokens[tokenIndex].equals(Constants.ContextMark.INHIBITED.toString());
				boolean promoted = promotedTokens[tokenIndex].equals(Constants.ContextMark.PROMOTED.toString());
				assignConcept = !hasAssignedConcept[tokenIndex];

				/*
				 * Glenn comment - 10/22/15 - In the for loop below, we are both putting the
				 * tokens into one phrase and determining if any of the tokens in that phrase
				 * are blocked, facilitated, or already have a concept assigned. Unless the
				 * phrase is assigned to a concept, we have to determine all token strings and
				 * evaluate promotion or inhibition for all these strings to determine what the
				 * conceptPhrase string is and whether it requires promotion or is inhibitable.
				 */
				if (assignConcept)
				{
					for (int j = tokenIndex + 1; j < (tokenIndex + foundPhraseLength); j++)
					{
						if (promotedTokens[j].equals(Constants.ContextMark.PROMOTED.toString()))
						{
							promoted = true;
						}
						if (inhibitedTokens[j].equals(Constants.ContextMark.INHIBITED.toString()))
						{
							inhibited = true;
						}
						if (hasAssignedConcept[j])
						{
							assignConcept = false;
							break;
						}

						sb.append(' ').append(tokenStrings[j]);
					}
				}

				if (assignConcept)
				{
					/*
					 * Glenn comment - 10/22/15 - This will retrieve a ConceptPhrase object
					 * indicating whether the phrase can be blocked or requires facilitation
					 */
					Map<String, MappedPhrase> phrasesForConcept = conceptNameToPhrasesMap.get(conceptName);
					conceptPhrase = phrasesForConcept.get(sb.toString().trim());

					if ((conceptPhrase.requiresPromotion && !promoted) || (conceptPhrase.inhibitable && inhibited))
					{
						assignConcept = false;
					}
				}
			}
			/*
			 * Skip over tokens that are part of a found phrase that was not previously
			 * marked
			 */
			if (assignConcept)
			{
				int endStringIndex = tokenIndex + foundPhraseLength;
				for (int j = tokenIndex; j < endStringIndex; j++)
				{
					hasAssignedConcept[j] = true;
				}

				TokenStringTag tokenStringTag = new TokenStringTag(conceptName, sb.toString(), tokenIndex,
						endStringIndex);
				tokenStringTags.add(tokenStringTag);

				tokenIndex = endStringIndex;
			}
			else
			{
				tokenIndex++;
			}
		}

		return tokenStringTags;
	}

	/**
	 * Build PhraseTree objects that efficiently store promoter, inhibitor, and
	 * concept phrase for look up
	 *
	 * @param conceptPhrases
	 * @param promoters
	 * @param inhibitors
	 * @return
	 */
	private PhraseTree initializeContextPhrases(Map<String, MappedPhrase> conceptPhrases,
			Map<String, MappingModifierPhrase> promoters, Map<String, MappingModifierPhrase> inhibitors)
	{
		PhraseTree contextPhraseTree = new PhraseTree();

		contextPhraseTree.addPhrases(buildList(promoters.keySet().toArray(new String[0])),
				Constants.ContextMark.PROMOTER);
		contextPhraseTree.addPhrases(buildList(inhibitors.keySet().toArray(new String[0])),
				Constants.ContextMark.INHIBITOR);
		contextPhraseTree.addPhrases(buildList(conceptPhrases.keySet().toArray(new String[0])),
				Constants.ContextMark.CONCEPTWORD);

		return contextPhraseTree;
	}

	public void printConceptPhraseMappings()
	{
		for (String key : phraseTrees.keySet())
		{
			System.out.println("\n*******************\nConcept:" + key + "\n*******************\n");
			PhraseTree tree = phraseTrees.get(key);
			for (Pair<List<String>, PhraseTree> phrase : tree)
			{
				System.out.print(phrase.right.getPhraseTag() + ":");
				StringBuilder sb = new StringBuilder("");
				for (String curString : phrase.left)
				{
					sb.append(" ").append(curString);
				}
				System.out.println(sb);
			}
		}
	}

	/**
	 * This method was created for debugging to show the state of the promoter,
	 * inhibitor, and concept phrase mappers for this object
	 */
	public void printMaps()
	{
		HashSet<String> allConcepts = new HashSet<String>();
		allConcepts.addAll(conceptNameToPromotersMap.keySet());
		allConcepts.addAll(conceptNameToInhibitorsMap.keySet());
		allConcepts.addAll(conceptNameToPhrasesMap.keySet());

		for (String curKey : allConcepts)
		{
			System.out.println("\n**************************\nCONCEPT:" + curKey + "\n**************************\n");

			System.out.println("\nCONCEPT MAPPED PHRASES:");
			Map<String, MappedPhrase> conceptPhrases = conceptNameToPhrasesMap.get(curKey);
			for (String curPhrase : conceptPhrases.keySet())
			{
				System.out.println(conceptPhrases.get(curPhrase).toString());
			}

			System.out.println("\nPROMOTERS:");
			Map<String, MappingModifierPhrase> curpromoters = conceptNameToPromotersMap.get(curKey);
			if (curpromoters != null)
			{
				for (String curPhrase : curpromoters.keySet())
				{
					System.out.println(curpromoters.get(curPhrase).toString());
				}
			}
			else
			{
				System.out.println("None");
			}

			System.out.println("\nINIHIBITORS:");
			Map<String, MappingModifierPhrase> curinhibitors = conceptNameToInhibitorsMap.get(curKey);
			if (curinhibitors != null)
			{
				for (String curPhrase : curinhibitors.keySet())
				{
					System.out.println(curinhibitors.get(curPhrase).toString());
				}
			}
			else
			{
				System.out.println("None");
			}
		}

	}


	
	/**
	 * Creates the promoter, inhibitor and concept phrase list from a single file.
	 *
	 * @param br
	 * @param promoters
	 * @param inhibitorMap
	 * @param conceptPhrases
	 * @return
	 */
	private void readConfigFile(BufferedReader br, Map<String, Map<String, MappingModifierPhrase>> promoters,
			Map<String, Map<String, MappingModifierPhrase>> inhibitorMap,
			Map<String, Map<String, MappedPhrase>> conceptPhrases)
	/*
	 * If there is no value in the 'requiresPromotion' or 'inhibitable' columns of
	 * the configFile, both the values should be set to a default value of false.
	 *
	 * TODO: Check consistency of configuration file during reading. A consistent
	 * phrase should not have any of its token subsequences be equal to another
	 * phrase. If this occurs but it is the same type of phrase (e.g. a phrase
	 * mapping to the same concept with the same settings of requiresPromotion and
	 * inhibitable, a inhibitor for the same concept with equal windows of
	 * influence, or a promoter with the same windows), the longer phrase could be
	 * discarded (unless the windows of influence were dramatically different).
	 */
	{
		/*
		 * These next 3 variables indicate columns within the configuration file where
		 * information should be located
		 */
		final int forwardPromoterIndex = 3;
		final int backwardInhibitedIndex = 4;
		final int overridesIndex = 5;

		Map<String, MappingModifierPhrase> conceptSpecificpromoters;
		Map<String, MappingModifierPhrase> conceptSpecificinhibitors;
		Map<String, MappedPhrase> textToConceptPhraseMap;

		boolean requiresPromoter;
		boolean canBeInhibited;
		boolean overridesProbabilisticAssignment = false;

		try
		{			
			String line;
			String[] tokens;
			int backwardWindow;
			int forwardWindow;

			/* Discard first line of titles */
			br.readLine();

			while ((line = br.readLine()) != null)
			{
				tokens = line.split("\\t");

				String phraseType = tokens[0];
				String conceptName = tokens[1];
				String textPhrase = tokens[2];

				/*
				 * Glenn comment 10/21/15 -These two variables indicate window of influence for
				 * promoters and inhibitors and indicates whether a phrase must be facilitated
				 * or can be blocked for concept phrases.
				 */
				String forwardWindowOrPromoted = null;
				String backWindowOrInhibited = null;

				if (tokens[forwardPromoterIndex] != null && tokens[forwardPromoterIndex].length() > 0)
				{
					forwardWindowOrPromoted = tokens[forwardPromoterIndex];
					if (tokens[backwardInhibitedIndex] != null && tokens[backwardInhibitedIndex].length() > 0)
					{
						backWindowOrInhibited = tokens[backwardInhibitedIndex];
					}
				}

				if (phraseType.equalsIgnoreCase("ConceptPhrase"))
				{
					if (backWindowOrInhibited == null)
					{
						canBeInhibited = false;

						if (forwardWindowOrPromoted == null)
						{
							requiresPromoter = false;
						}
						else
						{
							requiresPromoter = Boolean.parseBoolean(tokens[forwardPromoterIndex]);
						}
					}
					else
					{
						canBeInhibited = Boolean.parseBoolean(backWindowOrInhibited);
						requiresPromoter = Boolean.parseBoolean(forwardWindowOrPromoted);
					}

					if (tokens[overridesIndex] != null && tokens[overridesIndex].length() > 0)
					{
						overridesProbabilisticAssignment = Boolean.parseBoolean(tokens[overridesIndex]);
					}

					if ((textToConceptPhraseMap = conceptPhrases.get(conceptName)) == null)
					{
						textToConceptPhraseMap = new HashMap<String, MappedPhrase>();
						conceptPhrases.put(conceptName, textToConceptPhraseMap);
					}
					/*
					 * Glenn comment 10/21/15 -There is some redundancy of the key and the elements
					 * of ConceptPhrase (both contain the phrase itself).
					 */
					textToConceptPhraseMap.put(textPhrase.trim(), new MappedPhrase(textPhrase.trim(), requiresPromoter,
							canBeInhibited, overridesProbabilisticAssignment));
				}
				else
				{
					if (backWindowOrInhibited == null)
					{
						backwardWindow = Constants.DEFAULT_CONTEXT_WINDOW_RADIUS;

						if (forwardWindowOrPromoted == null)
						{
							forwardWindow = Constants.DEFAULT_CONTEXT_WINDOW_RADIUS;
						}
						else
						{
							forwardWindow = Integer.parseInt(forwardWindowOrPromoted);
						}
					}
					else
					{
						forwardWindow = Integer.parseInt(forwardWindowOrPromoted);
						backwardWindow = Integer.parseInt(backWindowOrInhibited);

						/*
						 * if their value is less than 0, then the window spans the whole sentence (as
						 * long as it's no bigger than Integer.MAX_VALUE!
						 */
						if (forwardWindow < 0)
						{
							forwardWindow = Integer.MAX_VALUE;
						}
						if (backwardWindow < 0)
						{
							backwardWindow = Integer.MAX_VALUE;
						}
					}

					if (phraseType.equalsIgnoreCase(Constants.ContextMark.PROMOTER.toString()))
					{
						if ((conceptSpecificpromoters = promoters.get(conceptName)) == null)
						{
							conceptSpecificpromoters = new HashMap<String, MappingModifierPhrase>();
							promoters.put(conceptName, conceptSpecificpromoters);
						}

						/*
						 * Glenn comment 10/21/15 -There is some redundancy of the key and the elements
						 * of ConceptPhraseModifier (both contain the phrase itself).
						 */
						conceptSpecificpromoters.put(textPhrase,
								new MappingModifierPhrase(textPhrase, backwardWindow, forwardWindow));
					}
					else if (phraseType.equalsIgnoreCase(Constants.ContextMark.INHIBITOR.toString()))
					{
						if ((conceptSpecificinhibitors = inhibitorMap.get(conceptName)) == null)
						{
							/*
							 * Glenn comment 10/21/15 -There is some redundancy of the key and the elements
							 * of ConceptPhraseModifier (both contain the phrase itself).
							 */
							conceptSpecificinhibitors = new HashMap<String, MappingModifierPhrase>();
							inhibitorMap.put(conceptName, conceptSpecificinhibitors);
						}

						conceptSpecificinhibitors.put(textPhrase,
								new MappingModifierPhrase(textPhrase, backwardWindow, forwardWindow));
					}
				}

			}
		}
		catch (IOException ex)
		{
			System.exit(-1);
		}
	}

	/**
	 * Analyzes each token in an array of tokens and sets a mark in tokenMarks if
	 * the phrase (starting at a given position) corresponds to a certain type of
	 * mark, "markToFind", is found. It also modifies the windows parameter with the
	 * window or radius in terms of number of tokens influenced on each side of the
	 * mark. Note that the method purposely changes what is contained in the array
	 * tokenMarks.
	 *
	 * Because inhibitors typically override promoters, calls to this method should
	 * be called first with markToFind set to Constants.ContextMark.INHIBITOR
	 *
	 * @param tokenStrings
	 * @param promoters
	 * @param inhibitors
	 * @param phraseTree
	 * @param tokenMarks
	 * @param mappingModifiers
	 * @param markToFind
	 */
	private void setContextMarks(String[] tokenStrings, Map<String, MappingModifierPhrase> phraseTextToModifierMap,
			PhraseTree phraseTree, Constants.ContextMark[] tokenMarks, MappingModifierPhrase[] mappingModifiers,
			Constants.ContextMark markToFind)
	{
		StringBuilder sb;

		for (int tokenIndex = 0; tokenIndex < tokenStrings.length; tokenIndex++)
		{
			/*
			 * Only consider GenMarks that have not already been tagged and therefore are
			 * currently marked as NONE
			 */
			if (tokenMarks[tokenIndex] == Constants.ContextMark.NONE)
			{
				int foundPhraseLength = phraseTree.phraseLengthAtIndexIgnoreCase(tokenStrings, tokenIndex, markToFind);

				if (foundPhraseLength > 0)
				{
					sb = new StringBuilder();
					// Recreating the phrase
					for (int j = tokenIndex; j < (tokenIndex + foundPhraseLength); j++)
					{
						sb.append(tokenStrings[j]).append(' ');
					}

					for (int j = tokenIndex; j < (tokenIndex + foundPhraseLength); j++)
					{
						// Stop if we reach a mark that has already been set
						// to something other than NONE
						if (tokenMarks[j] != Constants.ContextMark.NONE)
						{
							break;
						}
						tokenMarks[j] = markToFind;

						mappingModifiers[j] = phraseTextToModifierMap.get(sb.toString().trim());
					}
				}
			}
		}
	}

}
