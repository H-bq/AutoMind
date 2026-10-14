package com.hs.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hs.agent.exception.LowConfidenceException;
import org.springframework.ai.converter.BeanOutputConverter;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class StructuredOutputConverter<T extends Confident> implements org.springframework.ai.converter.StructuredOutputConverter<T> {

    private static final Set<String> NULL_SENTINELS = Set.of(
            "missing", "unknown", "null", "none", "n/a", "na", "未提供", "缺失", "待补充"
    );
    private static final Pattern MYSQL_DATE_TIME = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

    private final BeanOutputConverter<T> delegate;
    private final BigDecimal threshold;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StructuredOutputConverter(Class<T> targetType, BigDecimal threshold) {
        ensureConfident(targetType);
        this.delegate = new BeanOutputConverter<>(targetType);
        this.threshold = threshold;
    }

    public static <T extends Confident> StructuredOutputConverter<T> of(Class<T> targetType, BigDecimal threshold) {
        return new StructuredOutputConverter<>(targetType, threshold);
    }

    @Override
    public T convert(String source) {
        T value = delegate.convert(sanitize(source));
        BigDecimal confidence = value == null ? null : value.getConfidence();
        if (confidence == null || confidence.compareTo(threshold) < 0) {
            throw new LowConfidenceException(confidence, threshold, source);
        }
        return value;
    }

    @Override
    public String getFormat() {
        return delegate.getFormat();
    }

    private void ensureConfident(Class<T> targetType) {
        try {
            targetType.getMethod("getConfidence");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(targetType.getName() + " 必须实现 confidence 输出契约", e);
        }
    }

    private String sanitize(String source) {
        if (source == null || source.isBlank()) {
            return source;
        }
        String json = extractJsonObject(source);
        try {
            JsonNode root = objectMapper.readTree(json);
            normalizeMissingSentinels(root);
            normalizeDateTimeStrings(root);
            return objectMapper.writeValueAsString(root);
        } catch (Exception ignored) {
            return source;
        }
    }

    private String extractJsonObject(String source) {
        String trimmed = source.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private void normalizeMissingSentinels(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            objectNode.fields().forEachRemaining(entry -> {
                JsonNode child = entry.getValue();
                if (isNullSentinel(child)) {
                    objectNode.set(entry.getKey(), NullNode.getInstance());
                } else {
                    normalizeMissingSentinels(child);
                }
            });
        } else if (node instanceof ArrayNode arrayNode) {
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode child = arrayNode.get(i);
                if (isNullSentinel(child)) {
                    arrayNode.set(i, NullNode.getInstance());
                } else {
                    normalizeMissingSentinels(child);
                }
            }
        }
    }

    private boolean isNullSentinel(JsonNode node) {
        return node != null
                && node.isTextual()
                && NULL_SENTINELS.contains(node.asText().trim().toLowerCase(Locale.ROOT));
    }

    private void normalizeDateTimeStrings(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            objectNode.fields().forEachRemaining(entry -> {
                JsonNode child = entry.getValue();
                if (isMysqlDateTime(child)) {
                    objectNode.put(entry.getKey(), child.asText().replace(' ', 'T'));
                } else {
                    normalizeDateTimeStrings(child);
                }
            });
        } else if (node instanceof ArrayNode arrayNode) {
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode child = arrayNode.get(i);
                if (isMysqlDateTime(child)) {
                    arrayNode.set(i, objectMapper.getNodeFactory().textNode(child.asText().replace(' ', 'T')));
                } else {
                    normalizeDateTimeStrings(child);
                }
            }
        }
    }

    private boolean isMysqlDateTime(JsonNode node) {
        return node != null
                && node.isTextual()
                && MYSQL_DATE_TIME.matcher(node.asText().trim()).matches();
    }
}
