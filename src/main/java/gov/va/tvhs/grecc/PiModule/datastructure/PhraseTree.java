package gov.va.tvhs.grecc.PiModule.datastructure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import gov.va.tvhs.grecc.PiModule.core.Constants;

/**
 * Used to store phrases and their labels, stored as ContextMark enum values.
 * Used for storing phrases that determine context of tokens that precede or
 * follow the phrase.
 * <p>
 * <b>IMPORTANT NOTE</b> - To optimize efficiency, the phraseTag label can only
 * be set the first time a phrase is added to the PhraseTree instance. The only
 * way to change the label is to generate a new PhraseTree instance and
 * re-populate it with phrases.
 *
 * @author Glenn Gobbel - Jun 3, 2013
 *
 */
public class PhraseTree extends HashTree<PhraseTree> implements IterableHashTree<PhraseTree>
{
	/**
	 * Provides an implementation of iterable to return the labeled sequences and
	 * their corresponding field values.
	 *
	 * @author Glenn Gobbel - Feb 8, 2012
	 */
	private class PhraseTreeIterator implements Iterator<Pair<List<String>, PhraseTree>>
	{
		/*
		 * Keep track of where we are in terms of the depth of the hashtree
		 */
		private final ArrayDeque<Enumeration<String>> phraseTreeKeys;
		private final ArrayDeque<PhraseTree> phraseTrees;

		/*
		 * Keep the current string sequence to return during iteration
		 */
		private final ArrayDeque<String> keyStack;

		private Pair<List<String>, PhraseTree> curResult;
		boolean nextFound;

		private PhraseTreeIterator()
		{

			phraseTreeKeys = new ArrayDeque<Enumeration<String>>();
			phraseTrees = new ArrayDeque<PhraseTree>();
			keyStack = new ArrayDeque<String>();

			phraseTrees.addLast(PhraseTree.this);
			phraseTreeKeys.addLast(keys());
			nextFound = setNext();

		}

		
		public boolean hasNext()
		{
			return nextFound;
		}

		/**
		 * Returns the next sequence of string tokens in the hashTree along with the
		 * labeled, unlabeled, deleted, and likelihood for the sequence.
		 *
		 * @return - The labeledHashTreeFields for the next token sequence
		 */
		public Pair<List<String>, PhraseTree> next()
		{
			final Pair<List<String>, PhraseTree> theResult = curResult;
			nextFound = setNext();
			return theResult;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		private boolean setNext()
		{
			while (!phraseTrees.isEmpty())
			{
				PhraseTree curTree = phraseTrees.removeLast();
				Enumeration<String> curHashKeys = phraseTreeKeys.removeLast();

				while (curHashKeys.hasMoreElements())
				{
					final String testKey = curHashKeys.nextElement();
					final PhraseTree testTree = curTree.get(testKey);
					if (testTree.phraseTag != null)
					{
						keyStack.addLast(testKey);
						final List<String> keys = new ArrayList<String>(keyStack);
						curResult = new Pair<List<String>, PhraseTree>(keys, testTree);
						phraseTrees.addLast(curTree);
						phraseTreeKeys.addLast(curHashKeys);
						if (testTree.size() > 0)
						{
							curTree = testTree;
							curHashKeys = curTree.keys();
							phraseTrees.addLast(curTree);
							phraseTreeKeys.addLast(curHashKeys);
						}
						else
						{
							keyStack.removeLast();
						}
						return true;
					}
					if (testTree.size() > 0)
					{
						keyStack.addLast(testKey);
						phraseTrees.addLast(curTree);
						phraseTreeKeys.addLast(curHashKeys);
						curTree = testTree;
						curHashKeys = curTree.keys();
					}
				}

				if (!keyStack.isEmpty())
				{
					keyStack.removeLast();
				}

			}
			return false;
		}
	}

	private static final Logger LOGGER = Logger.getLogger(PhraseTree.class);

	static
	{
		LOGGER.setLevel(Level.DEBUG);
	}

	private static final long serialVersionUID = 3777907279564137001L;

	/*
	 * Field to store the type of phrase in terms of ContextMark that a phrase
	 * within a PhraseTree object corresponds to
	 */
	private Constants.ContextMark phraseTag = null;

	/**
	 * Determines whether phrases put into the tree and compare to the tree should
	 * consider character case. This is done to add flexibility to the class. We
	 * also have included a method phraseLengthAtIndexIgnoreCase() which speeds up
	 * processing by just avoiding an "if" test.
	 */
	private boolean ignoreCase = true;

	public PhraseTree()
	{
		super();
	}

	public PhraseTree(boolean ignoreCase)
	{
		this();
		this.ignoreCase = ignoreCase;
	}

	private PhraseTree(int treeSize, int treeDepth, boolean ignoreCase)
	{
		super(treeSize, treeDepth);
		this.ignoreCase = ignoreCase;
	}

