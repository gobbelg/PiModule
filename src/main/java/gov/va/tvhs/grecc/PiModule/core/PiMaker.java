package gov.va.tvhs.grecc.PiModule.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.log4j.*;

/**
 * A console-based application can be started with the main method of this
 * class, which will allow a user to test for context-dependent concepts in
 * single sentences provided by the user. The user can specify the path to
 * configuration file indicating promoters, inhibitors, and concept phrases as a
 * program argument. Otherwise, a default configuration file
 * ("/resources/piSampleDictionary_170808.txt") will be used for configuration.
 *
 * <p>
 * Instances of this class can also be used to identify concepts based on
 * context using calls to prcessSentence(String sentence) or
 * processStrings(String [] stringArray). PiMaker objects can be created via
 * calls to the constructor, PiMaker(String conigFilePath),
 *
 * @author Glenn Gobbel
 *
 */
/**
 * @author Glenn Gobbel
 *
 */
public class PiMaker
{
	/**
	 * Users can add other RunType fields to switch to other user defined methods
	 * from the main method of the PiMaker class.
	 *
	 * @author Glenn Gobbel
	 *
	 */
	private static enum RunType
	{
		CYCLE_USER_INPUT;
	}

	private static final Logger LOGGER = Logger.getLogger(PiMaker.class);

	static
	{
		LOGGER.setLevel(Level.DEBUG);
	}

	public static void main(final String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			/**
			 * Cycles through user input of sentences via the console and returns the
			 * concepts found in the sentences Aug 15, 2017
			 *
			 * @param piModule
			 *            void
			 */
			protected void cycleUserInput(PiMaker piModule)
			{
				String prompt = ("\n\nEnter sentence with words separated by white space:\n(Enter 'exit' or 'quit' to stop)");
				System.out.println(prompt);
				Scanner scanner = new Scanner(System.in);

				String testString = scanner.nextLine();
				while (!(testString.equalsIgnoreCase("exit")) && !(testString.equalsIgnoreCase("quit")))
				{
					if (testString.trim().length() > 0)
					{
						piModule.processSentence(testString);
					}
					else
					{
						System.out.println("Enter a non-empty string");
					}

					System.out.println(prompt);
					testString = scanner.nextLine();
				}

				scanner.close();
				System.exit(0);
			}

			public void run()
			{
				RunType runType = RunType.CYCLE_USER_INPUT;
				switch (runType)
				{
				case CYCLE_USER_INPUT:

					BufferedReader br = null;
					PiMaker piModule = null;
					if (args != null && args.length > 0)
					{
						piModule = new PiMaker(args[0]);
						System.out.println("Dictionary Path:" + args[0]);
					}
					else
					{
						InputStream defaultDictionaryStream = getClass().getResourceAsStream("/piSampleDictionary_170808.txt");
						br = new BufferedReader(new InputStreamReader(defaultDictionaryStream));
						piModule = new PiMaker(br);
						System.out.println("Reading from default PI Module dictionary");
					}

					if (piModule != null)
					{
						System.out.println("Running Pi Module");
						if (LOGGER.isDebugEnabled())
						{
							piModule.contextAnalyzer.getPhraseMapper().printMaps();
						}
						cycleUserInput(piModule);
					}
					if (br != null)
					{
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					break;

				default:

					break;
				}
			}

		});

	}

	/**
	 * Stores the configuration information in terms of promoters, inhibitors, and
	 * concept phrases
	 */
	private TokenContextAnalyzer contextAnalyzer;

	/**
	 * Constructor that takes a path to a configuration file and returns a PiMaker
	 * instance.
	 *
	 * @param configFilePath
	 *            Full path to the file used to configure the application. It
	 *            contains the phrases mapping to concepts, promoters, and
	 *            inhibitors.
	 */
	public PiMaker(String configFilePath)
	{
		createContextAnalyzer(configFilePath);
	}
	
	public PiMaker(BufferedReader br)
	{
		if (br != null)
		{
			contextAnalyzer = new TokenContextAnalyzer(br);
		}
	}

	
	
	private void createContextAnalyzer(String configFilePath)
	{
		if (configFilePath != null && !configFilePath.isEmpty())
		{
			if (!new File(configFilePath).exists())
			{
				String theError = "No dictionary found at:\n\n    " + configFilePath;
				JOptionPane.showMessageDialog(null, theError);
				System.exit(-1);
			}

			LOGGER.info("Configuring Pi module with file at:" + configFilePath);
			contextAnalyzer = getTokenContextAnalyzer(configFilePath);
			LOGGER.info("Pi module configuration complete");

		}
	}

	/**
	 * Aug 15, 2017
	 *
	 * @param dictionaryPath
	 * @return TokenContextAnalyzer
	 */
	private TokenContextAnalyzer getTokenContextAnalyzer(String dictionaryPath)
	{
		if (dictionaryPath != null)
		{
			contextAnalyzer = new TokenContextAnalyzer(dictionaryPath);
		}

		return contextAnalyzer;
	}
	

	/**
	 * Aug 15, 2017
	 *
	 * One of two key methods in the class to identify concepts within a sentence.
	 * This method takes a string of characters representing a sentence, splits it
	 * by whitespace, periods, commas, colons, and semicolons, and returns concepts
	 * based on the configuration file supplied to the PiMaker instance constructor.
	 * The concepts are returned as a collection of TokenStringTag instances.
	 *
	 * @param sentenceString
	 *            String representing the sentence to be analyzed for contextual
	 *            phrases
	 * @return Collection of TokenStringTag instances corresponding to the phrases
	 *         contained in the sentence that map to concepts
	 */
	public Collection<TokenStringTag> processSentence(String sentenceString)
	{
		/*
		 * Split on spaces, commas, periods, semicolons, or colons but discard only
		 * whitespace
		 */
		String[] sentenceTokens = sentenceString.toLowerCase().split("(\\s+)|(?=[.,;:])|(?<=[.,;:])");
		return processStrings(sentenceTokens);
	}

	/**
	 * One of two key methods in the class to identify concepts within a sentence.
	 * Takes an array of strings and determines the context based on an existing
	 * configuration file. The strings are assumed to be in the same order as found
	 * in a sentence with the leftmost string assigned to sentenceStrings[0]. The
	 * concepts are returned as a collection of TokenStringTag instances.
	 *
	 * @param sentenceStrings
	 *            Array of string representing the characters within each of the
	 *            tokens making up the sentence
	 * @return Collection of TokenStringTag instances corresponding to the phrases
	 *         contained in the sentence that map to concepts
	 */
	public Collection<TokenStringTag> processStrings(String[] sentenceStrings)
	{
		LOGGER.debug("\nProcessing:" + Arrays.toString(sentenceStrings));

		Collection<TokenStringTag> resultConcepts = new ArrayList<TokenStringTag>(sentenceStrings.length);

		if ((contextAnalyzer != null) && (contextAnalyzer.getPhraseMapper() != null))
		{
			resultConcepts = contextAnalyzer.getTokenStringTags(sentenceStrings);
			if (resultConcepts.isEmpty())
			{
				System.out.println("No concepts found");
			}
			else
			{
				int i = 0;
				for (TokenStringTag tag : resultConcepts)
				{
					System.out.println("Tagged Concept " + i + ")" + tag.toString());
				}
			}
		}
		else
		{
			System.err.println("No PhraseMapper instance available for phrase-to-concept mapping");
		}

		return resultConcepts;
	}

}
