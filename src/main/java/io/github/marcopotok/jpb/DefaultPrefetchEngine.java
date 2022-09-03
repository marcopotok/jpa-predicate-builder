package io.github.marcopotok.jpb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

class DefaultPrefetchEngine implements PrefetchEngine {
    private static final Pattern NESTED_ATTRIBUTES_MATCHER = Pattern.compile("^\\[(.*)]$");
    private static final int NESTED_LIST_GROUP = 1;
    private static final char LIST_START_CHAR = '[';
    private static final char LIST_END_CHAR = ']';
    private static final String ATTRIBUTE_CHAIN_DELIMITER = "\\.";
    private static final char ATTRIBUTES_DELIMITER = ',';

    private final Map<String, Fetch<?, ?>> fetchCache = new HashMap<>();

    @Override
    public <T> void prefetch(String attributeList, Root<T> root, CriteriaQuery<?> query) {
        if (Long.class != query.getResultType() && long.class != query.getResultType() && !attributeList.isBlank()) {
            prefetch(root, attributeList, "");
        }
    }

    private void prefetch(FetchParent<?, ?> node, String attributeList, String currentPath) {
        String[] split = splitSameLevel(attributeList);
        for (String rootAttributes : split) {
            prefetchChain(node, currentPath, rootAttributes);
        }
    }

    private void prefetchChain(FetchParent<?, ?> node, String currentPath, String rootAttributes) {
        String[] attributes = getAttributes(rootAttributes);
        FetchParent<?, ?> nodeFetch = node;
        for (String attribute : attributes) {
            Matcher matcher = NESTED_ATTRIBUTES_MATCHER.matcher(attribute);
            if (matcher.matches()) {
                prefetch(nodeFetch, matcher.group(NESTED_LIST_GROUP), currentPath);
            } else {
                currentPath += "." + attribute;
                nodeFetch = fetch(currentPath, nodeFetch, attribute);
            }
        }
    }

    private String[] splitSameLevel(String attributeList) {
        List<Integer> splitIndexes = calculateSplitIndexes(attributeList);
        String[] split = new String[splitIndexes.size() + 1];
        for (int i = 0, l = splitIndexes.size() + 1; i < l; i++) {
            boolean firstIteration = i == 0;
            boolean lastIteration = i == l - 1;
            int beginIndex = firstIteration ? 0 : splitIndexes.get(i - 1) + 1;
            int endIndex = lastIteration ? attributeList.length() : splitIndexes.get(i);
            split[i] = attributeList.substring(beginIndex, endIndex);
        }
        return split;
    }

    private List<Integer> calculateSplitIndexes(String attributeList) {
        List<Integer> splitIndexes = new ArrayList<>();
        char[] charArray = attributeList.toCharArray();
        for (int i = 0, nestingLevel = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char currentChar = charArray[i];
            if (currentChar == LIST_START_CHAR) {
                nestingLevel++;
            }
            if (currentChar == LIST_END_CHAR) {
                nestingLevel--;
            }
            if (nestingLevel == 0 && currentChar == ATTRIBUTES_DELIMITER) {
                splitIndexes.add(i);
            }
        }
        return splitIndexes;
    }

    private Fetch<?, ?> fetch(String currentPath, FetchParent<?, ?> node, String attributePath) {
        return fetchCache.computeIfAbsent(currentPath, ignored -> node.fetch(attributePath, JoinType.LEFT));
    }

    private String[] getAttributes(String attributeList) {
        int indexOfList = attributeList.indexOf(LIST_START_CHAR);
        if (indexOfList < 0) {
            return attributeList.split(ATTRIBUTE_CHAIN_DELIMITER);
        } else {
            String[] split = attributeList.substring(0, indexOfList).split(ATTRIBUTE_CHAIN_DELIMITER);
            String[] attributes = Arrays.copyOf(split, split.length + 1);
            attributes[attributes.length - 1] = attributeList.substring(indexOfList);
            return attributes;
        }
    }
}