	/**
	 * Adds a phrase to this phrase tree, starting with the string at phraseIndex
	 *
	 * @param thePhrase
	 * @param marker
	 * @param phraseIndex
	 *
	 *            Glenn Gobbel - Jul 17, 2014
	 */
	private void addPhrase(String[] thePhrase, Constants.ContextMark marker, int phraseIndex)
	{
		PhraseTree curTree;

		if (!containsKey(thePhrase[phraseIndex]))
		{
			curTree = new PhraseTree(getNextTreeSize(), treeDepth + 1, ignoreCase);
			put(thePhrase[phraseIndex], curTree);
			++phraseIndex;
		}
		else
		{
			curTree = get(thePhrase[phraseIndex]);
			++phraseIndex;
		}

		if (phraseIndex < thePhrase.length)
		{
			curTree.addPhrase(thePhrase, marker, phraseIndex);
		}
		else
		{
			curTree.phraseTag = marker;
		}
	}

	/**
	 * Adds the phrases in the list, thePhrases, to the phrase tree, with the
	 * marker, marker, at the end of the String arrays that make up the each phrase
	 * in the list
	 *
	 * Aug 15, 2017
	 *
	 * @param thePhrases
	 *            - A list of String array instances to be added to this PhraseTree
	 *            instance.
	 * @param marker
	 *            - A ContextMark enum value to be assigned to as the phraseTag for
	 *            each of the added String array instances.
	 *
	 */
	public void addPhrases(List<String[]> thePhrases, Constants.ContextMark marker)
	{
		for (String[] curPhrase : thePhrases)
		{
			addPhrase(curPhrase, marker, 0);
		}
	}

	@Override
	public boolean canEqual(Object other)
	{
		return (other instanceof PhraseTree);
	}

	/**
	 * Determines if the tokenStrings array parameter is contained within this
	 * phraseTree instance
	 *
	 * Aug 15, 2017
	 *
	 * @param tokenStrings
	 *            - Array of String objects representing a sentence. The tokens in
	 *            the array are assumed to be in the same order as found in the
	 *            sentence with tokenStrings[0] being the first token in the
	 *            sentence
	 * @return boolean - Return true is a phrase is found. Otherwise return false.
	 *         If the tokenStrings array is null or of zero length, return false.
	 */
	public boolean containsPhrase(String[] tokenStrings)
	{
		if ((tokenStrings == null) || (tokenStrings.length == 0))
		{
			return false;
		}

		PhraseTree curTree = this;
		int curIndex = 0;

		if (ignoreCase)
		{
			while ((curIndex < tokenStrings.length)
					&& ((curTree = curTree.get(tokenStrings[curIndex].toLowerCase())) != null))
			{
				++curIndex;
			}
		}
		else
		{
			while ((curIndex < tokenStrings.length) && ((curTree = curTree.get(tokenStrings[curIndex])) != null))
			{
				++curIndex;
			}
		}
		return (tokenStrings.length == curIndex);
	}

	public Constants.ContextMark getPhraseTag()
	{
		return phraseTag;
	}

	/**
	 * Method to find all the phraseTag Mark objects for the strings in the
	 * tokenStrings array starting at startIndex and continuing to the end of the
	 * array.
	 *
	 * @param tokenStrings
	 *            - Array of String objects representing a sentence. The tokens in
	 *            the array are assumed to be in the same order as found in the
	 *            sentence.
	 * @param startIndex
	 *            - Index indicating where in the tokenStrings array to search for
	 *            phrases contained in the array and in this PhraseTree object
	 * @return - A HashMap in which the keys are all the ContextMark enum values of
	 *         any phrases founded in this PhraseTree instance, and the values are
	 *         lists of the lengths of the found phrases
	 */
	public HashMap<Constants.ContextMark, List<Integer>> getPhraseTags(String[] tokenStrings, int startIndex)
	{
		if (tokenStrings == null)
		{
			return null;
		}

		HashMap<Constants.ContextMark, List<Integer>> foundMarkList = new HashMap<Constants.ContextMark, List<Integer>>();

		PhraseTree curTree = this;
		int phraseIndex = startIndex;
		Constants.ContextMark foundMark;

		while (phraseIndex < tokenStrings.length)
		{
			/*
			 * Stop if there is no further part of the tokenString array in the current
			 * phraseTree object
			 */
			if ((curTree = curTree.get(tokenStrings[phraseIndex])) == null)
			{
				return foundMarkList;
			}

			/*
			 * If a marking phraseTag is found in the current phraseTree, add it to the
			 * HashMap of results
			 */
			if ((foundMark = curTree.getPhraseTag()) != null)
			{
				List<Integer> indexList;
				if ((indexList = foundMarkList.get(foundMark)) == null)
				{
					indexList = new ArrayList<Integer>();
					foundMarkList.put(foundMark, indexList);
				}
				indexList.add(phraseIndex);
			}

			/*
			 * Increase phraseIndex so we can test the string at the next index in
			 * tokenStrings to see if it is in the curTree phraseTree object
			 */
			++phraseIndex;
		}

		return foundMarkList;
	}

