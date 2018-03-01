import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String FILE_READ_PATH = "/Users/xyz/study/GU semester 2/Information Retrieval/BigSample/";

    private static final String[] FILE_NAME = {"fr940104.0", "fr940104.2", "fr940128.2", "fr940303.1", "fr940405.1",
            "fr940525.0", "fr940617.2", "fr940810.0", "fr940810.2", "fr941006.1", "fr941206.1"};

    class ListNode {
        int termId;
        String term;
        String docId;
        int termFrequency = 0;

        ListNode next;

        ListNode(String x) {
            term = x;
        }
    }

    public class ListPositionalNode extends ListNode {
        List<Integer> postions = new ArrayList<>();

        ListPositionalNode next;

        ListPositionalNode(String x) {
            super(x);
        }
    }

    private long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);

//        Calendar calendar1 = Calendar.getInstance();
//        calendar1.setTimeInMillis(startTime);
//        System.out.println("Start time: " +
//                calendar1.get(Calendar.HOUR_OF_DAY) + ":" + calendar1.get(Calendar.MINUTE) + ":" +
//                calendar1.get(Calendar.SECOND) + ":" + calendar1.get(Calendar.MILLISECOND));


        Main m = new Main();
        m.process();

        long endTime = System.currentTimeMillis();
        System.out.println("End time: " + endTime);
        System.out.println("Total time: " + (endTime - startTime));

