package com.cebeller.autocomplete;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VocabularyTrieTest {

    private VocabularyTrie trie;

    @Before
    public void setup() {
        trie = new VocabularyTrie();
    }

    @Test
    public void emptyTrie_DoesNotContainsKey() {
        assertFalse(trie.contains("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valuesLessThanOne_AreNotAllowed() {
        trie.put("key", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroLengthKeys_AreNotAllowed() {
        trie.put("", 1);
    }

    @Test
    public void whenPut_TrieContainsKey() {
        trie.put("key", 1);
        assertTrue(trie.contains("key"));
    }

    @Test
    public void whenPutValue_CanGetValue() {
        trie.put("key", 1);
        assertEquals(1, trie.get("key"));
    }

    @Test
    public void whenOverwriteValue_CanGetLastValue() {
        trie.put("key", 1);
        trie.put("key", 2);
        assertEquals(2, trie.get("key"));
    }

    @Test
    public void whenPutMultipleKeys_CanGetAllValues() {
        trie.put("key", 1);
        trie.put("kex", 2);
        trie.put("kez", 3);
        assertEquals(1, trie.get("key"));
        assertEquals(2, trie.get("kex"));
        assertEquals(3, trie.get("kez"));
    }

    @Test
    public void whenPutMultipleKeys_CanGetKeysInOrder() {
        trie.put("kez", 3);
        trie.put("kex", 2);
        trie.put("key", 1);
        trie.put("not", 4);
        trie.put("and", 5);
        assertEquals(asList("and", "kex", "key", "kez", "not"), trie.keys());
    }

    @Test
    public void whenPutMultipleKeys_CanGetPrefixedKeysInOrder() {
        trie.put("kez", 3);
        trie.put("kex", 2);
        trie.put("key", 1);
        trie.put("not", 4);
        trie.put("and", 5);
        assertEquals(asList("kex", "key", "kez"), trie.keysWithPrefix("ke"));
    }

    @Test
    public void withLongPassage_GetsAllAndOnlyPrefixKeys() {
        populateTrie("The third thing that I need to tell you is that this thing does not think thoroughly.");
        List<String> matches = trie.keysWithPrefix("thi");
        assertEquals(4, matches.size());
    }

    private void populateTrie(String passage) {
        String[] tokens = passage.toLowerCase().split("(\\W|_)+");
        for (String token : tokens) {
            if (!trie.contains(token)) {
                trie.put(token, 1);
            } else {
                trie.put(token, trie.get(token) + 1);
            }
        }
    }

    private List<String> asList(String...strings) {
        return Arrays.asList(strings);
    }
}
