package de.dhbw.woped.process2text;

import de.dhbw.woped.process2text.textGenerator.TextGenerator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class Test {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ICON_SUM = "\u2211\t";
    private static final String TEST_SUCCESS = ANSI_GREEN + "\u2714\t" + ANSI_RESET;
    private static final String TEST_FAILED = ANSI_RED + "\u2716\t" + ANSI_RESET;
    private static final String TEST_EXCEPTION = ANSI_RED + "\u2757\t" + ANSI_RESET;

    private static final String BASE_PATH = "/home/codekeks/Studium/Semester06/BPM/PT2/process2text/src/main/resources/Petrinets/";

    private static final SortedMap<String, List<String>> tests = new TreeMap<>();

    static {
        //      Key = Dateiname, Value = Erwartetes Ergebnis
        tests.put("LoanApplication-Resources.pnml", Arrays.asList(
                "<phrase ids=\"t17\"> The process begins, when it is registered. </phrase><phrase ids=\"t1_op_1\"> Then, the process is split into 3 parallel branches: </phrase><phrase ids=\"t5\"> The Clerk checks form. </phrase><phrase ids=\"t4\"> The Clerk checks the history. </phrase><phrase ids=\"t3\"> The Manager checks the funds. </phrase><phrase ids=\"t1_op_1\"> Once all 3 branches were executed, it is decided. </phrase><phrase ids=\"p12\"> Afterwards, one of the following branches is executed: </phrase><phrase ids=\"t12_op_1\"> The Manager decide:s the granted. </phrase><phrase ids=\"t13\"> Subsequently, the Secretary sends the approval. </phrase><phrase ids=\"t14_op_2\"> Then, it is archived. </phrase><phrase ids=\"t12_op_2\"> The Manager decide:s the rejected. </phrase><phrase ids=\"t15\"> Afterwards, the Secretary sends the rejection. </phrase><phrase ids=\"t14_op_1\"> Subsequently, it is archived. </phrase>"
        ));
        tests.put("LoanApplication.pnml", Arrays.asList(
                "<phrase ids=\"t17\"> The process begins, when it is registered. </phrase><phrase ids=\"null, t1_op_1\"> Then, the process is split into 3 parallel branches: </phrase><phrase ids=\"t4\"> The history is checked. </phrase><phrase ids=\"t3\"> The funds is checked. </phrase><phrase ids=\"t5\"> Form is checked. </phrase><phrase ids=\"t1_op_1\"> Once all 3 branches were executed, it is decided. </phrase><phrase ids=\"p12\"> Afterwards, one of the following branches is executed: </phrase><phrase ids=\"t12_op_1\"> The granted is decide:ed. </phrase><phrase ids=\"t13\"> Subsequently, the approval is sent. </phrase><phrase ids=\"t14_op_2\"> Then, it is archived. </phrase><phrase ids=\"t12_op_2\"> The rejected is decide:ed. </phrase><phrase ids=\"t15\"> Afterwards, the rejection is sent. </phrase><phrase ids=\"t14_op_1\"> Subsequently, it is archived. </phrase>"
        ));
        tests.put("AND - (PetriNet Logik).pnml", Arrays.asList(
                "<phrase ids=\"t3\"> The process begins, when a customer database is updated. </phrase><phrase ids=\"t2\"> In concurrency to the latter step, the receipt is printed. </phrase>",
                "<phrase ids=\"t2\"> The process begins, when a receipt is printed. </phrase><phrase ids=\"t3\"> In concurrency to the latter step, the customer database is updated. </phrase>"
        ));
        tests.put("AND-Join.pnml", Arrays.asList(
                "<phrase ids=\"t1\"> The process begins, when it is checked for the veggie recipe. </phrase><phrase ids=\"t5\"> Then, the ingredients is bought. </phrase><phrase ids=\"t4\"> In concurrency to the latter step, the recipe is printed. </phrase>",
                "<phrase ids=\"t1\"> The process begins, when it is checked for the veggie recipe. </phrase><phrase ids=\"t4\"> Then, the recipe is printed. </phrase><phrase ids=\"t5\"> In concurrency to the latter step, the ingredients is bought. </phrase>"
        ));
        tests.put("AND-Split.pnml", Arrays.asList(
                "<phrase ids=\"t1\"> The process begins, when the Cook checks for the recepies. </phrase><phrase ids=\"t4\"> Then, the salad is prepared. </phrase><phrase ids=\"t3\"> In concurrency to the latter step, the veggies is roasted. </phrase>",
                "<phrase ids=\"t1\"> The process begins, when the Cook checks for the recepies. </phrase><phrase ids=\"t3\"> Then, the veggies is roasted. </phrase><phrase ids=\"t4\"> In concurrency to the latter step, the salad is prepared. </phrase>"
        ));
        tests.put("AND-Split-Woped.pnml", Arrays.asList(
                "<phrase ids=\"t1\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"t3\"> Then, the veggies is roasted. </phrase><phrase ids=\"t4\"> In concurrency to the latter step, the salad is prepared. </phrase>",
                "<phrase ids=\"t1\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"t4\"> Then, the salad is prepared. </phrase><phrase ids=\"t3\"> In concurrency to the latter step, the veggies is roasted. </phrase>"
        ));
        tests.put("AND-XOR (PetriNet Logik).pnml", Arrays.asList(
        ));
        tests.put("AND-XOR-Split-Woped.pnml", Arrays.asList(
                "<phrase ids=\"null\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"p1\"> Then, one of the following branches is executed: </phrase><phrase ids=\"t7_op_2\"> It is checked for the recepies: meat recepie found. </phrase><phrase ids=\"t6\"> Afterwards, the needed ingredients is gotten. </phrase><phrase ids=\"t9\"> Subsequently, the steak is grilled. </phrase><phrase ids=\"t1_op_3\"> Then, the t1 is conducted. </phrase><phrase ids=\"t7_op_1\"> It is checked for the recepies: veggie recepie found. </phrase><phrase ids=\"t4\"> Afterwards, the salad is prepared. </phrase><phrase ids=\"t3\"> In concurrency to the latter step, the veggies is roasted. </phrase><phrase ids=\"t1_op_4,t8_op_1\"> Once both branches were finished, the t1 is conducted. </phrase>",
                "<phrase ids=\"null\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"p1\"> Then, one of the following branches is executed: </phrase><phrase ids=\"t7_op_2\"> It is checked for the recepies: meat recepie found. </phrase><phrase ids=\"t6\"> Afterwards, the needed ingredients is gotten. </phrase><phrase ids=\"t9\"> Subsequently, the steak is grilled. </phrase><phrase ids=\"t1_op_3\"> Then, the t1 is conducted. </phrase><phrase ids=\"t7_op_1\"> It is checked for the recepies: veggie recepie found. </phrase><phrase ids=\"t3\"> Afterwards, the veggies is roasted. </phrase><phrase ids=\"t4\"> In concurrency to the latter step, the salad is prepared. </phrase><phrase ids=\"t1_op_4,t8_op_1\"> Once both branches were finished, the t1 is conducted. </phrase>"
        ));
        tests.put("Mehrfachkombination.pnml", Arrays.asList(
        ));
        tests.put("Rigid.pnml", Arrays.asList(
        ));
        tests.put("Schleife-XOR-Split.pnml", Arrays.asList(
        ));
        tests.put("Sequenz.pnml", Arrays.asList(
                "<phrase ids=\"t1\"> The process begins, when a receipt is printed. </phrase><phrase ids=\"t2\"> Then, the receipt is sent. </phrase><phrase ids=\"t3\"> Afterwards, the customer database is updated. </phrase><phrase ids=\"t4\"> Subsequently, the email is sent. </phrase>"
        ));
        tests.put("sts.pnml", Arrays.asList(
                "<phrase ids=\"t1\"> The process begins, when a text is read. </phrase><phrase ids=\"t3\"> Then, the text is printed. </phrase>"
        ));
        tests.put("XOR (PetriNet Logik).pnml", Arrays.asList(
                "<phrase ids=\"t5\"> The process begins, when the Manager checks a payment. </phrase><phrase ids=\"p8\"> Then, one of the following branches is executed: </phrase><phrase ids=\"t7\"> The t7 is conducted. </phrase><phrase ids=\"t9\"> Afterwards, the customer is informed. </phrase><phrase ids=\"t6\"> The t6 is conducted. </phrase><phrase ids=\"t8\"> Subsequently, the customer is informed. </phrase>"
        ));
        tests.put("XOR-SplitnJoin.pnml", Arrays.asList(
                "<phrase ids=\"null\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"p1\"> Then, one of the following branches is executed: </phrase><phrase ids=\"t1_op_2\"> It is checked for the recepies: veggie recepie found. </phrase><phrase ids=\"t2\"> Afterwards, the needed ingredients is gotten. </phrase><phrase ids=\"t4_op_1\"> Subsequently, the meal is prepared. </phrase><phrase ids=\"t1_op_1\"> It is checked for the recepies: meat recepie found. </phrase><phrase ids=\"t3\"> Then, the needed ingredients is gotten. </phrase><phrase ids=\"t4_op_2\"> Afterwards, the meal is prepared. </phrase>",
                "<phrase ids=\"null\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"p1\"> Then, one of the following branches is executed: </phrase><phrase ids=\"t1_op_1\"> It is checked for the recepies: meat recepie found. </phrase><phrase ids=\"t3\"> Afterwards, the needed ingredients is gotten. </phrase><phrase ids=\"t4_op_2\"> Subsequently, the meal is prepared. </phrase><phrase ids=\"t1_op_2\"> It is checked for the recepies: veggie recepie found. </phrase><phrase ids=\"t2\"> Then, the needed ingredients is gotten. </phrase><phrase ids=\"t4_op_1\"> Afterwards, the meal is prepared. </phrase>"
        ));
        tests.put("AND-XOR-Split-Woped.pnml", Arrays.asList(
                "<phrase ids=\"null\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"p1\"> Then, one of the following branches is executed: </phrase><phrase ids=\"t7_op_2\"> It is checked for the recepies: meat recepie found. </phrase><phrase ids=\"t6\"> Afterwards, the needed ingredients is gotten. </phrase><phrase ids=\"t9\"> Subsequently, the steak is grilled. </phrase><phrase ids=\"t1_op_3\"> Then, the t1 is conducted. </phrase><phrase ids=\"t7_op_1\"> It is checked for the recepies: veggie recepie found. </phrase><phrase ids=\"t4\"> Afterwards, the salad is prepared. </phrase><phrase ids=\"t3\"> In concurrency to the latter step, the veggies is roasted. </phrase><phrase ids=\"t1_op_4,t8_op_1\"> Once both branches were finished, the t1 is conducted. </phrase>",
                "<phrase ids=\"null\"> The process begins, when it is checked for the recepies. </phrase><phrase ids=\"p1\"> Then, one of the following branches is executed: </phrase><phrase ids=\"t7_op_2\"> It is checked for the recepies: meat recepie found. </phrase><phrase ids=\"t6\"> Afterwards, the needed ingredients is gotten. </phrase><phrase ids=\"t9\"> Subsequently, the steak is grilled. </phrase><phrase ids=\"t1_op_3\"> Then, the t1 is conducted. </phrase><phrase ids=\"t7_op_1\"> It is checked for the recepies: veggie recepie found. </phrase><phrase ids=\"t3\"> Afterwards, the veggies is roasted. </phrase><phrase ids=\"t4\"> In concurrency to the latter step, the salad is prepared. </phrase><phrase ids=\"t1_op_4,t8_op_1\"> Once both branches were finished, the t1 is conducted. </phrase>"
        ));
    }

    public static void main(String[] args) {
        int successful = 0;
        int failed = 0;
        int error = 0;

        List<Exception> exceptions = new ArrayList<>();

        for (Map.Entry<String, List<String>> test : tests.entrySet()) {
            List<String> expected = test.getValue();
            List<String> roleTestList = new ArrayList<>();
//            System.out.println("//////////////////////////////////////////////////// C TEST");
//            System.out.println(test.getValue());
            String result;

            try {

                TextGenerator textGenerator = new TextGenerator();
                System.out.print(test.getKey() + ": ");
                String content = new String(Files.readAllBytes(Paths.get(BASE_PATH + test.getKey())));
                //test
                System.out.println(BASE_PATH + test.getKey());
                File f = new File(BASE_PATH + test.getKey());
                System.out.println(f.getAbsolutePath());
                result = textGenerator.toText(content);
                roleTestList = textGenerator.roleList;
                result = removeNewLinesAndSurroundingSpaces(result);
                result = result.substring(0, result.length() - 7).substring(6);
            } catch (Exception e) {
                exceptions.add(e);
                System.out.println(TEST_EXCEPTION + " failed with exception: " + e + " (expected: " + expected + ")");
                error++;
                continue;
            }

            if (result.isEmpty()) {
                System.out.println(TEST_FAILED + " failed with no result.");
                error++;
                continue;
            }
            if (!roleCheck(result, expected, roleTestList)) {
                System.out.println(TEST_FAILED + " failed because of no matching Roles");
                printDifference(expected, result);
                failed++;
                continue;
            }

            if (!equals(result, expected)) {
                System.out.println(TEST_FAILED + " failed.");
                printDifference(expected, result);
                failed++;
                continue;
            }
            successful++;
            System.out.println(TEST_SUCCESS + " succeeded.");
        }

        System.out.println("\n=============================================");
        System.out.println(TEST_SUCCESS + successful + " tests successful.");
        System.out.println(TEST_FAILED + failed + " tests failed.");
        System.out.println(TEST_EXCEPTION + error + " Petrinets failed with exception.");
        System.out.println(ICON_SUM + (successful + failed + error) + " tests executed.");

        System.out.println("\n=============================================");

        if (exceptions.size() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Exception e : exceptions) {
                e.printStackTrace();
            }
        }
    }

    private static boolean roleCheck(String result, List<String> control, List<String> roleTestList) {
        for (String c : control) {
            if (!roleTestList.isEmpty()) {
                int o = 0;
                for (String x : roleTestList) {
                    if (KMPSearch(x, c)) {
                        o++;
                    }
                }
                if (o != roleTestList.size()) {
                    return false;
                }
            }
        }
        return true;
    }


    private static boolean equals(String result, List<String> control) {
        for (String c : control) {
            // if (result.equals(removeNewLinesAndSurroundingSpaces(c)));
            System.out.println("////////////////////////////////////////////////// CTEEEEESSSSST");
            System.out.println(c);
            String[] splitResult = result.split("</phrase>");
            String[] splitControll = c.split("</phrase>");
            int i = 0;
                /*for (String f : splitResult) {
                    splitResult[i] = splitResult[i] + "</phrase>";
                    i++;
                }*/

            int numberOfResultPhrases = splitResult.length;
            boolean prüfung = true;
            int levent = 0;
            int truePattern = 0;
            int falsePattern = 0;
            //Einzelne Sätze vergleichen

            for (int j = 0; j < splitResult.length - 1; j++) {

//                if (KMPSearch("_op_", splitControll[j])) {
//                    break;
//                } else {


                //String PatterMatch Algorithmus
                prüfung = KMPSearch(splitResult[j], splitControll[j]);
                //Leventstein-Distanz Algorithmus
                levent = diff(splitControll[j], splitResult[j]);
                System.out.println("///////////////////////////  Prüfung " + prüfung + ": " + j);
                System.out.println("///////////////////////////  Leven " + levent + ": " + j);
                System.out.println(splitControll.length + "           " + splitResult.length);
                //
                if (prüfung) {
                    truePattern++;
                } else {
                    if (levent < 20) {
                        truePattern++;
                    } else {
                        falsePattern++;
                    }
                }
            }
            System.out.println("/////////////////////////// + Numberof ResultPhrases   " + numberOfResultPhrases);
            System.out.println("/////////////////////////// + Numberof True Pattern   " + truePattern);
            System.out.println("/////////////////////////// + Numberof False Pattern   " + falsePattern);
            System.out.println("/////////////////////////// + Percentage   " + percentageCalculator(numberOfResultPhrases, truePattern));
            if (percentageCalculator(numberOfResultPhrases, truePattern) >= 50.0) {
                return true;
            } else {
                return false;
            }
               /* if (result.equals(removeNewLinesAndSurroundingSpaces(c))) {
                    return true;
                }*/
        }
        //  }
        return false;
    }

    private static double percentageCalculator(int numberOfResultPhrases, int truePattern) {
        return (truePattern * 100) / numberOfResultPhrases;
    }

    private static String removeNewLinesAndSurroundingSpaces(String input) {
        if (input == null) {
            throw new RuntimeException("input was null");
        }

        return input.replaceAll("\\s*\n\\s*", "");
    }

    private static void printDifference(List<String> expected, String actual) {
        if (expected == null || actual == null) {
            return;
        }

        System.out.println("\tExpected one of: ");
        for (String s : expected) {
            System.out.println("\t\t\t- " + s);
        }
        System.out.println("\tBut got:  " + actual);
    }

    public static int diff(String orig, String eing) {
        int matrix[][] = new int[orig.length() + 1][eing.length() + 1];
        for (int i = 0; i < orig.length() + 1; i++) {
            matrix[i][0] = i;
        }
        for (int i = 0; i < eing.length() + 1; i++) {
            matrix[0][i] = i;
        }
        for (int a = 1; a < orig.length() + 1; a++) {
            for (int b = 1; b < eing.length() + 1; b++) {
                int right = 0;
                if (orig.charAt(a - 1) != eing.charAt(b - 1)) {
                    right = 1;
                }
                int mini = matrix[a - 1][b] + 1;
                if (matrix[a][b - 1] + 1 < mini) {
                    mini = matrix[a][b - 1] + 1;
                }
                if (matrix[a - 1][b - 1] + right < mini) {
                    mini = matrix[a - 1][b - 1] + right;
                }
                matrix[a][b] = mini;
            }
        }
        return matrix[orig.length()][eing.length()];
    }

    public static boolean KMPSearch(String pat, String txt) {
        int M = pat.length();
        int N = txt.length();
        System.out.println("Pattern///////" + pat);
        System.out.println("Text//////////" + txt);

        // create lps[] that will hold the longest
        // prefix suffix values for pattern
        int lps[] = new int[M];
        int j = 0; // index for pat[]
        int foundPatternNumb = 0;

        // Preprocess the pattern (calculate lps[]
        // array)
        computeLPSArray(pat, M, lps);
        boolean wert = false;
        int i = 0; // index for txt[]
        while (i < N) {
            if (pat.charAt(j) == txt.charAt(i)) {
                j++;
                i++;
            }
            if (j == M) {
                wert = true;
                foundPatternNumb++;

                System.out.println(wert);
                System.out.println("Found pattern "
                        + "at index " + (i - j));
                j = lps[j - 1];
            }


            // mismatch after j matches
            else if (i < N && pat.charAt(j) != txt.charAt(i)) {
                // Do not match lps[0..lps[j-1]] characters,
                // they will match anyway
                if (j != 0)
                    j = lps[j - 1];
                else
                    i = i + 1;
            }
        }
        return wert;
    }


    public static void computeLPSArray(String pat, int M, int lps[]) {
        // length of the previous longest prefix suffix
        int len = 0;
        int i = 1;
        lps[0] = 0; // lps[0] is always 0

        // the loop calculates lps[i] for i = 1 to M-1
        while (i < M) {
            if (pat.charAt(i) == pat.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else // (pat[i] != pat[len])
            {
                // This is tricky. Consider the example.
                // AAACAAAA and i = 7. The idea is similar
                // to search step.
                if (len != 0) {
                    len = lps[len - 1];

                    // Also, note that we do not increment
                    // i here
                } else // if (len == 0)
                {
                    lps[i] = len;
                    i++;
                }
            }
        }
    }
}
