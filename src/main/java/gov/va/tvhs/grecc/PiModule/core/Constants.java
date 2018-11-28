package gov.va.tvhs.grecc.PiModule.core;

/**
 * Holds global constants used across the project
 *
 * @author Glenn Gobbel, Jul 25, 2017
 *
 */
public interface Constants
{

	/**
	 * Used to indicate whether a given sentence token is part of a concept phrase,
	 * promoter, or inhibitor or, alternatively, promoted or inhibited by a nearby
	 * token
	 *
	 * @author Glenn Gobbel
	 *
	 */
	public static enum ContextMark
	{
		CONCEPTWORD, PROMOTER, INHIBITOR, INHIBITED, PROMOTED, NONE
	}

	// Size of window to how far inhibitors or promoters can be and still
	// affect whether a particular phrase maps to a concept
	public static final int DEFAULT_CONTEXT_WINDOW_RADIUS = 10;

	// This is based on the number of unique first words of phrases
	// in initial testing data. It can be decreased in size which may save memory,
	// but this may increase the time required to read in a configuration file.
	public static final int HASH_TREE_SIZE_DEPTH_0_DEFAULT = 10000;

	// This is based on the number of unique second words of phrases
	// in initial testing data
	public static final int HASH_TREE_SIZE_DEPTH_1_DEFAULT = 8;

	// This is based on the number of unique third words of phrases
	// in initial testing data
	public static final int HASH_TREE_SIZE_DEPTH_2_DEFAULT = 4;

	// This is based on the number of unique fourth or higher words of phrases
	// in initial testing data
	public static final int HASH_TREE_SIZE_DEPTH_OVER2_DEFAULT = 2;

}
