package com.cebeller.autocomplete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            public int compare(Candidate a, Candidate b) {
                return b.getConfidence() - a.getConfidence();
            }
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


    public static void main(String[] args) throws IOException {
        AutocompleteProvider completer = new AutocompleteProvider();
        if (args.length > 0) {
            completer.train(readFile(args[0]));
        }

        System.out.println("Enter a fragment to get typing suggestions, enter 'quit!' to exit");

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            List<String> tokens = null;
            String fragment;
            while ((line = reader.readLine()) != null && !line.equals("quit!")) {
                tokens = completer.getTokens(line);
                if (tokens.size() > 0 && tokens.get(0).length() > 0) {
                    fragment = tokens.get(0);
                    completer.printCandidates(fragment);
                }
            }
        }
    }

    List<Candidate> printCandidates(String input) {
        List<Candidate> candidates = getWords(input);
        System.out.print(String.format("Input: \"%s\" --> ", input));
        for (int i = 0; i < candidates.size(); i++) {
            Candidate c = candidates.get(i);
            System.out.print(String.format("\"%s\" (%d)", c.getWord(), c.getConfidence()));
            if (i < candidates.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println();
        return candidates;
    }

    private static String readFile(String path) throws IOException {
        {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded);
        }
    }
}
