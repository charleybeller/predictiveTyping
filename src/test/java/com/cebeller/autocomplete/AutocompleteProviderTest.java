package com.cebeller.autocomplete;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
    public void beforeTrain_GetWordsIsEmpty() {
        List<Candidate> words = completer.getWords("thi");
        assertTrue(words.isEmpty());
    }

    @Test
    public void withEmptyTrain_GetWordsIsEmpty() {
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
    public void tokensAreLowercased() {
        String passage = "This is a passage.";
        assertEquals(asList("this", "is", "a", "passage"), completer.getTokens(passage));
    }

    @Test
    public void withPassageTrain_FrequenciesTrieIsNotEmpty() {
        String passage = "one two two";
        completer.train(passage);
        assertEquals(2, completer.unigramFrequencies.get("two"));
    }

    @Test
    public void withPassageTrain_CanGetCandidates() {
        String passage = "one two two";
        completer.train(passage);
        assertEquals(2, completer.getCandidates(asList("one", "two")).size());
    }

    @Test
    public void withPassageTrain_CanGetSortedCandidates() {
        String passage = "one two two";
        completer.train(passage);
        List<Candidate> descending = completer.getSortedCandidates(asList("one", "two"));
        Candidate firstCandidate = descending.get(0);
        assertEquals("two", firstCandidate.getWord());
        assertEquals(2, firstCandidate.getConfidence());
    }

    @Test
    public void getWords_IgnoresCaseOnFragmentQuery() {
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
    public void withPassageTrain_FragmentsProduceExampleCandidates() {
        String passage = "The third thing that I need to tell you is that this thing does not think thoroughly.";
        completer.train(passage);

        List<Candidate> thi = completer.printCandidates("thi");
        assertEquals(4, thi.size());
        assertCandidate("thing", 2, thi.get(0));

        List<Candidate> nee = completer.printCandidates("nee");
        assertEquals(1, nee.size());
        assertCandidate("need", 1, nee.get(0));

        List<Candidate> th = completer.printCandidates("th");
        assertEquals(7, th.size());
        assertCandidate("that", 2, th.get(0));
    }

    @Test
    public void withTrainOnMultiplePassages_FrequenciesTrieLearnsMore() {
        String passage = "The third thing that I need to tell you is that this thing does not think thoroughly.";
        completer.train(passage);

        String preamble = "When, in the course of human events, it becomes necessary for one people to dissolve the" +
                " political bonds which have connected them with another, and to assume among the powers of the earth," +
                " the separate and equal station to which the laws of nature and of nature's God entitle them, a decent" +
                " respect to the opinions of mankind requires that they should declare the causes which impel them to the" +
                " separation.\n" +
                "We hold these truths to be self-evident, that all men are created equal, that they are endowed by their " +
                "Creator with certain unalienable rights, that among these are life, liberty and the pursuit of happiness. " +
                "That to secure these rights, governments are instituted among men, deriving their just powers from the " +
                "consent of the governed. That whenever any form of government becomes destructive to these ends, it is " +
                "the right of the people to alter or to abolish it, and to institute new government, laying its foundation" +
                " on such principles and organizing its powers in such form, as to them shall seem most likely to effect" +
                " their safety and happiness. Prudence, indeed, will dictate that governments long established should not" +
                " be changed for light and transient causes; and accordingly all experience hath shown that mankind are " +
                "more disposed to suffer, while evils are sufferable, than to right themselves by abolishing the forms to " +
                "which they are accustomed. But when a long train of abuses and usurpations, pursuing invariably the same " +
                "object evinces a design to reduce them under absolute despotism, it is their right, it is their duty, to " +
                "throw off such government, and to provide new guards for their future security. --\n" +
                "Such has been the patient sufferance of these colonies; and such is now the necessity which constrains " +
                "them to alter their former systems of government. The history of the present King of Great Britain is a " +
                "history of repeated injuries and usurpations, all having in direct object the establishment of an absolute " +
                "tyranny over these states. To prove this, let facts be submitted to a candid world.\n";
        completer.train(preamble);
        List<Candidate> th = completer.getWords("th");
        assertEquals(14, th.size());
        assertCandidate("the", 22, th.get(0));
    }
}
