package com.cebeller.autocomplete;

import java.util.ArrayList;
import java.util.List;

public class VocabularyTrie {

    private Node root;

    private class Node {
        char c;
        int value;
        Node left, right, mid;
    }

    public void put(String key, int value) {
        if (key.length() < 1) throw new IllegalArgumentException("VocabularyTrie keys must be at least length 1");
        if (value < 1) throw new IllegalArgumentException("VocabularyTrie values must be greater than 0");
        root = put(root, key, value, 0);
    }

    private Node put(Node node, String key, int value, int index) {
        char c = key.charAt(index);
        if (node == null) {
            node = new Node();
            node.c = c;
        }
        if (c < node.c) {
            node.left = put(node.left, key, value, index);
        } else if (c > node.c) {
            node.right = put(node.right, key, value, index);
        } else if (index < key.length() - 1) {
            node.mid = put(node.mid, key, value, index+1);
        } else {
            node.value = value;
        }
        return node;
    }

    public boolean contains(String key) {
        return get(key) > 0;
    }

    public int get(String key) {
        Node node = get(root, key, 0);
        if (node == null) return 0;
        return node.value;
    }

    private Node get(Node node, String key, int index) {
        if (node == null) return null;
        char c = key.charAt(index);
        if (c < node.c) {
            return get(node.left, key, index);
        } else if (c > node.c) {
            return get(node.right, key, index);
        } else if (index < key.length()-1) {
            return get(node.mid, key, index+1);
        } else {
            return node;
        }
    }

    public List<String> keys() {
        List<String> keys = new ArrayList<>();
        return collect(root, "", keys);
    }

    private List<String> collect(Node n, String prefix, List<String> keys) {
        if (n == null) return keys;
        keys = collect(n.left, prefix, keys);
        if (n.value > 0) keys.add(prefix + n.c);
        keys = collect(n.mid, prefix + n.c, keys);
        keys = collect(n.right, prefix, keys);
        return keys;
    }

    public List<String> keysWithPrefix(String prefix) {
        List<String> keys = new ArrayList<>();
        Node start = get(root, prefix, 0);
        if (start == null) {
            return keys;
        } else if (start.value > 0) {
            keys.add(prefix);
        }
        return collect(start.mid, prefix, keys);
    }
}