	public Iterator<Pair<List<String>, PhraseTree>> iterator()
	{
		return new PhraseTreeIterator();
	}

	/**
	 * Given an array of token strings, this method finds whether any sequence of
	 * the strings in the array, starting at 'startIndex' and going to the length of
	 * the string, maps to a tree for which the phraseTag field is equal to the
	 * parameter 'markToFind.'
	 *
	 *
	 * @param tokenStrings
	 *            - Array of String objects representing a sentence. The tokens in
	 *            the array are assumed to be in the same order as found in the
	 *            sentence.
	 * @param startIndex
	 *            - Index indicating where in the tokenStrings array to search for
	 *            phrases contained in the array and in this PhraseTree object
	 * @param markToFind
	 *            - A ContextMark enum value. For a phrase within the tokenStrings
	 *            array to be found in this PhraseTree, the markToFind must match
	 *            the phraseTag of the subcomponent PhraseTree found within this
	 *            PhraseTree.
	 * @return - The length of the longest phrase found. Returns 0 if no phrase
	 *         found.
	 */
	public int phraseLengthAtIndex(String[] tokenStrings, int startIndex, Constants.ContextMark markToFind)
	{
		if ((tokenStrings == null) || (startIndex >= tokenStrings.length))
		{
			return 0;
		}

		int curMarkerLocation = startIndex;
		int curIndex = startIndex;
		PhraseTree curTree = this;

		if (ignoreCase)
		{

			while ((curIndex < tokenStrings.length)
					&& ((curTree = curTree.get(tokenStrings[curIndex].toLowerCase())) != null))
			{
				++curIndex;
				if (curTree.phraseTag == markToFind)
				{
					curMarkerLocation = curIndex;
				}
			}
		}
		else
		{
			while ((curIndex < tokenStrings.length) && ((curTree = curTree.get(tokenStrings[curIndex])) != null))
			{
				++curIndex;
				if (curTree.phraseTag == markToFind)
				{
					curMarkerLocation = curIndex;
				}
			}
		}
		return curMarkerLocation - startIndex;
	}

	/**
	 * Method added only to speed processing and avoid a test for ignore case, which
	 * is carried out by phraseLengthAtIndex. Given an array of token strings, this
	 * method finds whether any sequence of the strings in the array, starting at
	 * 'startIndex' and going to the length of the string, maps to a tree for which
	 * the phraseTag field is equal to the parameter 'markToFind.'
	 *
	 * @param tokenStrings
	 *            - Array of String objects representing a sentence. The tokens in
	 *            the array are assumed to be in the same order as found in the
	 *            sentence.
	 * @param startIndex
	 *            - Index indicating where in the tokenStrings array to search for
	 *            phrases contained in the array and in this PhraseTree object
	 * @param markToFind
	 *            - A ContextMark enum value. For a phrase within the tokenStrings
	 *            array to be found in this PhraseTree, the markToFind must match
	 *            the phraseTag of the subcomponent PhraseTree found within this
	 *            PhraseTree.
	 * @return - The length of the longest phrase found. Returns 0 if no phrase
	 *         found.
	 */
	public int phraseLengthAtIndexIgnoreCase(String[] tokenStrings, int startIndex, Constants.ContextMark markToFind)
	{
		if ((tokenStrings == null) || (startIndex >= tokenStrings.length))
		{
			return 0;
		}

		int curMarkerLocation = startIndex;
		int curIndex = startIndex;
		PhraseTree curTree = this;

		while ((curIndex < tokenStrings.length)
				&& ((curTree = curTree.get(tokenStrings[curIndex].toLowerCase())) != null))
		{
			++curIndex;
			if (curTree.phraseTag == markToFind)
			{
				curMarkerLocation = curIndex;
			}
		}

		return curMarkerLocation - startIndex;
	}

	public void print()
	{
		StringBuilder sb = new StringBuilder();
		print(sb);
	}

	private void print(StringBuilder tabStrings)
	{
		int length = tabStrings.length() / 4;
		String startTabs = tabStrings.toString();
		System.out.println("\n" + startTabs + "----------------------");
		System.out.println(startTabs + "BeginLevel:" + length);
		System.out.println(startTabs + "----------------------");

		tabStrings.append("    ");

		for (String curKey : keySet())
		{
			System.out.println(tabStrings + curKey);

			PhraseTree nextTree = get(curKey);
			if (nextTree.isEmpty() && nextTree.phraseTag != null)
			{
				System.out.println(tabStrings + "Tag:" + nextTree.phraseTag);
			}
			else
			{
				nextTree.print(new StringBuilder(tabStrings));
			}

			System.out.println();
		}
		System.out.println("\n" + startTabs + "----------------------");
		System.out.println(startTabs + "EndLevel:" + length);
		System.out.println(startTabs + "----------------------");

	}
}
