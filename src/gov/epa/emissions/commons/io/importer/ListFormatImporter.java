package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.DbServer;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains all the common features for importing files which are
 * delimited.
 */
public abstract class ListFormatImporter extends FormattedImporter {
    public static final String COMMA_REGEX = ",";

    public static final String WHITESPACE_REGEX = "\\s";

    private static final char SINGLE_QUOTE_TOKEN = '\'';

    private static final String END_OF_LINE_COMMENT = "!";

    /* regular expressions used for splits */
    private String splitRegularExpression;

    private String quoteToken;

    private String blankValue;

    private Pattern /* pattern, */quotePattern;

    private Matcher /* matcher, */quoteMatcher;

    private int index;

    private int previousEnd;

    private boolean endOfLineComment;

    // private boolean firstLineHeader;

    public ListFormatImporter(DbServer dbServer, String splitRegex, boolean useTransactions, boolean multipleOccurances) {
        super(dbServer);
        splitRegularExpression = splitRegex;
        this.useTransactions = useTransactions;
        // add quantifier to find regular expression one or more times
        if (multipleOccurances) {
            splitRegularExpression += "+";// greedy
            // splitRegularExpression += "+?";//reluctant
            // splitRegularExpression += "++";//possessive
        }
        // TODO have variable for quoteToken; what about those that do not have
        // quote pattern?
        // pattern = Pattern.compile(splitRegularExpression);
        quoteToken = String.valueOf(SINGLE_QUOTE_TOKEN);
        quotePattern = Pattern.compile(quoteToken);
        blankValue = "-9";
        endOfLineComment = false;
    }

    public String[] breakUpLine(String line, int[] widths) throws Exception {
        // create a list to hold the split up strings
        String[] stringlets = new String[widths.length];
        // fill stringlets with empty Strings
        Arrays.fill(stringlets, "");

        // TODO what about those that do not have quote pattern?
        // first split on quotes
        quoteMatcher = quotePattern.matcher(line);

        // Parse, alternating between non-quoted text that must be split-up
        // and quoted text which is accepted as-is. It always begins with
        // non-quoted text, even if the first character in the line
        // is the quote symbol.
        boolean nonQuotedText = true;
        index = 0;
        previousEnd = 0;
        endOfLineComment = false;
        while (quoteMatcher.find() && !endOfLineComment) {
            String text = line.substring(previousEnd, quoteMatcher.start());
            if (nonQuotedText) {
                stringlets = parseNonQuotedText(stringlets, text, line);
            } else {
                stringlets = parseQuotedText(stringlets, text, line);
            }

            nonQuotedText = !nonQuotedText;
            previousEnd = quoteMatcher.end();
        }// while(quoteMatcher.find())

        // get the final quote delimited token (non-quoted text)
        // 1) entire line if no quotes in line
        // 2) rest of line if line does not end with quote
        // 3) empty string if line ends with quote
        String text = line.substring(previousEnd);
        stringlets = parseNonQuotedText(stringlets, text, line);

        return stringlets;
    }

    private String[] parseNonQuotedText(String[] stringlets, String text, String line) throws Exception {
        String[] subsplit = text.split(splitRegularExpression);
        for (int i = 0; i < subsplit.length; i++) {
            if (subsplit[i].length() > 0) {
                // check for end of line comment
                if (subsplit[i].startsWith(END_OF_LINE_COMMENT)) {
                    endOfLineComment = true;
                    break;// break for i
                }
                // check for index out of bounds
                checkForIndexOutOfBounds(index, stringlets, line);
                // set value
                stringlets[index] = subsplit[i];
                // check for blank value
                if (stringlets[index].equals(blankValue)) {
                    stringlets[index] = "";
                }
                index++;
            }
        }
        return stringlets;
    }

    private String[] parseQuotedText(String[] stringlets, String text, String line) throws Exception {
        // make sure that quotes are properly delimited
        String checkStart = "";
        String checkEnd = "";
        String[] checkStartSplit = null;
        String[] checkEndSplit = null;

        // get the String value immediately before the quote token
        if (previousEnd != 1) {
            checkStart = line.substring(previousEnd - 2, previousEnd - 1);
            checkStartSplit = checkStart.split(splitRegularExpression);
        }
        // get the String value immediately after the quote token
        if (quoteMatcher.end() != line.length()) {
            checkEnd = line.substring(quoteMatcher.end(), quoteMatcher.end() + 1);
            checkEndSplit = checkEnd.split(splitRegularExpression);
        }

        if ((checkStart.length() > 0 && checkStartSplit.length != 0)
                || (checkEnd.length() > 0 && checkEndSplit.length != 0)) {
            throw new Exception("The quoted token " + checkStart + quoteToken + text + quoteToken + checkEnd
                    + " is not properly delimited in line\n" + line);
        }

        // check for index out of bounds
        checkForIndexOutOfBounds(index, stringlets, line);
        stringlets[index] = text;
        index++;

        return stringlets;
    }
}
