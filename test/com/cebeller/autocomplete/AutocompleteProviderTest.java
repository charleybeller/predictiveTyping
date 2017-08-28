package com.cebeller.autocomplete;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AutocompleteProviderTest {

    private AutocompleteProvider completer;

    private List<String> asList(String...strings) {
        return Arrays.asList(strings);
    }

    @Before
    public void setup() {
        completer = new AutocompleteProvider();
    }

    @Test
    public void beforeTrain_getWordsIsEmpty() {
        List<Candidate> words = completer.getWords("thi");
        assertTrue(words.isEmpty());
    }

    @Test
    public void withEmptyTrain_getWordsIsEmpty() {
        completer.train("");
        List<Candidate> words = completer.getWords("thi");
        assertTrue(words.isEmpty());
    }

    @Test
    public void withEmptyPassage_TokensListIsEmpty() {
        assertEquals(asList(), completer.getTokens(""));
    }

    @Test
    public void withNonWordCharacters_TokensListIsEmpty() {
        String symbols = "  ! @ # $ % ^ & * ( ) - _ = + , < . > / ? ; : ' \" [ { ] } \\ |";
        assertEquals(asList(), completer.getTokens(symbols));
    }
    
    @Test
    public void withWordsAndSpaces_TokensContainsWords() {
        String passage = "this is a passage";
        assertEquals(asList("this", "is", "a", "passage"), completer.getTokens(passage));
    }

    @Test
    public void withWordsAndSpecialCharacters_TokensContainsWords() {
        String passage = "this! is? a, passage.";
        assertEquals(asList("this", "is", "a", "passage"), completer.getTokens(passage));
    }

    @Test
    public void tokensAreLowercase() {
        String passage = "This is a passage.";
        assertEquals(asList("this", "is", "a", "passage"), completer.getTokens(passage));
    }

    @Test
    public void withPassageTrain_frequenciesTrieIsNotEmpty() {
        String passage = "one two two";
        completer.train(passage);
        assertEquals(2, completer.unigramFrequencies.get("two"));
    }

    @Test
    public void withPassageTrain_canGetCandidates() {
        String passage = "one two two";
        completer.train(passage);
        assertEquals(2, completer.getCandidates(asList("one", "two")).size());
    }

    @Test
    public void withPassageTrain_canGetSortedCandidates() {
        String passage = "one two two";
        completer.train(passage);
        List<Candidate> descending = completer.getSortedCandidates(asList("one", "two"));
        Candidate firstCandidate = descending.get(0);
        assertEquals("two", firstCandidate.getWord());
        assertEquals(2, firstCandidate.getConfidence());
    }

    @Test
    public void getWords_IgnoresCaseOnFragment() {
        completer.train("The them They their Theses");
        List<Candidate> matches = completer.getWords("The");
        assertEquals(5, matches.size());
        assertCandidate("the", 1, matches.get(0));
    }

    private void assertCandidate(String word, int confidence, Candidate candidate) {
        assertEquals(word, candidate.getWord());
        assertEquals(confidence, candidate.getConfidence());
    }

    @Test
    public void withPassageTrain_fragmentsProduceExampleCandidates() {
        String passage = "The third thing that I need to tell you is that this thing does not think thoroughly.";
        completer.train(passage);

        List<Candidate> thi = completer.getWords("thi");
        printCandidates("thi", thi);
        assertEquals(4, thi.size());
        assertCandidate("thing", 2, thi.get(0));

        List<Candidate> nee = completer.getWords("nee");
        printCandidates("nee", nee);
        assertEquals(1, nee.size());
        assertCandidate("need", 1, nee.get(0));

        List<Candidate> th = completer.getWords("th");
        printCandidates("th", th);
        assertEquals(7, th.size());
        assertCandidate("that", 2, th.get(0));
    }

    private void printCandidates(String input, List<Candidate> candidates) {
        System.out.print(String.format("Input: \"%s\" --> ", input));
        Iterator<Candidate> iterator = candidates.iterator();
        for (int i = 0; i < candidates.size(); i++) {
            Candidate c = candidates.get(i);
            System.out.print(String.format("\"%s\" (%d)", c.getWord(), c.getConfidence()));
            if (i < candidates.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println();
    }
}
