# PiModule

This is a simple tool for labeling text-based concepts specified in a tab-delimited lexicon.  The lexicon file specifies both 'phrases of interest' for labeling as particular concepts and phrases that are either required for ('promoters') or block ('inhibitor') labeling.  The tool has been tested within Eclipse (version Photon) and includes an example lexicon that will be used by default.

### Running the Tool within Eclipse
1.  **Import the downloaded project folder** into Eclipse (Menu: 'File' > 'Open Projects from File System ...').
1.  Right-click the 'pom.xml' file within the PiModule project within Eclipse and **run 'Maven Clean'** (Menu: 'Run As' > 'Maven clean').
1.  **Update the project** by right-clicking the top PiModule project folder in Eclipse (Menu: 'Maven' > 'Update Project ...').  Make sure the PiModule project is selected in the dialog box that subsequently appears and select 'OK'.
1.  Navigate to the 'PiMaker.java' file within the package 'gov.va.tvhs.grecc.PiModule.core' in the src/main/java folder within the project, right-click and **run as a Java application** (Menu: 'Run As' > 'Java Application').
