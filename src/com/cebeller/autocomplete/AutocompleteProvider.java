package com.cebeller.autocomplete;

import java.util.*;

public class AutocompleteProvider {

    VocabularyTrie unigramFrequencies;

    public AutocompleteProvider() {
        unigramFrequencies = new VocabularyTrie();
    }

    public List<Candidate> getWords(String fragment) {
        List<String> keys = unigramFrequencies.keysWithPrefix(fragment.toLowerCase());
        return getSortedCandidates(keys);
    }

    List<Candidate> getSortedCandidates(List<String> keys) {
        List<Candidate> candidates = getCandidates(keys);
        Collections.sort(candidates, byDescendingConfidence());
        return candidates;
    }

    private Comparator<Candidate> byDescendingConfidence() {
        return new Comparator<Candidate>() {
            public int compare(Candidate a, Candidate b) { return b.getConfidence() - a.getConfidence(); }
        };
    }

    List<Candidate> getCandidates(List<String> keys) {
        List<Candidate> candidates = new ArrayList<>();
        for (String key : keys) {
            candidates.add(new Candidate(key, unigramFrequencies.get(key)));
        }
        return candidates;
    }

    public void train(String passage) {
        List<String> tokens = getTokens(passage);
        for (String token : tokens) {
            if (!unigramFrequencies.contains(token)) {
                unigramFrequencies.put(token, 1);
            } else {
                unigramFrequencies.put(token, unigramFrequencies.get(token) + 1);
            }
        }
    }

    List<String> getTokens(String passage) {
        //underscore is not a non-word character according to Java's regex engine
        String nonWordCharacters = "(\\W|_)+";
        String[] tokenized = passage.toLowerCase().split(nonWordCharacters);
        if (tokenized.length == 1 && tokenized[0].length() < 1) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(tokenized);
        }
    }
}
