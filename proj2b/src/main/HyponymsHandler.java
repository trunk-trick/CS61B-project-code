package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;
import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HyponymsHandler extends NgordnetQueryHandler {
    private Map<String, Set<Integer>> wordToIds;
    private Map<Integer, List<Integer>> hyponymGraph;
    private Map<Integer, String> idToWord;

    public HyponymsHandler(String wordFile, String countFile,
                           String synsetFile, String hyponymFile) {
        wordToIds = new HashMap<>();
        hyponymGraph = new HashMap<>();
        idToWord = new HashMap<>();

        // Read synsets: id,word,definition (tab-separated)
        In synsetIn = new In(synsetFile);
        while (synsetIn.hasNextLine()) {
            String line = synsetIn.readLine();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\t");
            int id = Integer.parseInt(parts[0]);
            String word = parts[1];
            idToWord.put(id, word);
            if (!wordToIds.containsKey(word)) {
                wordToIds.put(word, new HashSet<>());
            }
            wordToIds.get(word).add(id);
        }

        // Read hyponyms: id,hyponymId1,hyponymId2,... (comma-separated)
        In hyponymIn = new In(hyponymFile);
        while (hyponymIn.hasNextLine()) {
            String line = hyponymIn.readLine();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            int id = Integer.parseInt(parts[0]);
            List<Integer> hyponyms = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                hyponyms.add(Integer.parseInt(parts[i]));
            }
            hyponymGraph.put(id, hyponyms);
        }
    }

    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        int k = q.k();

        // Find all hyponyms recursively for each word
        Set<String> result = null;
        for (String word : words) {
            Set<String> wordHyponyms = new TreeSet<>();
            if (wordToIds.containsKey(word)) {
                for (int id : wordToIds.get(word)) {
                    collectHyponyms(id, wordHyponyms, new HashSet<>());
                }
            }
            if (result == null) {
                result = wordHyponyms;
            } else {
                result.retainAll(wordHyponyms);
            }
        }

        if (result == null) {
            result = new TreeSet<>();
        }

        return result.toString();
    }

    private void collectHyponyms(int synsetId, Set<String> result, Set<Integer> visited) {
        if (visited.contains(synsetId)) {
            return;
        }
        visited.add(synsetId);

        // Add all hyponyms directly from the hyponym file (non-recursive)
        if (hyponymGraph.containsKey(synsetId)) {
            for (int hyponymId : hyponymGraph.get(synsetId)) {
                if (idToWord.containsKey(hyponymId)) {
                    result.add(idToWord.get(hyponymId));
                }
            }
        }
    }
}