//        Calendar calendar2 = Calendar.getInstance();
//        calendar2.setTimeInMillis(endTime);
//        System.out.println("End time: " + calendar2.get(Calendar.HOUR_OF_DAY) + ":" + calendar2.get(Calendar.MINUTE) +
//                ":" + calendar2.get(Calendar.SECOND) + ":" + calendar2.get(Calendar.MILLISECOND));
//
//        System.out.println("Total time: " +
//                String.valueOf((calendar2.get(Calendar.HOUR_OF_DAY) - calendar1.get(Calendar.HOUR_OF_DAY)) * 3600 * 1000 +
//                        (calendar2.get(Calendar.MINUTE) - calendar1.get(Calendar.MINUTE)) * 60 * 1000 +
//                        (calendar2.get(Calendar.SECOND) - calendar1.get(Calendar.SECOND)) * 1000 +
//                        (calendar2.get(Calendar.MILLISECOND) - calendar1.get(Calendar.MILLISECOND))) + " ms");
    }

    /**
     * process
     */
    public void process() throws IOException {

        List<List<ListNode>> singleTermIndex = new ArrayList<>();
        List<List<ListPositionalNode>> singleTermPositionalIndex = new ArrayList<>();
        List<List<ListNode>> stemIndex = new ArrayList<>();
        List<List<ListNode>> phraseIndex = new ArrayList<>();

        Set<String> stopWordSet = readStopWords(FILE_READ_PATH + "stops.txt");

        Map<String, String> map = new HashMap<>();
        for (String fileName : FILE_NAME) {
            Map<String, String> tempMap = readText(FILE_READ_PATH + fileName);
            map.putAll(tempMap);
        }
//        StringBuilder sb = new StringBuilder();

        Set<String> sets = map.keySet();
        List<String> docIdLists = new ArrayList<>(sets);
        Collections.sort(docIdLists);


        for (String docId : docIdLists) {
            List<ListNode> tempSingleTermIndex = new ArrayList<>();
            List<ListPositionalNode> tempSingleTermPositionalIndex = new ArrayList<>();
            List<ListNode> tempStemIndex = new ArrayList<>();
            List<ListNode> tempPhraseIndex = new ArrayList<>();

            String text = map.get(docId);
            text = processEscapeSequences(text);
            text = parsingSingleTerms(text);


            String[] singleTerms = text.split(" ");

            int positionalCount = 0;//count position

            StringBuilder phraseBuilder = new StringBuilder();
            int cnt = 0;
            int countPhrase = 0;

            for (String singleTerm : singleTerms) {
                if (singleTerm.equals("")) {
                    continue;
                }

                //build phrase index

                boolean phraseFlag = false;

                if (!stopWordSet.contains(singleTerm) && singleTerm == singleTerms[singleTerms.length - 1]
                        && (countPhrase == 1 || countPhrase == 2) && (isNumeric(singleTerm) || isAlphabetic(singleTerm))) {
                    phraseBuilder.append(" " + singleTerm);
                    countPhrase++;
                }

                if (stopWordSet.contains(singleTerm) || singleTerm == singleTerms[singleTerms.length - 1]) {
                    if (countPhrase == 2 || countPhrase == 3) {
                        for (ListNode phraseNode : tempPhraseIndex) {
                            if (phraseNode.term.equals(phraseBuilder.toString())) {
                                phraseFlag = true;
                                phraseNode.termFrequency++;
                                break;
                            }
                        }
                        if (!phraseFlag) {
                            ListNode phraseNode = new ListNode(phraseBuilder.toString());
                            phraseNode.termFrequency++;
                            phraseNode.docId = docId;
                            tempPhraseIndex.add(phraseNode);
                        }
                    }
                    phraseBuilder = new StringBuilder();
                    countPhrase = 0;
                } else if ((isNumeric(singleTerm) || isAlphabetic(singleTerm))) {
                    countPhrase++;
                    phraseBuilder.append(" " + singleTerm);
                } else {
                    phraseBuilder = new StringBuilder();
                    countPhrase = 0;
                }


                //build single index

                positionalCount++;

                if (isNumeric(singleTerm) || isAlphabetic(singleTerm)) {
                    boolean singleFlag = false;
                    boolean positionalFlag = false;
                    boolean stemFlag = false;

                    //repeated emergence

                    //single term index
                    for (ListNode aSingleTerm : tempSingleTermIndex) {
                        if (aSingleTerm.term.equals(singleTerm)) {
                            singleFlag = true;
                            aSingleTerm.termFrequency++;
                            break;
                        }
                    }

                    //positional index
                    for (ListPositionalNode positionalTerm : tempSingleTermPositionalIndex) {
                        if (positionalTerm.term.equals(singleTerm)) {
                            positionalFlag = true;
                            positionalTerm.termFrequency++;
                            positionalTerm.postions.add(positionalCount);
                            break;
                        }
                    }

                    //stem index
                    if (isAlphabetic(singleTerm)) {
                        Stemmer stemmer = new Stemmer();
                        stemmer.add(singleTerm.toCharArray(), singleTerm.length());
                        stemmer.stem();
                        for (ListNode stemTerm : tempStemIndex) {
                            if (stemTerm.term.equals(stemmer.toString())) {
                                stemFlag = true;
                                stemTerm.termFrequency++;
                                break;
                            }
                        }
                    }

//                    first appear

                    //single term index
                    if (!singleFlag) {
                        ListNode tempSingleTermNode = new ListNode(singleTerm);
                        cnt++;
                        tempSingleTermNode.termFrequency++;
                        tempSingleTermNode.docId = docId;
                        tempSingleTermIndex.add(tempSingleTermNode);
                    }

                    //positional index
                    if (!positionalFlag) {
                        ListPositionalNode tempPositionalTermNode = new ListPositionalNode(singleTerm);
                        tempPositionalTermNode.termFrequency++;
                        tempPositionalTermNode.docId = docId;
                        tempPositionalTermNode.postions.add(positionalCount);
                        tempSingleTermPositionalIndex.add(tempPositionalTermNode);
                    }

                    //stem index
                    if (!stemFlag) {
                        if (isAlphabetic(singleTerm)) {
                            Stemmer stemmer = new Stemmer();
                            stemmer.add(singleTerm.toCharArray(), singleTerm.length());
                            stemmer.stem();
                            ListNode tempStemNode = new ListNode(stemmer.toString());
                            tempStemNode.termFrequency++;
                            tempStemNode.docId = docId;
                            tempStemIndex.add(tempStemNode);
                        }
                    }

                } else {
                    List<String> tempStrings = new ArrayList<>();
                    tempStrings = identifyIPAddress(singleTerm, tempStrings);
                    tempStrings = identifyEmailAddress(singleTerm, tempStrings);
                    tempStrings = identifyURL(singleTerm, tempStrings);
                    tempStrings = identifyFileExtensions(singleTerm, tempStrings);
                    tempStrings = changeDigitFormats(singleTerm, tempStrings);
                    tempStrings = normalize(singleTerm, tempStrings);
                    tempStrings = keepMonetaryValues(singleTerm, tempStrings);
                    tempStrings = identifyAlphabetDigit(singleTerm, tempStrings);
                    tempStrings = identifyDigitAlphabet(singleTerm, tempStrings);
                    tempStrings = identifyHyphenatedTerms(singleTerm, tempStrings);

                    if (tempStrings.size() != 0) {
                        for (String tempString : tempStrings) {
                            if (tempString.equals("")) {
                                continue;
                            }

                            boolean singleFlag = false;

                            for (ListNode aSingleTerm : tempSingleTermIndex) {
                                if (aSingleTerm.term.equals(tempString) && !stopWordSet.contains(tempString)) {
                                    singleFlag = true;
                                    aSingleTerm.termFrequency++;
                                    break;
                                }
                            }

                            if (!singleFlag) {
                                if (!stopWordSet.contains(tempString)) {
                                    ListNode tempSingleTermNode = new ListNode(tempString);
                                    cnt++;
                                    tempSingleTermNode.termFrequency++;
                                    tempSingleTermNode.docId = docId;
                                    tempSingleTermIndex.add(tempSingleTermNode);
                                }
                            }

                        }

                    } else {
                        String[] tempOtherSpecialStrings =
                                singleTerm.split("@|\\^|\\*|#|-|\\$|¢|/|§|×|&|<|>|≥|\"|‘|’|¶|○|•|®|'|_|%|\\.");

                        for (String tempString : tempOtherSpecialStrings) {

                            if (tempString.equals("")) {
                                continue;
                            }

                            boolean singleFlag = false;

                            for (ListNode aSingleTerm : tempSingleTermIndex) {
                                if (aSingleTerm.term.equals(tempString) && !stopWordSet.contains(tempString)) {
                                    singleFlag = true;
                                    aSingleTerm.termFrequency++;
                                    break;
                                }
                            }

                            if (!singleFlag) {
                                if (!stopWordSet.contains(tempString)) {
                                    ListNode tempSingleTermNode = new ListNode(tempString);
                                    cnt++;
                                    tempSingleTermNode.termFrequency++;
                                    tempSingleTermNode.docId = docId;
                                    tempSingleTermIndex.add(tempSingleTermNode);
                                }
                            }
                        }
                    }
                }
                if(cnt == 1000){
                    break;
                }
            }

//            sb.append(text);
//
            tempSingleTermIndex = sortOneListByTerm(tempSingleTermIndex);
            tempSingleTermPositionalIndex = sortOnePositionalListByTerm(tempSingleTermPositionalIndex);
            tempStemIndex = sortOneListByTerm(tempStemIndex);
            tempPhraseIndex = sortOneListByTerm(tempPhraseIndex);

            singleTermIndex.add(tempSingleTermIndex);
            singleTermPositionalIndex.add(tempSingleTermPositionalIndex);
            stemIndex.add(tempStemIndex);
            phraseIndex.add(tempPhraseIndex);

            saveListNodeAsCsvFile(tempSingleTermIndex, FILE_READ_PATH + "singleTermIndex/" + docId);
            savePositionalListNodeAsCsvFile(tempSingleTermPositionalIndex, FILE_READ_PATH + "positionalIndex/" + docId);
            saveListNodeAsCsvFile(tempStemIndex, FILE_READ_PATH + "stemIndex/" + docId);
            saveListNodeAsCsvFile(tempPhraseIndex, FILE_READ_PATH + "phraseIndex/" + docId);

            if(cnt == 1000){
                break;
            }
        }
        // calculate time
        long tempTime1 = System.currentTimeMillis();
        System.out.println("Time 1: " + tempTime1);
        System.out.println("Time taken to create temporary files: " + (tempTime1 - startTime));


//        //            saveAsFile(sb.toString(), FILE_READ_PATH + "output" + fileName);
//        saveAllListAsCsvFile(singleTermIndex, FILE_READ_PATH + "singleTermIndex");
//        saveAllPositionalListAsCsvFile(singleTermPositionalIndex, FILE_READ_PATH + "positionalIndex");
//        saveAllListAsCsvFile(stemIndex, FILE_READ_PATH + "stemIndex");
//        saveAllListAsCsvFile(phraseIndex, FILE_READ_PATH + "phraseIndex");

        List<ListNode> completedSingleTermIndex = mergeSortListNode(singleTermIndex);
        saveFinalListAsCsvFile(completedSingleTermIndex, FILE_READ_PATH + "singleTermIndex");

        int n = completedSingleTermIndex.size();
        int maxDfSingle = 0;
        int minDfSingle = Integer.MAX_VALUE;
        int meanDfSingle = 0;
        int medianDfSingle = 0;
        int countSingle = 0;
        System.out.println("***single term index: ");
        System.out.println("Lexicon: " + n);
        for (int i = 0; i < n; i++) {
            ListNode p = completedSingleTermIndex.get(i);
            int tempDf = 0;
            while (p != null) {
                countSingle++;
                tempDf += p.termFrequency;
                p = p.next;
            }
            if (i == n / 2) {
                medianDfSingle = tempDf;
            }
            maxDfSingle = Math.max(maxDfSingle, tempDf);
            minDfSingle = Math.min(minDfSingle, tempDf);
            meanDfSingle += tempDf;
        }
        meanDfSingle = meanDfSingle / n;
        System.out.println("Index size: " + countSingle);
        System.out.println("Max df: " + maxDfSingle);
        System.out.println("Min df: " + minDfSingle);
        System.out.println("Mean df: " + meanDfSingle);
        System.out.println("Median df: " + medianDfSingle);

        List<ListPositionalNode> completedPositionalIndex = mergeSortPositionalListNode(singleTermPositionalIndex);
        saveFinalPositionalListAsCsvFile(completedPositionalIndex, FILE_READ_PATH + "positionalIndex");
        int n2 = completedPositionalIndex.size();
        int maxDfPosition = 0;
        int minDfPosition = Integer.MAX_VALUE;
        int meanDfPosition = 0;
        int medianDfPosition = 0;
        int countPosition = 0;
        System.out.println("***Single term positional index: ");
        System.out.println("Lexicon: " + n2);
        for (int i = 0; i < n2; i++) {
            ListPositionalNode p = completedPositionalIndex.get(i);
            int tempDf = 0;
            while (p != null) {
                countPosition++;
                tempDf += p.termFrequency;
                p = p.next;
            }
            if (i == n2 / 2) {
                medianDfPosition = tempDf;
            }
            maxDfPosition = Math.max(maxDfPosition, tempDf);
            minDfPosition = Math.min(minDfPosition, tempDf);
            meanDfPosition += tempDf;
        }
        meanDfPosition = meanDfPosition / n2;
        System.out.println("Index size: " + countPosition);
        System.out.println("Max df: " + maxDfPosition);
        System.out.println("Min df: " + minDfPosition);
        System.out.println("Mean df: " + meanDfPosition);
        System.out.println("Median df: " + medianDfPosition);

        List<ListNode> completedPhraseIndex = mergeSortListNode(phraseIndex);
        int m = completedPhraseIndex.size();
        for (int i = 0; i < m; i++) {
            int count = 0;
            ListNode p = completedPhraseIndex.get(i);
            while (p != null) {
                count += p.termFrequency;
                p = p.next;
            }
            if (count < 3) {
                completedPhraseIndex.remove(i);
                i--;
                m--;
            }
        }

        int n3 = completedPhraseIndex.size();
        int maxDfPhrase = 0;
        int minDfPhrase = Integer.MAX_VALUE;
        int meanDfPhrase = 0;
        int medianDfPhrase = 0;
        int countPhrase = 0;
        System.out.println("***Phrase index: ");
        System.out.println("Lexicon: " + n3);
        for (int i = 0; i < n3; i++) {
            ListNode p = completedPhraseIndex.get(i);
            int tempDf = 0;
            while (p != null) {
                countPhrase++;
                tempDf += p.termFrequency;
                p = p.next;
            }
            if (i == n3 / 2) {
                medianDfPhrase = tempDf;
            }
            maxDfPhrase = Math.max(maxDfPhrase, tempDf);
            minDfPhrase = Math.min(minDfPhrase, tempDf);
            meanDfPhrase += tempDf;
        }
        meanDfPhrase = meanDfPhrase / n3;
        System.out.println("Index size: " + countPhrase);
        System.out.println("Max df: " + maxDfPhrase);
        System.out.println("Min df: " + minDfPhrase);
        System.out.println("Mean df: " + meanDfPhrase);
        System.out.println("Median df: " + medianDfPhrase);

        saveFinalListAsCsvFile(completedPhraseIndex, FILE_READ_PATH + "phraseIndex");


        List<ListNode> completedStemIndex = mergeSortListNode(stemIndex);
        saveFinalListAsCsvFile(completedStemIndex, FILE_READ_PATH + "stemIndex");

        int n4 = completedStemIndex.size();
        int maxDfStem = 0;
        int minDfStem = Integer.MAX_VALUE;
        int meanDfStem = 0;
        int medianDfStem = 0;
        int countStem = 0;
        System.out.println("***Phrase index: ");
        System.out.println("Lexicon: " + n4);
        for (int i = 0; i < n4; i++) {
            ListNode p = completedStemIndex.get(i);
            int tempDf = 0;
            while (p != null) {
                countStem++;
                tempDf += p.termFrequency;
                p = p.next;
            }
            if (i == n4 / 2) {
                medianDfStem = tempDf;
            }
            maxDfStem = Math.max(maxDfStem, tempDf);
            minDfStem = Math.min(minDfStem, tempDf);
            meanDfStem += tempDf;
        }
        meanDfStem = meanDfStem / n4;
        System.out.println("Index size: " + countStem);
        System.out.println("Max df: " + maxDfStem);
        System.out.println("Min df: " + minDfStem);
        System.out.println("Mean df: " + meanDfStem);
        System.out.println("Median df: " + medianDfStem);

        // calculate time
        long tempTime2 = System.currentTimeMillis();
//        System.out.println("Time 2: " + tempTime2);
        System.out.println("Time taken to merge temp files: " + (tempTime2 - tempTime1));
    }

    public boolean isNumeric(String term) {
        for (int i = 0; i < term.length(); i++) {
            int chr = term.charAt(i);
            if (!(chr > 47 && chr < 58)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAlphabetic(String term) {
        for (int i = 0; i < term.length(); i++) {
            int chr = term.charAt(i);
            if (!((chr > 64 && chr < 91) || (chr > 96 && chr < 123))) {
                return false;
            }
        }
        return true;
    }

    /**
     * read file as a String
     *
     * @param fileName
     * @return
     */
    public Map<String, String> readText(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        Map<String, String> map = new HashMap();
        String tempDocId = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            int mark = 0;
            //read one line at one time, until read null
            //only save text between <TEXT> and </TEXT>, excluding comments <!-- -->
            while ((tempString = reader.readLine()) != null) {
                if (tempString.endsWith("</TEXT>")) {
                    mark = 0;
                    map.put(tempDocId, sb.toString());
                    sb = new StringBuilder();
                }
                // read and set docId as key, text as value in map
                if (tempString.contains("<DOCNO>")) {
                    String[] strings = tempString.split("<DOCNO>|</DOCNO>");
                    for (int i = 0; i < strings.length; i++) {
                        char[] ch = strings[i].toCharArray();
                        boolean isBlank = true;
                        for (int j = 0; j < ch.length; j++) {
                            if (ch[j] != ' ') {
                                isBlank = false;
                                break;
                            }
                        }
                        if (!isBlank) {
                            tempDocId = strings[i].trim();
                            break;
                        }
                    }
                }
                if (mark == 1 && !tempString.startsWith("<!--")) {
                    //for a line like <xxx> text </xxx>   ----->   text
                    if (tempString.contains("<")) {
                        String[] strings = tempString.split("<|/|>");
                        if (strings.length == 5) {
                            sb = appendWithBlankSpace(sb, strings[2]);
                        }
                    } else {
                        sb = appendWithBlankSpace(sb, tempString);
                    }
                }
                if (tempString.startsWith("<TEXT>")) {
                    mark = 1;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return map;
    }

    /**
     * a temp function for test
     * save a string to a file
     */
    public void saveAsFile(String text, String fileName) throws IOException {
        File file = new File(fileName);
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(text);
        out.close();
    }

    /**
     * @param list
     * @param fileName
     * @throws IOException
     */
    public void saveListNodeAsCsvFile(List<ListNode> list, String fileName) throws IOException {
        File file = new File(fileName + ".csv");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("term,termId,docId,termFrequency");
        for (int i = 0; i < list.size(); i++) {
            ListNode p = list.get(i);
            out.write("\r\n");
            out.write(p.term + ",");
            out.write(p.termId + ",");
            out.write(p.docId + ",");
            out.write(p.termFrequency + ",");
        }
        out.close();
    }

    /**
     * @param list
     * @param fileName
     * @throws IOException
     */
    public void savePositionalListNodeAsCsvFile(List<ListPositionalNode> list, String fileName) throws IOException {
        File file = new File(fileName + ".csv");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("term,termId,docId,termFrequency,positions");
        for (int i = 0; i < list.size(); i++) {
            ListPositionalNode p = list.get(i);
            out.write("\r\n");
            out.write(p.term + ",");
            out.write(p.termId + ",");
            out.write(p.docId + ",");
            out.write(p.termFrequency + ",");
            out.write(p.postions + ",");
        }
        out.close();
    }

    /**
     * @param list
     * @param fileName
     * @throws IOException
     */
    public void saveAllListAsCsvFile(List<List<ListNode>> list, String fileName) throws IOException {
        File file = new File(fileName + ".csv");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("term,termId,docId,termFrequency");
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                ListNode p = list.get(i).get(j);
                out.write("\r\n");
                out.write(p.term + ",");
                out.write(p.termId + ",");
                out.write(p.docId + ",");
                out.write(p.termFrequency + ",");
            }
        }
        out.close();
    }

    /**
     * @param list
     * @param fileName
     * @throws IOException
     */
    public void saveAllPositionalListAsCsvFile(List<List<ListPositionalNode>> list, String fileName) throws IOException {
        File file = new File(fileName + ".csv");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("term,termId,docId,termFrequency,positions");
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                ListPositionalNode p = list.get(i).get(j);
                out.write("\r\n");
                out.write(p.term + ",");
                out.write(p.termId + ",");
                out.write(p.docId + ",");
                out.write(p.termFrequency + ",");
                out.write(p.postions + ",");
            }
        }
        out.close();
    }

    /**
     * @param list
     * @param fileName
     * @throws IOException
     */
    public void saveFinalListAsCsvFile(List<ListNode> list, String fileName) throws IOException {
        File file = new File(fileName + ".csv");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("term,termId,docId,termFrequency");
        for (int i = 0; i < list.size(); i++) {
            ListNode p = list.get(i);
            while (p != null) {
                out.write("\r\n");
                out.write(p.term + ",");
                out.write(p.termId + ",");
                out.write(p.docId + ",");
                out.write(p.termFrequency + ",");
                p = p.next;
            }
        }
        out.close();
    }


    /**
     * @param list
     * @param fileName
     * @throws IOException
     */
    public void saveFinalPositionalListAsCsvFile(List<ListPositionalNode> list, String fileName) throws IOException {
        File file = new File(fileName + ".csv");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write("term,termId,docId,termFrequency,positions");
        for (int i = 0; i < list.size(); i++) {
            ListPositionalNode p = list.get(i);
            while (p != null) {
                out.write("\r\n");
                out.write(p.term + ",");
                out.write(p.termId + ",");
                out.write(p.docId + ",");
                out.write(p.termFrequency + ",");
                out.write(p.postions + ",");
                p = p.next;
            }
        }
        out.close();
    }

    /**
     * goal: there is always a blankSpace between last word of sb and a new line
     */

    public StringBuilder appendWithBlankSpace(StringBuilder sb, String tempString) {
        if (tempString.endsWith(" ") || tempString.equals("")) {
            return sb.append(tempString);
        } else {
            return sb.append(tempString).append(' ');
        }
    }

    /**
     * process html5 characters
     */
    public String processEscapeSequences(String str) {
        str = str.replaceAll("&blank;", " ");
        str = str.replaceAll("&hyph;", "-");
        str = str.replaceAll("&sect;", "§");
        str = str.replaceAll("&times", "×");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&ge;", "≥");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&lsquo;", "‘");
        str = str.replaceAll("&rsquo;", "’");
//        str = str.replaceAll("&para;", " ");
//        str = str.replaceAll("&cir;", " ");
//        str = str.replaceAll("&bull;", " ");
//        str = str.replaceAll("&reg;", " ");
//        str = str.replaceAll("&cent;", " ");
        str = str.replaceAll("&para;", "¶");
        str = str.replaceAll("&cir;", "○");
        str = str.replaceAll("&bull;", "•");
        str = str.replaceAll("&reg;", "®");
        str = str.replaceAll("&cent;", "¢");
        str = str.replaceAll("&mu;", "M");
        str = str.replaceAll("&ntilde;", "n");
        str = str.replaceAll("&racute;", "r");
        str = str.replaceAll("&atilde;", "a");
        str = str.replaceAll("&agrave;", "a");
        str = str.replaceAll("&aacute;", "a");
        str = str.replaceAll("&eacute;", "e");
        str = str.replaceAll("&egrave;", "e");
        str = str.replaceAll("&iacute;", "i");
        str = str.replaceAll("&oacute;", "o");
        str = str.replaceAll("&ccedil;", "c");
        str = str.replaceAll("&ocirc;", "o");
        str = str.replaceAll("&uuml;", "u");
//        str = str.replaceAll("", "");
        return str;
    }

    /**
     * parse single terms
     *
     * @param text text to be pre-processing
     * @return separated Strings
     */
    public String parsingSingleTerms(String text) {
        text = text.toLowerCase();
        text = text.replace("(", " ");
        text = text.replace(")", " ");
        text = text.replace("‘", "'");
        text = text.replace("’", "'");
        text = text.replace("``", " ");
        text = text.replace("''", " ");
        text = text.replace(". ", " ");
        text = text.replace(",", "");
        text = text.replace(":", " ");
        text = text.replace(";", " ");
        text = text.replace("[", " ");
        text = text.replace("]", " ");
        text = text.replace("+", " ");
        text = text.replace("=", " ");
        text = text.replace("?", " ");
        text = text.replace("!", " ");
        return text;
    }

    /**
     * Parse Special Single Terms : change digit formats
     * 10000.00 -> 10000
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> changeDigitFormats(String text, List<String> res) {
        String pattern = "[0-9]+\\.+[0]+$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String tempString = m.group().replaceAll("0+?$", "");
            tempString = tempString.replaceAll("[.]$", "");
            res.add(tempString);
        }
        return res;
    }

    /**
     * Parse Special Single Terms : File Extensions
     * .pdf .html .csv .rtf .txt
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> identifyFileExtensions(String text, List<String> res) {
        String pattern = "\\.pdf|\\.html|\\.csv|\\.rtf|\\.txt";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            res.add(m.group());
        }
        return res;
    }

    /**
     * Parse Special Single Terms : email address
     * xxxx@yyy.zz
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> identifyEmailAddress(String text, List<String> res) {
        String pattern = "\\w+@\\w+\\.\\w+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            res.add(m.group());
        }
        return res;
    }

    /**
     * Parse Special Single Terms : IP address
     * (0~255.0~255.0~255.0~255)
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> identifyIPAddress(String text, List<String> res) {
        String pattern = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            res.add(m.group());
        }
        return res;
    }


    /**
     * Parse Special Single Terms : URL
     * xxx://yyyyyyyy
     * could be .-/ [A-Za-z0-9_]
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> identifyURL(String text, List<String> res) {
        String pattern = "[\\w.-/]+://+[\\w.-/]+[\\w.-/]";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            res.add(m.group());
        }
        return res;
    }

    /**
     * Parse Special Single Terms : normalization
     * N.B.A -> NBA
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> normalize(String text, List<String> res) {
        String pattern = "[A-Za-z.]+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            String tempString = m.group().replace(".", "");
            res.add(tempString);
        }
        return res;
    }

    /**
     * Parse Special Single Terms : keep Monetary Values
     * keep $ ¢
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> keepMonetaryValues(String text, List<String> res) {
        String pattern = "[$|¢][0-9]+\\.?[0-9]+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            res.add(m.group());
        }
        return res;
    }

    /**
     * Parse Special Single Terms : identify Alphabet-Digit
     * F-20 -> F20
     * TAXI-303 -> TAXI303, TAXI
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> identifyAlphabetDigit(String text, List<String> res) {
        String pattern = "[A-Za-z]+[-][0-9]+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            int i = m.group().indexOf("-");
            String tempString = m.group().replace("-", "");
            res.add(tempString);
            if (i > 2) {
                res.add(tempString.substring(0, i));
            }
        }
        return res;
    }

    /**
     * Parse Special Single Terms : identify Digit Alphabet
     * 1-HOUR -> 1HOUR, HOUR
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> identifyDigitAlphabet(String text, List<String> res) {
        String pattern = "[0-9]+[-][A-Za-z]+";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            int n = m.group().length();
            int i = n - m.group().indexOf("-") - 1;
            String tempString = m.group().replace("-", "");
            res.add(tempString);
            if (i > 2) {
                res.add(tempString.substring(m.group().indexOf("-"), n - 1));
            }
        }
        return res;
    }

    /**
     * Parse Special Single Terms : identify hyphenated terms
     * drop prefix
     *
     * @param text
     * @param res
     * @return
     */
    public List<String> identifyHyphenatedTerms(String text, List<String> res) {
        String pattern = "((?:\\w+\\-)+\\w+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(text);
        while (m.find()) {
            StringBuilder sb = new StringBuilder();
            String[] tempStrings = m.group().split("-");
            for (String tempString : tempStrings) {
                // drop prefix
                if (!(tempString.equals("anti") || tempString.equals("auto") || tempString.equals("de")
                        || tempString.equals("dis") || tempString.equals("down") || tempString.equals("extra")
                        || tempString.equals("hyper") || tempString.equals("ir") || tempString.equals("inter")
                        || tempString.equals("mega") || tempString.equals("mid") || tempString.equals("mis")
                        || tempString.equals("non") || tempString.equals("over") || tempString.equals("out")
                        || tempString.equals("post") || tempString.equals("pre") || tempString.equals("pro")
                        || tempString.equals("re") || tempString.equals("semi") || tempString.equals("sub")
                        || tempString.equals("super") || tempString.equals("tele") || tempString.equals("trans")
                        || tempString.equals("ultra") || tempString.equals("un") || tempString.equals("under")
                        || tempString.equals("up"))) {
                    res.add(tempString);
                }
                sb.append(tempString);
            }
            res.add(sb.toString());
        }
        return res;
    }

    /**
     * read stop words as a HashSet
     *
     * @param fileName
     * @return
     */
    public Set<String> readStopWords(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        Set<String> set = new HashSet<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            //read one line at one time, until read null
            while ((tempString = reader.readLine()) != null) {
                set.add(tempString.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return set;
    }

    /**
     * sort one list by term
     *
     * @param list
     * @return
     */
    public List<ListNode> sortOneListByTerm(List<ListNode> list) {
        int n = list.size();
        String[] strings = new String[n];
        for (int i = 0; i < n; i++) {
            strings[i] = list.get(i).term;
        }
        Arrays.sort(strings);
        List<ListNode> res = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (strings[i].equals(list.get(j).term)) {
                    res.add(list.get(j));
                    break;
                }
            }
        }
        return res;
    }

    /**
     * m-way sort
     *
     * @param list
     * @return
     */
    public List<ListNode> mergeSortListNode(List<List<ListNode>> list) {
        int termId = 0;
        List<ListNode> resList = new ArrayList<>();
        int m = list.size();
        List<String> s = new ArrayList<>();

        for (int j = 0; j < m; j++) {
            if (list.get(j).size() == 0) {
                list.remove(j);
                j--;
                m--;
            } else {
                s.add(list.get(j).get(0).term);
            }
        }

        //m-way merge
        while (list.size() != 0) {
            Collections.sort(s);

            for (int j = 0; j < m; j++) {

                if (list.get(j).get(0).term.equals(s.get(0))) {
                    int len = resList.size();

                    if (len != 0 && resList.get(len - 1).term.equals(s.get(0))) {
                        ListNode newNode = list.get(j).get(0);
                        newNode.termId = termId;
                        resList.get(len - 1).next = newNode;
                    } else {
                        termId++;
                        list.get(j).get(0).termId = termId;
                        resList.add(list.get(j).get(0));
                    }

                    list.get(j).remove(0);
                    s.remove(0);

                    if (list.get(j).size() == 0) {
                        list.remove(j);
                        m--;
                    } else {
                        s.add(list.get(j).get(0).term);
                    }

                    break;
                }
            }
        }

        return resList;
    }

    /**
     * sort one positional list by term
     *
     * @param list
     * @return
     */
    public List<ListPositionalNode> sortOnePositionalListByTerm(List<ListPositionalNode> list) {
        int n = list.size();
        String[] strings = new String[n];
        for (int i = 0; i < n; i++) {
            strings[i] = list.get(i).term;
        }
        Arrays.sort(strings);
        List<ListPositionalNode> res = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (strings[i].equals(list.get(j).term)) {
                    res.add(list.get(j));
                    break;
                }
            }
        }
        return res;
    }

    /**
     * m-way sort positional node
     *
     * @param list
     * @return
     */
    public List<ListPositionalNode> mergeSortPositionalListNode(List<List<ListPositionalNode>> list) {
        int termId = 0;

        List<ListPositionalNode> resList = new ArrayList<>();
        int m = list.size();
        List<String> s = new ArrayList<>();

        for (int j = 0; j < m; j++) {
            if (list.get(j).size() == 0) {
                list.remove(j);
                j--;
                m--;
            } else {
                s.add(list.get(j).get(0).term);
            }
        }

        //m-way merge
        while (list.size() != 0) {
            Collections.sort(s);

            for (int j = 0; j < m; j++) {
                if (list.get(j).get(0).term.equals(s.get(0))) {
                    int len = resList.size();

                    if (len != 0 && resList.get(len - 1).term.equals(s.get(0))) {
                        ListPositionalNode newNode = list.get(j).get(0);
                        newNode.termId = termId;
                        resList.get(len - 1).next = newNode;
                    } else {
                        termId++;
                        list.get(j).get(0).termId = termId;
                        resList.add(list.get(j).get(0));
                    }

                    list.get(j).remove(0);
                    s.remove(0);

                    if (list.get(j).size() == 0) {
                        list.remove(j);
                        m--;
                    } else {
                        s.add(list.get(j).get(0).term);
                    }

                    break;
                }
            }
        }
        return resList;
    }


}