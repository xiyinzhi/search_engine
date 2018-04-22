import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final String FILE_READ_PATH = "./BigSample/";

    private static final String[] FILE_NAME = {"fr940104.0", "fr940104.2", "fr940128.2", "fr940303.1", "fr940405.1",
            "fr940525.0", "fr940617.2", "fr940810.0", "fr940810.2", "fr941006.1", "fr941206.1"};

    private static final String QUERY_READ_PATH = "./";

    private static final double threshold = 0.7;

    private static final int TOP_TERM_NUM = 5;

    private static final int TOP_DOC_NUM = 5;

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

    public class QueryTerm {
        String term;

        int termFrequency = 0;
    }

    public class PositionalQuery extends QueryTerm {
        List<Integer> postions = new ArrayList<>();
    }

    public class QueryResult {
        String queryNum;
        int zero = 0;
        String docId;
        int rank;
        double similarityScore;
        String comment = "TfIdf";
    }

    private long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Start time: " + startTime);

        Main m = new Main();

        m.process();
//        m.readSingleIndex(FILE_READ_PATH + "singleTermIndex.csv");
//        m.readStemIndex(FILE_READ_PATH + "stemIndex.csv");
//        m.readPositionalIndex(FILE_READ_PATH + "positionalIndex.csv");
//        m.readPhraseIndex(FILE_READ_PATH + "phraseIndex.csv");

        Map<String, String> queryNarrMap = m.readNarrativeQueries(QUERY_READ_PATH + "queryfile.txt");
        Map<String, String> queryMap = m.readTopicQueries(QUERY_READ_PATH + "queryfile.txt");
        m.queryProcess(queryNarrMap, 4);
        m.queryProcess(queryMap, 5);

        long endTime = System.currentTimeMillis();
        System.out.println("End time: " + endTime);
        System.out.println("Total time: " + (endTime - startTime));

    }

    List<ListNode> completedStemIndex = new ArrayList<>();
    List<ListNode> completedPhraseIndex = new ArrayList<>();
    List<ListNode> completedSingleTermIndex = new ArrayList<>();
    List<ListPositionalNode> completedPositionalIndex = new ArrayList<>();

    /**
     * read single index(output of project1)
     */
    public void readSingleIndex(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        int count = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            ListNode p = new ListNode(null);
            //read one line at one time, until read null
            while ((tempString = reader.readLine()) != null) {
                count++;
                if (count == 1) {
                    continue;
                }
                String[] strings = tempString.split(",");
                ListNode node = new ListNode(strings[0]);
                node.termId = Integer.parseInt(strings[1]);
                node.docId = strings[2];
                node.termFrequency = Integer.parseInt(strings[3]);
                if (node.term.equals(p.term)) {
                    p.next = node;
                    p = p.next;
                } else {
                    completedSingleTermIndex.add(node);
                    p = node;
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
    }

    /**
     * read stem index(output of project1)
     */
    public void readStemIndex(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        int count = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            ListNode p = new ListNode(null);
            //read one line at one time, until read null
            while ((tempString = reader.readLine()) != null) {
                count++;
                if (count == 1) {
                    continue;
                }
                String[] strings = tempString.split(",");
                ListNode node = new ListNode(strings[0]);
                node.termId = Integer.parseInt(strings[1]);
                node.docId = strings[2];
                node.termFrequency = Integer.parseInt(strings[3]);
                if (node.term.equals(p.term)) {
                    p.next = node;
                    p = p.next;
                } else {
                    completedStemIndex.add(node);
                    p = node;
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
    }

    /**
     * read phrase index(output of project1)
     */
    public void readPhraseIndex(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        int count = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            ListNode p = new ListNode(null);
            //read one line at one time, until read null
            while ((tempString = reader.readLine()) != null) {
                count++;
                if (count == 1) {
                    continue;
                }
                String[] strings = tempString.split(",");
                ListNode node = new ListNode(strings[0].trim());
                node.termId = Integer.parseInt(strings[1]);
                node.docId = strings[2];
                node.termFrequency = Integer.parseInt(strings[3]);
                if (node.term.equals(p.term)) {
                    p.next = node;
                    p = p.next;
                } else {
                    completedPhraseIndex.add(node);
                    p = node;
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
    }

    /**
     * read positional index(output of project1)
     */
    public void readPositionalIndex(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        int count = 0;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            ListPositionalNode p = new ListPositionalNode(null);
            //read one line at one time, until read null
            while ((tempString = reader.readLine()) != null) {
                count++;
                if (count == 1) {
                    continue;
                }
                String[] str = tempString.split(",\\[|\\],");
                String[] strings = str[0].split(",");
                ListPositionalNode node = new ListPositionalNode(strings[0]);
                node.termId = Integer.parseInt(strings[1]);
                node.docId = strings[2];
                node.termFrequency = Integer.parseInt(strings[3]);
                String[] ps = str[1].split(",");
                int k = ps.length;
                for (int i = 0; i < k; i++) {
                    node.postions.add(Integer.parseInt(ps[i].trim()));
                }
                if (node.term.equals(p.term)) {
                    p.next = node;
                    p = p.next;
                } else {
                    completedPositionalIndex.add(node);
                    p = node;
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

                if (stopWordSet.contains(singleTerm) || singleTerm == singleTerms[singleTerms.length - 1]) {
                    if (singleTerm == singleTerms[singleTerms.length - 1] && !stopWordSet.contains(singleTerm)
                            && (isNumeric(singleTerm) || isAlphabetic(singleTerm))) {
                        phraseBuilder.append(" " + singleTerm);
                        countPhrase++;
                    }
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
                    if (!singleFlag && !stopWordSet.contains(singleTerm)) {
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

                            String[] tempStr = tempString.split("_");

                            for (String tempS : tempStr) {

                                if (tempS.equals("")) {
                                    continue;
                                }

                                boolean singleFlag = false;
                                for (ListNode aSingleTerm : tempSingleTermIndex) {
                                    if (aSingleTerm.term.equals(tempS) && !stopWordSet.contains(tempS)) {
                                        singleFlag = true;
                                        aSingleTerm.termFrequency++;
                                        break;
                                    }
                                }

                                if (!singleFlag) {
                                    if (!stopWordSet.contains(tempS)) {
                                        ListNode tempSingleTermNode = new ListNode(tempS);
                                        cnt++;
                                        tempSingleTermNode.termFrequency++;
                                        tempSingleTermNode.docId = docId;
                                        tempSingleTermIndex.add(tempSingleTermNode);
                                    }
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

            File myPath1 = new File(FILE_READ_PATH + "singleTermIndex/");
            if (!myPath1.exists()) {
                myPath1.mkdir();
            }
            File myPath2 = new File(FILE_READ_PATH + "positionalIndex/");
            if (!myPath2.exists()) {
                myPath2.mkdir();
            }
            File myPath3 = new File(FILE_READ_PATH + "stemIndex/");
            if (!myPath3.exists()) {
                myPath3.mkdir();
            }
            File myPath4 = new File(FILE_READ_PATH + "phraseIndex/");
            if (!myPath4.exists()) {
                myPath4.mkdir();
            }
            saveListNodeAsCsvFile(tempSingleTermIndex, FILE_READ_PATH + "singleTermIndex/" + docId);
            savePositionalListNodeAsCsvFile(tempSingleTermPositionalIndex, FILE_READ_PATH + "positionalIndex/" + docId);
            saveListNodeAsCsvFile(tempStemIndex, FILE_READ_PATH + "stemIndex/" + docId);
            saveListNodeAsCsvFile(tempPhraseIndex, FILE_READ_PATH + "phraseIndex/" + docId);

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

        completedSingleTermIndex = mergeSortListNode(singleTermIndex);
        saveFinalListAsCsvFile(completedSingleTermIndex, FILE_READ_PATH + "singleTermIndex");

        completedPositionalIndex = mergeSortPositionalListNode(singleTermPositionalIndex);
        saveFinalPositionalListAsCsvFile(completedPositionalIndex, FILE_READ_PATH + "positionalIndex");

        completedPhraseIndex = mergeSortListNode(phraseIndex);
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
        saveFinalListAsCsvFile(completedPhraseIndex, FILE_READ_PATH + "phraseIndex");

        completedStemIndex = mergeSortListNode(stemIndex);
        saveFinalListAsCsvFile(completedStemIndex, FILE_READ_PATH + "stemIndex");

        // calculate time
        long tempTime2 = System.currentTimeMillis();
        System.out.println("Time 2: " + tempTime2);
        System.out.println("Time taken to merge files: " + (tempTime2 - startTime));
    }

    Map<String, List<QueryTerm>> singleMap = new HashMap<>();
    Map<String, List<PositionalQuery>> positionalMap = new HashMap<>();
    Map<String, List<QueryTerm>> stemMap = new HashMap<>();
    Map<String, List<QueryTerm>> phraseMap = new HashMap<>();

    /**
     * query processing
     */
    public void queryProcess(Map<String, String> queryMap, int choice) throws IOException {

        Set<String> stopWordSet = readStopWords(FILE_READ_PATH + "stops.txt");
        Map<String, List<String>> map = new HashMap<>();

        for (String queryNum : queryMap.keySet()) {
            String text = queryMap.get(queryNum);
            //pre-processing
            text = processEscapeSequences(text);
            text = parsingSingleTerms(text);
//            System.out.println(queryNum + " : " + text);

            String[] singleTerms = text.split(" ");
            List<String> tempList = new ArrayList<>();

            for (int j = 0; j < singleTerms.length; j++) {
                if (!singleTerms[j].isEmpty()) {
                    tempList.add(singleTerms[j]);
                }
            }
            map.put(queryNum, tempList);
//            System.out.println(tempList);
        }
//        System.out.println(queryMap.size());


        //single
        for (String queryNum : map.keySet()) {
            List<String> tempList = map.get(queryNum);
            List<String> newList = new ArrayList<>();
            List<QueryTerm> newQueryList = new ArrayList<>();

            for (int i = 0; i < tempList.size(); i++) {
                String singleTerm = tempList.get(i);
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

                for (int j = 0; j < tempStrings.size(); j++) {
                    if (!tempStrings.get(j).isEmpty()) {
                        newList.add(tempStrings.get(j));
                    }
                }
            }

            for (int i = 0; i < newList.size(); i++) {
                if (stopWordSet.contains(newList.get(i))) {
                    newList.remove(i);
                    i--;
                }
            }

            for (int i = 0; i < newList.size(); i++) {
                int n = newQueryList.size();
                for (int j = 0; j < n; j++) {
                    if (newQueryList.get(j).term.equals(newList.get(i))) {
                        newQueryList.get(j).termFrequency++;
                        break;
                    }
                    if (j == n - 1) {
                        QueryTerm q = new QueryTerm();
                        q.term = newList.get(i);
                        q.termFrequency++;
                        newQueryList.add(q);
                    }
                }
                if (i == 0) {
                    QueryTerm q = new QueryTerm();
                    q.term = newList.get(i);
                    q.termFrequency++;
                    newQueryList.add(q);
                }
            }
            singleMap.put(queryNum, newQueryList);
        }

        //positional
        for (String queryNum : map.keySet()) {
            List<String> tempList = map.get(queryNum);
            List<String> tempList2 = new ArrayList<>();
            List<PositionalQuery> newList = new ArrayList<>();

            for (int i = 0; i < tempList.size(); i++) {
                String singleTerm = tempList.get(i);
                String[] tempStrings = singleTerm.split("-|/");

                for (int j = 0; j < tempStrings.length; j++) {
                    if (!tempStrings[j].isEmpty()) {
                        tempList2.add(tempStrings[j]);
                    }
                }
            }

            for (int i = 0; i < tempList2.size(); i++) {
                int n = newList.size();
                for (int j = 0; j < n; j++) {
                    if (newList.get(j).term.equals(tempList2.get(i))) {
                        newList.get(j).postions.add(i);
                        newList.get(j).termFrequency++;
                        break;
                    } else if (j == n - 1) {
                        PositionalQuery p = new PositionalQuery();
                        p.term = tempList2.get(i);
                        p.postions.add(i);
                        p.termFrequency++;
                        newList.add(p);
                    }
                }
                if (i == 0) {
                    PositionalQuery p = new PositionalQuery();
                    p.term = tempList2.get(i);
                    p.postions.add(i);
                    p.termFrequency++;
                    newList.add(p);
                }
            }
            positionalMap.put(queryNum, newList);
        }

        //stem
        for (String queryNum : map.keySet()) {
            List<String> tempList = map.get(queryNum);
            List<String> tempList2 = new ArrayList<>();
            List<String> newList = new ArrayList<>();
            List<QueryTerm> newQueryList = new ArrayList<>();

            for (int i = 0; i < tempList.size(); i++) {
                String singleTerm = tempList.get(i);
                String[] tempStrings = singleTerm.split("-|/");

                for (int j = 0; j < tempStrings.length; j++) {
                    if (!tempStrings[j].isEmpty()) {
                        tempList2.add(tempStrings[j]);
                    }
                }
            }

            for (int i = 0; i < tempList2.size(); i++) {
                Stemmer stemmer = new Stemmer();
                stemmer.add(tempList2.get(i).toCharArray(), tempList2.get(i).length());
                stemmer.stem();
                newList.add(stemmer.toString());
            }

            for (int i = 0; i < newList.size(); i++) {
                int n = newQueryList.size();
                for (int j = 0; j < n; j++) {
                    if (newQueryList.get(j).term.equals(newList.get(i))) {
                        newQueryList.get(j).termFrequency++;
                        break;
                    }
                    if (j == n - 1) {
                        QueryTerm q = new QueryTerm();
                        q.term = newList.get(i);
                        q.termFrequency++;
                        newQueryList.add(q);
                    }
                }
                if (i == 0) {
                    QueryTerm q = new QueryTerm();
                    q.term = newList.get(i);
                    q.termFrequency++;
                    newQueryList.add(q);
                }
            }

            stemMap.put(queryNum, newQueryList);
//            System.out.println(newList);
        }

        //phrase
        for (String queryNum : map.keySet()) {
            List<String> tempList = map.get(queryNum);
            List<String> tempList2 = new ArrayList<>();
            List<String> newList = new ArrayList<>();
            List<QueryTerm> newQueryList = new ArrayList<>();

            for (int i = 0; i < tempList.size(); i++) {
                String singleTerm = tempList.get(i);
                singleTerm = singleTerm.replace("/", " or ");
                String[] tempStrings = singleTerm.split("-| ");

                for (int j = 0; j < tempStrings.length; j++) {
                    if (!tempStrings[j].isEmpty()) {
                        tempList2.add(tempStrings[j]);
                    }
                }
            }

            int countPhrase = 0;
            StringBuilder phraseBuilder = new StringBuilder();
            int n = tempList2.size();

            for (int i = 0; i < n; i++) {
                String singleTerm = tempList2.get(i);

                if (stopWordSet.contains(singleTerm) || singleTerm == tempList2.get(n - 1)) {
                    if (singleTerm == tempList2.get(n - 1) && !stopWordSet.contains(singleTerm)) {
                        phraseBuilder.append(" " + singleTerm);
                        countPhrase++;
                    }
                    if (countPhrase == 2 || countPhrase == 3) {
                        newList.add(phraseBuilder.toString().trim());
                    }
                    phraseBuilder = new StringBuilder();
                    countPhrase = 0;
                } else {
                    phraseBuilder.append(" " + singleTerm);
                    countPhrase++;
                }
            }

            for (int i = 0; i < newList.size(); i++) {
                int len = newQueryList.size();
                for (int j = 0; j < len; j++) {
                    if (newQueryList.get(j).term.equals(newList.get(i))) {
                        newQueryList.get(j).termFrequency++;
                        break;
                    }
                    if (j == len - 1) {
                        QueryTerm q = new QueryTerm();
                        q.term = newList.get(i);
                        q.termFrequency++;
                        newQueryList.add(q);
                    }
                }
                if (i == 0) {
                    QueryTerm q = new QueryTerm();
                    q.term = newList.get(i);
                    q.termFrequency++;
                    newQueryList.add(q);
                }
            }

            phraseMap.put(queryNum, newQueryList);
//            System.out.println(newList);
        }

        List<String> docIds = new ArrayList<>();
        for (int i = 0; i < completedSingleTermIndex.size(); i++) {
            ListNode node = completedSingleTermIndex.get(i);
            ListNode p = node;
            boolean flag = false;
            while (p != null) {
                for (int j = 0; j < docIds.size(); j++) {
                    if (docIds.get(j).equals(p.docId)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    docIds.add(p.docId);
                }
                p = p.next;
                flag = false;
            }
        }
        int docNum = docIds.size();

        // calculate time
        long tempTime3 = System.currentTimeMillis();
        System.out.println("Time 3: " + tempTime3);
        System.out.println("Time taken to build query maps: " + (tempTime3 - startTime));

        switch (choice) {
            case 0:
                commonOutput(map, docNum, tempTime3, "before_reduced");
                //only output: long query without reduction
            case 1:
                queryReductionProcess(map, docNum, tempTime3, "after_reduced");
                //only output: long query after reduction
            case 2:
                commonOutput(map, docNum, tempTime3, "before_expanded");
                //only output: topic query without expansion
            case 3:
                queryExpansionProcess(map, docNum, "after_expanded");
                //only output: topic query after expansion
            case 4:
                queryReductionProcess(map, docNum, tempTime3, "after_reduced");
                commonOutput(map, docNum, tempTime3, "before_reduced");
                //output reduction results at one time
            case 5:
                queryExpansionProcess(map, docNum, "after_expanded");
                commonOutput(map, docNum, tempTime3, "before_expanded");
                //output expansion results at one time
            default:
                System.out.println("Should choose at least one query process!");
        }
    }

    /**
     * query Reduction
     *
     * @param map
     * @param docNum
     * @param tempTime3
     * @param name
     * @throws IOException
     */
    public void queryReductionProcess(Map<String, List<String>> map, int docNum, long tempTime3, String name) throws IOException {
        //calculate cosine VSM - singleIndex
        List<QueryResult> singleVSMAll = new ArrayList<>();

        Map<String, List<QueryTerm>> reducedSingleMap = reducedMap(singleMap, docNum, completedSingleTermIndex);
        Map<String, List<QueryTerm>> reducedStemMap = reducedMap(stemMap, docNum, completedStemIndex);

        for (String queryNum : map.keySet()) {
            List<QueryResult> singleVSM = calculateVSM(queryNum, reducedSingleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_VSM_" + name + "_" + threshold + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleVSM) {
                singleVSMAll.add(query);
            }
            saveQueryResult(singleVSM, QUERY_READ_PATH + "single_VSM_" + name + "_" + threshold + "/" + queryNum);
        }
        saveQueryResult(singleVSMAll, QUERY_READ_PATH + "single_VSM_" + name + "_" + threshold + "_All");

        // calculate time
        long tempTime4 = System.currentTimeMillis();
        System.out.println("Time 4: " + tempTime4);
        System.out.println("Time taken to calculate cosine VSM - single: " + (tempTime4 - tempTime3));

//        List<QueryResult> singleVSM = calculateVSM("275", singleMap.get("275"), completedSingleTermIndex, docNum);
//        saveQueryResult(singleVSM, QUERY_READ_PATH + "275single");

        //calculate cosine VSM - stemIndex
        List<QueryResult> stemVSMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemVSM = calculateVSM(queryNum, reducedStemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_VSM_" + name + "_" + threshold + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemVSM) {
                stemVSMAll.add(query);
            }
            saveQueryResult(stemVSM, QUERY_READ_PATH + "stem_VSM_" + name + "_" + threshold + "/" + queryNum);
        }
        saveQueryResult(stemVSMAll, QUERY_READ_PATH + "stem_VSM_" + name + "_" + threshold + "_All");
        // calculate time
        long tempTime5 = System.currentTimeMillis();
        System.out.println("Time 5: " + tempTime5);
        System.out.println("Time taken to calculate cosine VSM - stem: " + (tempTime5 - tempTime4));

//        List<QueryResult> stemVSM = calculateVSM("275", stemMap.get("275"), completedStemIndex, docNum);
//        saveQueryResult(stemVSM, QUERY_READ_PATH + "275stem");

        //calculate BM25 - singleIndex
        List<QueryResult> singleBM25All = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleBM25 = calculateBM25(queryNum, reducedSingleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_BM25_" + name + "_" + threshold + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleBM25) {
                singleBM25All.add(query);
            }
            saveQueryResult(singleBM25, QUERY_READ_PATH + "single_BM25_" + name + "_" + threshold + "/" + queryNum);
        }
        saveQueryResult(singleBM25All, QUERY_READ_PATH + "single_BM25_" + name + "_" + threshold + "_All");
        // calculate time
        long tempTime6 = System.currentTimeMillis();
        System.out.println("Time 6: " + tempTime6);
        System.out.println("Time taken to calculate BM25 - single: " + (tempTime6 - tempTime5));

        //calculate BM25 - stemIndex
        List<QueryResult> stemBM25All = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemBM25 = calculateBM25(queryNum, reducedStemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_BM25_" + name + "_" + threshold + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemBM25) {
                stemBM25All.add(query);
            }
            saveQueryResult(stemBM25, QUERY_READ_PATH + "stem_BM25_" + name + "_" + threshold + "/" + queryNum);
        }
        saveQueryResult(stemBM25All, QUERY_READ_PATH + "stem_BM25_" + name + "_" + threshold + "_All");
        // calculate time
        long tempTime7 = System.currentTimeMillis();
        System.out.println("Time 7: " + tempTime7);
        System.out.println("Time taken to calculate BM25 - stem: " + (tempTime7 - tempTime6));

        //calculate LM - singleIndex
        List<QueryResult> singleLMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleLM = calculateLM(queryNum, reducedSingleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_LM_" + name + "_" + threshold + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleLM) {
                singleLMAll.add(query);
            }
            saveQueryResult(singleLM, QUERY_READ_PATH + "single_LM_" + name + "_" + threshold + "/" + queryNum);
        }
        saveQueryResult(singleLMAll, QUERY_READ_PATH + "single_LM_" + name + "_" + threshold + "_All");
        // calculate time
        long tempTime8 = System.currentTimeMillis();
        System.out.println("Time 8: " + tempTime8);
//        System.out.println("Time taken to calculate LM - single: " + (tempTime8 - tempTime3));
        System.out.println("Time taken to calculate LM - single: " + (tempTime8 - tempTime7));

        //calculate LM - stemIndex
        List<QueryResult> stemLMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemLM = calculateLM(queryNum, reducedStemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_LM_" + name + "_" + threshold + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemLM) {
                stemLMAll.add(query);
            }
            saveQueryResult(stemLM, QUERY_READ_PATH + "stem_LM_" + name + "_" + threshold + "/" + queryNum);
        }
        saveQueryResult(stemLMAll, QUERY_READ_PATH + "stem_LM_" + name + "_" + threshold + "_All");
        // calculate time
        long tempTime9 = System.currentTimeMillis();
        System.out.println("Time 9: " + tempTime9);
        System.out.println("Time taken to calculate LM - stem: " + (tempTime9 - tempTime8));
    }


    /**
     * do reduction
     *
     * @param map
     * @param docNum
     * @param index
     * @return
     */
    public Map<String, List<QueryTerm>> reducedMap(Map<String, List<QueryTerm>> map, int docNum, List<ListNode> index) {
        Map<String, List<QueryTerm>> newMap = new HashMap<>();
        for (String queryNum : map.keySet()) {

            List<QueryTerm> query = map.get(queryNum);

            //calculate query weight
            int len = query.size();
//            Map<Double, String> tempMap = new HashMap<>();
            Map<Double, QueryTerm> tempMap = new TreeMap<Double, QueryTerm>(
                    new Comparator<Double>() {
                        public int compare(Double obj1, Double obj2) {
                            // 降序排序
                            return obj2.compareTo(obj1);
                        }
                    });

            for (QueryTerm queryTerm : query) {
                int queryTF = queryTerm.termFrequency;
                String term = queryTerm.term;
                ListNode node = null;
                for (int j = 0; j < index.size(); j++) {
                    if (index.get(j).term.equals(term)) {
                        node = index.get(j);
                        break;
                    }
                }
                int df = 0;
                ListNode p = node;
                while (p != null) {
                    df++;
                    p = p.next;
                }

                if (df != 0) {
                    double x = (double) (docNum) / (double) (df);
                    double idf = Math.log10(x);
                    double tempR = queryTF * idf;
                    while (tempMap.containsKey(tempR)) {
                        tempR -= Math.pow(10, -5);
                    }
                    tempMap.put(tempR, queryTerm);
                }
            }

            List<QueryTerm> reducedQueryList = new ArrayList<>();
            int topNum = (int) (len * threshold);

            Set<Double> keySet = tempMap.keySet();
            Iterator<Double> iter = keySet.iterator();
            for (int j = 0; j < topNum; j++) {
                Double key = iter.next();
                reducedQueryList.add(tempMap.get(key));
            }
            newMap.put(queryNum, reducedQueryList);
        }
        return newMap;
    }

    /**
     * @param map
     * @param docNum
     * @param tempTime3
     * @param name
     * @throws IOException
     */
    public void commonOutput(Map<String, List<String>> map, int docNum, long tempTime3, String name) throws IOException {
        //calculate cosine VSM - singleIndex
        List<QueryResult> singleVSMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleVSM = calculateVSM(queryNum, singleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_VSM_" + name + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleVSM) {
                singleVSMAll.add(query);
            }
            saveQueryResult(singleVSM, QUERY_READ_PATH + "single_VSM_" + name + "/" + queryNum);
        }
        saveQueryResult(singleVSMAll, QUERY_READ_PATH + "single_VSM_" + name + "_All");

        // calculate time
        long tempTime4 = System.currentTimeMillis();
        System.out.println("Time 4: " + tempTime4);
        System.out.println("Time taken to calculate cosine VSM - single: " + (tempTime4 - tempTime3));

//        List<QueryResult> singleVSM = calculateVSM("275", singleMap.get("275"), completedSingleTermIndex, docNum);
//        saveQueryResult(singleVSM, QUERY_READ_PATH + "275single");

        //calculate cosine VSM - stemIndex
        List<QueryResult> stemVSMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemVSM = calculateVSM(queryNum, stemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_VSM_" + name + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemVSM) {
                stemVSMAll.add(query);
            }
            saveQueryResult(stemVSM, QUERY_READ_PATH + "stem_VSM_" + name + "/" + queryNum);
        }
        saveQueryResult(stemVSMAll, QUERY_READ_PATH + "stem_VSM_" + name + "_All");
        // calculate time
        long tempTime5 = System.currentTimeMillis();
        System.out.println("Time 5: " + tempTime5);
        System.out.println("Time taken to calculate cosine VSM - stem: " + (tempTime5 - tempTime4));

//        List<QueryResult> stemVSM = calculateVSM("275", stemMap.get("275"), completedStemIndex, docNum);
//        saveQueryResult(stemVSM, QUERY_READ_PATH + "275stem");

        //calculate BM25 - singleIndex
        List<QueryResult> singleBM25All = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleBM25 = calculateBM25(queryNum, singleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_BM25_" + name + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleBM25) {
                singleBM25All.add(query);
            }
            saveQueryResult(singleBM25, QUERY_READ_PATH + "single_BM25_" + name + "/" + queryNum);
        }
        saveQueryResult(singleBM25All, QUERY_READ_PATH + "single_BM25_" + name + "_All");
        // calculate time
        long tempTime6 = System.currentTimeMillis();
        System.out.println("Time 6: " + tempTime6);
        System.out.println("Time taken to calculate BM25 - single: " + (tempTime6 - tempTime5));

        //calculate BM25 - stemIndex
        List<QueryResult> stemBM25All = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemBM25 = calculateBM25(queryNum, stemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_BM25_" + name + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemBM25) {
                stemBM25All.add(query);
            }
            saveQueryResult(stemBM25, QUERY_READ_PATH + "stem_BM25_" + name + "/" + queryNum);
        }
        saveQueryResult(stemBM25All, QUERY_READ_PATH + "stem_BM25_" + name + "_All");
        // calculate time
        long tempTime7 = System.currentTimeMillis();
        System.out.println("Time 7: " + tempTime7);
        System.out.println("Time taken to calculate BM25 - stem: " + (tempTime7 - tempTime6));

        //calculate LM - singleIndex
        List<QueryResult> singleLMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleLM = calculateLM(queryNum, singleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_LM_" + name + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleLM) {
                singleLMAll.add(query);
            }
            saveQueryResult(singleLM, QUERY_READ_PATH + "single_LM_" + name + "/" + queryNum);
        }
        saveQueryResult(singleLMAll, QUERY_READ_PATH + "single_LM_" + name + "_All");
        // calculate time
        long tempTime8 = System.currentTimeMillis();
        System.out.println("Time 8: " + tempTime8);
//        System.out.println("Time taken to calculate LM - single: " + (tempTime8 - tempTime3));
        System.out.println("Time taken to calculate LM - single: " + (tempTime8 - tempTime7));

        //calculate LM - stemIndex
        List<QueryResult> stemLMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemLM = calculateLM(queryNum, stemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_LM_" + name + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemLM) {
                stemLMAll.add(query);
            }
            saveQueryResult(stemLM, QUERY_READ_PATH + "stem_LM_" + name + "/" + queryNum);
        }
        saveQueryResult(stemLMAll, QUERY_READ_PATH + "stem_LM_" + name + "_All");
        // calculate time
        long tempTime9 = System.currentTimeMillis();
        System.out.println("Time 9: " + tempTime9);
        System.out.println("Time taken to calculate LM - stem: " + (tempTime9 - tempTime8));
    }

    /**
     * query Expansion
     *
     * @param map
     * @param docNum
     * @param name
     * @throws IOException
     */
    public void queryExpansionProcess(Map<String, List<String>> map, int docNum, String name) throws IOException {
        //calculate cosine VSM - singleIndex
        Map<String, List<QueryResult>> singleDocMap = new HashMap<>();
        Map<String, List<QueryResult>> stemDocMap = new HashMap<>();

        //use vector space model to get the ranked docs without expansion firstly
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleVSM = calculateVSM(queryNum, singleMap.get(queryNum), completedSingleTermIndex, docNum);
            singleDocMap.put(queryNum, singleVSM);
        }

        for (String queryNum : map.keySet()) {
            List<QueryResult> stemVSM = calculateVSM(queryNum, stemMap.get(queryNum), completedStemIndex, docNum);
            stemDocMap.put(queryNum, stemVSM);
        }

        // add new term -> expand query
        Map<String, List<QueryTerm>> expandedSingleMap = expandMap(singleMap, docNum, completedSingleTermIndex, singleDocMap);
        Map<String, List<QueryTerm>> expandedStemMap = expandMap(stemMap, docNum, completedStemIndex, stemDocMap);

        List<QueryResult> singleVSMAll = new ArrayList<>();

        long tempTime3 = System.currentTimeMillis();
        System.out.println("Time 3: " + tempTime3);
        System.out.println("Time taken to build query maps: " + (tempTime3 - startTime));

        for (String queryNum : map.keySet()) {
            List<QueryResult> singleVSM = calculateVSM(queryNum, expandedSingleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_VSM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleVSM) {
                singleVSMAll.add(query);
            }
            saveQueryResult(singleVSM, QUERY_READ_PATH + "single_VSM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/" + queryNum);
        }
        saveQueryResult(singleVSMAll, QUERY_READ_PATH + "single_VSM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "_All");

        // calculate time
        long tempTime4 = System.currentTimeMillis();
        System.out.println("Time 4: " + tempTime4);
        System.out.println("Time taken to calculate cosine VSM - single: " + (tempTime4 - tempTime3));

//        List<QueryResult> singleVSM = calculateVSM("275", singleMap.get("275"), completedSingleTermIndex, docNum);
//        saveQueryResult(singleVSM, QUERY_READ_PATH + "275single");
        List<QueryResult> stemVSMAll = new ArrayList<>();

        //calculate cosine VSM - stemIndex
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemVSM = calculateVSM(queryNum, expandedStemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_VSM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemVSM) {
                stemVSMAll.add(query);
            }
            saveQueryResult(stemVSM, QUERY_READ_PATH + "stem_VSM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/" + queryNum);
        }
        saveQueryResult(stemVSMAll, QUERY_READ_PATH + "stem_VSM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "_All");
        // calculate time
        long tempTime5 = System.currentTimeMillis();
        System.out.println("Time 5: " + tempTime5);
        System.out.println("Time taken to calculate cosine VSM - stem: " + (tempTime5 - tempTime4));

//        List<QueryResult> stemVSM = calculateVSM("275", stemMap.get("275"), completedStemIndex, docNum);
//        saveQueryResult(stemVSM, QUERY_READ_PATH + "275stem");

        //calculate BM25 - singleIndex
        List<QueryResult> singleBM25All = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleBM25 = calculateBM25(queryNum, expandedSingleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_BM25_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleBM25) {
                singleBM25All.add(query);
            }
            saveQueryResult(singleBM25, QUERY_READ_PATH + "single_BM25_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/" + queryNum);
        }
        saveQueryResult(singleBM25All, QUERY_READ_PATH + "single_BM25_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "_All");
        // calculate time
        long tempTime6 = System.currentTimeMillis();
        System.out.println("Time 6: " + tempTime6);
        System.out.println("Time taken to calculate BM25 - single: " + (tempTime6 - tempTime5));

        //calculate BM25 - stemIndex
        List<QueryResult> stemBM25All = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemBM25 = calculateBM25(queryNum, expandedStemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_BM25_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemBM25) {
                stemBM25All.add(query);
            }
            saveQueryResult(stemBM25, QUERY_READ_PATH + "stem_BM25_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/" + queryNum);
        }
        saveQueryResult(stemBM25All, QUERY_READ_PATH + "stem_BM25_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "_All");
        // calculate time
        long tempTime7 = System.currentTimeMillis();
        System.out.println("Time 7: " + tempTime7);
        System.out.println("Time taken to calculate BM25 - stem: " + (tempTime7 - tempTime6));

        //calculate LM - singleIndex
        List<QueryResult> singleLMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> singleLM = calculateLM(queryNum, expandedSingleMap.get(queryNum), completedSingleTermIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "single_LM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : singleLM) {
                singleLMAll.add(query);
            }
            saveQueryResult(singleLM, QUERY_READ_PATH + "single_LM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/" + queryNum);
        }
        saveQueryResult(singleLMAll, QUERY_READ_PATH + "single_LM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "_All");
        // calculate time
        long tempTime8 = System.currentTimeMillis();
        System.out.println("Time 8: " + tempTime8);
//        System.out.println("Time taken to calculate LM - single: " + (tempTime8 - tempTime3));
        System.out.println("Time taken to calculate LM - single: " + (tempTime8 - tempTime7));

        //calculate LM - stemIndex
        List<QueryResult> stemLMAll = new ArrayList<>();
        for (String queryNum : map.keySet()) {
            List<QueryResult> stemLM = calculateLM(queryNum, expandedStemMap.get(queryNum), completedStemIndex, docNum);

            File myPath = new File(QUERY_READ_PATH + "stem_LM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/");
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            for (QueryResult query : stemLM) {
                stemLMAll.add(query);
            }
            saveQueryResult(stemLM, QUERY_READ_PATH + "stem_LM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "/" + queryNum);
        }
        saveQueryResult(stemLMAll, QUERY_READ_PATH + "stem_LM_" + name + "_" + TOP_TERM_NUM + "_" + TOP_DOC_NUM + "_All");
        // calculate time
        long tempTime9 = System.currentTimeMillis();
        System.out.println("Time 9: " + tempTime9);
        System.out.println("Time taken to calculate LM - stem: " + (tempTime9 - tempTime8));
    }

    public Map<String, List<QueryTerm>> expandMap(Map<String, List<QueryTerm>> map, int docNum, List<ListNode> index,
                                                  Map<String, List<QueryResult>> baseQueryResultMap) {

        Map<String, List<QueryTerm>> newMap = new HashMap<>();
        for (String queryNum : map.keySet()) {
            List<QueryTerm> newList = map.get(queryNum);
            List<QueryResult> resList = baseQueryResultMap.get(queryNum);
            int num = Math.min(TOP_DOC_NUM, resList.size());

            String[] newDocIds = new String[num];
            for (int i = 0; i < num; i++) {
                newDocIds[i] = resList.get(i).docId;
            }

            int n = index.size();
            double[] df = new double[n];

            for (int j = 0; j < n; j++) {
                df[j] = 0.0;
                ListNode node = index.get(j);
                ListNode p = node;
                while (p != null) {
                    df[j]++;
                    p = p.next;
                }
            }

            double[] idf = new double[n];
            for (int j = 0; j < n; j++) {
                idf[j] = Math.log10(docNum / df[j]);
            }

            List<QueryTerm> query = map.get(queryNum);


            int[] nrel = new int[n];
            for (int j = 0; j < n; j++) {
                ListNode node = index.get(j);
                ListNode p = node;
                while (p != null) {
                    for (int k = 0; k < num; k++) {
                        if (p.docId.equals(newDocIds[k])) {
                            nrel[j]++;
                            break;
                        }
                    }
                    p = p.next;
                }
            }

            double[] nidf = new double[n];
            double[] temp = new double[n];
            for (int j = 0; j < n; j++) {
                nidf[j] = nrel[j] * idf[j];
                temp[j] = nidf[j];
            }

            Arrays.sort(temp);
            double[] r = new double[TOP_TERM_NUM];
            for (int j = 0; j < TOP_TERM_NUM; j++) {
                r[j] = temp[n - j - 1];
            }

            for (int j = 0; j < TOP_TERM_NUM; j++) {
                for (int k = 0; k < n; k++) {
                    if (r[j] == nidf[k]) {
                        QueryTerm qt = new QueryTerm();
                        qt.term = index.get(k).term;
                        qt.termFrequency = 1;
                        newList.add(qt);
                        break;
                    }
                }
            }

            newMap.put(queryNum, newList);
        }

        return newMap;
    }


    /**
     * calculate VSM score
     *
     * @param queryNum
     * @param query
     * @param index
     * @param docNum
     * @return
     * @throws IOException
     */
    public List<QueryResult> calculateVSM(String queryNum, List<QueryTerm> query, List<ListNode> index, int docNum) {
        //calculate query weight
        int len = query.size();
        double queryDenominator = 0;
        for (int i = 0; i < len; i++) {
            QueryTerm queryTerm = query.get(i);
            int queryTF = queryTerm.termFrequency;
            String term = queryTerm.term;
            ListNode node = null;
            for (int j = 0; j < index.size(); j++) {
                if (index.get(j).term.equals(term)) {
                    node = index.get(j);
                    break;
                }
            }
            int df = 0;
            ListNode p = node;
            while (p != null) {
                df++;
                p = p.next;
            }

            if (df != 0) {
                double idf = Math.log10(docNum / df);
                queryDenominator += Math.pow((Math.log10(queryTF) + 1) * idf, 2);
            } else {
                query.remove(queryTerm);
                len--;
                i--;
            }
        }

        double[] queryWeight = new double[len];
        int i = 0;
        for (QueryTerm queryTerm : query) {
            int queryTF = queryTerm.termFrequency;
            String term = queryTerm.term;
            ListNode node = null;
            for (int j = 0; j < index.size(); j++) {
                if (index.get(j).term.equals(term)) {
                    node = index.get(j);
                    break;
                }
            }
            int df = 0;
            ListNode p = node;
            while (p != null) {
                df++;
                p = p.next;
            }

            double idf = df == 0 ? 0 : Math.log10(docNum / df);

            queryWeight[i] = (Math.log10(queryTF) + 1) * idf / queryDenominator;
            i++;
        }

        //calculate doc nums
        int selectedDocNum = 0;
        List<String> docIds = new ArrayList<>();
        for (QueryTerm queryTerm : query) {

            String term = queryTerm.term;
            ListNode node = null;
            for (int j = 0; j < index.size(); j++) {
                if (index.get(j).term.equals(term)) {
                    node = index.get(j);
                    break;
                }
            }
            ListNode p = node;
            boolean flag = false;
            while (p != null) {
                for (int j = 0; j < docIds.size(); j++) {
                    if (docIds.get(j).equals(p.docId)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    docIds.add(p.docId);
                }
                p = p.next;
                flag = false;
            }
        }
        selectedDocNum = docIds.size();

        //calculate doc weight
        double[][] docWeight = new double[selectedDocNum][len];
        for (int s = 0; s < selectedDocNum; s++) {
            double[] factors = new double[len];
            int l = 0;
            for (QueryTerm queryTerm : query) {
                String term = queryTerm.term;
                ListNode node = null;
                for (int j = 0; j < index.size(); j++) {
                    if (index.get(j).term.equals(term)) {
                        node = index.get(j);
                        break;
                    }
                }
                int df = 0;
                int docTF = 0;
                ListNode p = node;
                while (p != null) {
                    df++;
                    if (p.docId.equals(docIds.get(s))) {
                        docTF = p.termFrequency;
                    }
                    p = p.next;
                }
                double idf = df == 0 ? 0 : Math.log10(docNum / df);
//                double idf = Math.log10(docNum / (df + 1));
                factors[l] = docTF == 0 ? 0 : (Math.log10(docTF) + 1) * idf;
                l++;
            }
            double sum = 0;
            for (int k = 0; k < len; k++) {
                sum += Math.pow(factors[k], 2);
            }
            for (int k = 0; k < len; k++) {
                docWeight[s][k] = factors[k] / sum;
            }
        }

        //calculate cosine
        double[] scores = new double[selectedDocNum];
        for (int s = 0; s < selectedDocNum; s++) {
            double sum = 0;
            for (int j = 0; j < len; j++) {
                sum += queryWeight[j] * docWeight[s][j];
            }
            double mode1 = 0;
            double mode2 = 0;
            for (int j = 0; j < len; j++) {
                mode1 += Math.pow(queryWeight[j], 2);
            }
            mode1 = Math.sqrt(mode1);
            for (int j = 0; j < len; j++) {
                mode2 += Math.pow(docWeight[s][j], 2);
            }
            mode2 = Math.sqrt(mode2);
            scores[s] = sum / (mode1 * mode2);
        }

        List<QueryResult> resList = new ArrayList<>();
        for (int s = 0; s < selectedDocNum; s++) {
            QueryResult res = new QueryResult();
            res.queryNum = queryNum;
            res.docId = docIds.get(s);
            res.similarityScore = scores[s];
            resList.add(res);
        }
        resList = sortQueryResult(resList);
        return resList;
    }

    /**
     * calculate BM25 score
     *
     * @param queryNum
     * @param query
     * @param index
     * @param docNum
     * @return
     * @throws IOException
     */
    public List<QueryResult> calculateBM25(String queryNum, List<QueryTerm> query, List<ListNode> index, int docNum) {
        double k1 = 1.2, k2 = 800, b = 0.75;

        //calculate avgdl
        int sum = 0;
        for (int i = 0; i < index.size(); i++) {
            ListNode p = index.get(i);
            while (p != null) {
                sum += p.termFrequency;
                p = p.next;
            }
        }
        double avgdl = sum / docNum;

        //calculate query weight
        int len = query.size();
        double[] queryWeight = new double[len];
        int i = 0;
        for (QueryTerm queryTerm : query) {
            String term = queryTerm.term;
            ListNode node = null;
            for (int j = 0; j < index.size(); j++) {
                if (index.get(j).term.equals(term)) {
                    node = index.get(j);
                    break;
                }
            }
            int df = 0;
            ListNode p = node;
            while (p != null) {
                df++;
                p = p.next;
            }

            queryWeight[i] = Math.log10((docNum - df + 0.5) / (df + 0.5));
            i++;
        }

        //calculate doc nums
        int selectedDocNum = 0;
        List<String> docIds = new ArrayList<>();
        for (QueryTerm queryTerm : query) {
            String term = queryTerm.term;
            ListNode node = null;
            for (int j = 0; j < index.size(); j++) {
                if (index.get(j).term.equals(term)) {
                    node = index.get(j);
                    break;
                }
            }
            ListNode p = node;
            boolean flag = false;
            while (p != null) {
                for (int j = 0; j < docIds.size(); j++) {
                    if (docIds.get(j).equals(p.docId)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    docIds.add(p.docId);
                }
                p = p.next;
                flag = false;
            }
        }
        selectedDocNum = docIds.size();

        //calculate |D|, |D| is the length of the document D in words
        int[] docLength = new int[selectedDocNum];
        for (int s = 0; s < selectedDocNum; s++) {
            for (int j = 0; j < index.size(); j++) {
                ListNode p = index.get(j);
                while (p != null) {
                    if (p.docId.equals(docIds.get(s))) {
                        docLength[s] += p.termFrequency;
                        break;
                    }
                    p = p.next;
                }
            }
        }

        //calculate BM25 for each doc
        double[] scores = new double[selectedDocNum];
        for (int s = 0; s < selectedDocNum; s++) {
            double sumBM25 = 0;
            for (int j = 0; j < len; j++) {
                QueryTerm queryTerm = query.get(j);
                ListNode node = null;
                for (int k = 0; k < index.size(); k++) {
                    if (index.get(k).term.equals(queryTerm.term)) {
                        node = index.get(k);
                        break;
                    }
                }
                int docTF = 0;
                ListNode p = node;
                while (p != null) {
                    if (p.docId.equals(docIds.get(s))) {
                        docTF = p.termFrequency;
                    }
                    p = p.next;
                }
                sumBM25 += queryWeight[j] * ((k1 + 1) * docTF) / (docTF + k1 * (1 - b + b * docLength[s] / avgdl))
                        * (k2 + 1) * queryTerm.termFrequency / (k2 + queryTerm.termFrequency);
            }
            scores[s] = sumBM25;
        }

        List<QueryResult> resList = new ArrayList<>();
        for (int s = 0; s < selectedDocNum; s++) {
            QueryResult res = new QueryResult();
            res.queryNum = queryNum;
            res.docId = docIds.get(s);
            res.similarityScore = scores[s];
            resList.add(res);
        }
        resList = sortQueryResult(resList);
        return resList;
    }

    /**
     * calculate LM score
     *
     * @param queryNum
     * @param query
     * @param index
     * @param docNum
     * @return
     * @throws IOException
     */
    public List<QueryResult> calculateLM(String queryNum, List<QueryTerm> query, List<ListNode> index, int docNum) {
        //μ
        int u = 150;

        int len = query.size();

        //calculate |C|
        int collectionLength = 0;
        for (int i = 0; i < index.size(); i++) {
            ListNode p = index.get(i);
            while (p != null) {
                collectionLength += p.termFrequency;
                p = p.next;
            }
        }

        //calculate doc nums
        int selectedDocNum = 0;
        List<String> docIds = new ArrayList<>();
        for (QueryTerm queryTerm : query) {
            String term = queryTerm.term;
            ListNode node = null;
            for (int j = 0; j < index.size(); j++) {
                if (index.get(j).term.equals(term)) {
                    node = index.get(j);
                    break;
                }
            }
            ListNode p = node;
            boolean flag = false;
            while (p != null) {
                for (int j = 0; j < docIds.size(); j++) {
                    if (docIds.get(j).equals(p.docId)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    docIds.add(p.docId);
                }
                p = p.next;
                flag = false;
            }
        }
        selectedDocNum = docIds.size();


        //calculate |D|, |D| is the length of the document D in words
        int[] docLength = new int[selectedDocNum];
        for (int s = 0; s < selectedDocNum; s++) {
            for (int j = 0; j < index.size(); j++) {
                ListNode p = index.get(j);
                while (p != null) {
                    if (p.docId.equals(docIds.get(s))) {
                        docLength[s] += p.termFrequency;
                        break;
                    }
                    p = p.next;
                }
            }
        }

        // calculate tf-c
        int[] cTF = new int[len];
        for (int i = 0; i < len; i++) {
            QueryTerm queryTerm = query.get(i);
            ListNode node = null;
            for (int k = 0; k < index.size(); k++) {
                if (index.get(k).term.equals(queryTerm.term)) {
                    node = index.get(k);
                    break;
                }
            }
            ListNode p = node;
            while (p != null) {
                cTF[i] += p.termFrequency;
                p = p.next;
            }
            if (cTF[i] == 0) {
                cTF[i] = 1;
            }
        }

        //calculate score
        double[] logP = new double[selectedDocNum];
        for (int s = 0; s < selectedDocNum; s++) {
            int[] dTF = new int[len];
            for (int j = 0; j < len; j++) {
                QueryTerm queryTerm = query.get(j);
                ListNode node = null;
                for (int k = 0; k < index.size(); k++) {
                    if (index.get(k).term.equals(queryTerm.term)) {
                        node = index.get(k);
                        break;
                    }
                }
                ListNode p = node;
                while (p != null) {
                    if (p.docId.equals(docIds.get(s))) {
                        dTF[j] = p.termFrequency;
                        break;
                    }
                    p = p.next;
                }
            }

            logP[s] = 0;
            double b = docLength[s] + u;
            for (int j = 0; j < len; j++) {
                double a = u * cTF[j];
                double d = a / collectionLength;
                double c = dTF[j] + d;
                logP[s] += Math.log10(c / b);
//                logP[s] += Math.log10((dTF[j] + u * cTF[j] / collectionLength) / (docLength[s] + u));
            }
        }

        List<QueryResult> resList = new ArrayList<>();
        for (int s = 0; s < selectedDocNum; s++) {
            QueryResult res = new QueryResult();
            res.queryNum = queryNum;
            res.docId = docIds.get(s);
            res.similarityScore = logP[s];
            resList.add(res);
        }
        resList = sortQueryResult(resList);

        return resList;
    }

    /**
     * sort query result
     *
     * @return
     */
    public List<QueryResult> sortQueryResult(List<QueryResult> list) {
        List<QueryResult> resList = new ArrayList<>();
        int n = list.size();
        double[] s = new double[n];
        for (int i = 0; i < n; i++) {
            s[i] = list.get(i).similarityScore;
        }
        Arrays.sort(s);
        for (int i = 0; i < n / 2; i++) {
            double temp = s[i];
            s[i] = s[n - i - 1];
            s[n - i - 1] = temp;

        }
        int m = n;
        int k = n > 100 ? 100 : n;
        int kk = k;
        for (int i = 0; i < kk; i++) {
            for (int j = 0; j < m; j++) {
                if (list.get(j).similarityScore == s[i]) {
                    list.get(j).rank = kk - k + 1;
                    resList.add(list.get(j));
                    list.remove(j);
                    m--;
                    k--;
                    break;
                }
            }
            if (k == 0) {
                break;
            }
        }
        return resList;
    }

    /**
     * @param list
     * @param fileName
     * @throws IOException
     */
    public void saveQueryResult(List<QueryResult> list, String fileName) throws IOException {
        File file = new File(fileName + ".txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
//        out.write("Query_No,zero,Document_id,Similarity_rank,Similarity_score,Comment");
        for (int i = 0; i < list.size(); i++) {
            QueryResult p = list.get(i);
            out.write(p.queryNum + " ");
            out.write(p.zero + " ");
            out.write(p.docId + " ");
            out.write(p.rank + " ");
            out.write(p.similarityScore + " ");
            out.write(p.comment);
            if (i != list.size() - 1) {
                out.write("\r\n");
            }
        }
        out.close();
    }

    /**
     * read topic query from file
     *
     * @param fileName
     * @return num, title
     */
    public Map<String, String> readTopicQueries(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        Map<String, String> map = new HashMap();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            String tempQueryNum = null;
            String tempQueryTitle;
            //read one line at one time, until read null
            while ((tempString = reader.readLine()) != null) {
                if (tempString.startsWith("<num>")) {
                    tempQueryNum = tempString.replace("<num> Number:", "").trim();
                } else if (tempString.startsWith("<title>")) {
                    tempQueryTitle = tempString.replace("<title> Topic:", "").trim();
                    map.put(tempQueryNum, tempQueryTitle);
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
     * read narrative query from file
     *
     * @param fileName
     * @return num, Narrative
     */
    public Map<String, String> readNarrativeQueries(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        Map<String, String> map = new HashMap();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            String tempQueryNum = null;
            StringBuilder tempQueryNarr = new StringBuilder();
            boolean flag = false;
            //read one line at one time, until read null
            while ((tempString = reader.readLine()) != null) {
                if (tempString.startsWith("</top>")) {
                    flag = false;
                    map.put(tempQueryNum, tempQueryNarr.toString());
                    tempQueryNarr = new StringBuilder();
                }
                if (flag == true) {
                    tempQueryNarr.append(tempString);
                }
                if (tempString.startsWith("<num>")) {
                    tempQueryNum = tempString.replace("<num> Number:", "").trim();
                } else if (tempString.startsWith("<narr>")) {
                    flag = true;
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
        //s : the list of first element in m-way list
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
                        ListNode p = resList.get(len - 1);
                        while (p.next != null) {
                            p = p.next;
                        }
                        p.next = newNode;
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
                        ListPositionalNode p = resList.get(len - 1);
                        while (p.next != null) {
                            p = p.next;
                        }
                        p.next = newNode;
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
