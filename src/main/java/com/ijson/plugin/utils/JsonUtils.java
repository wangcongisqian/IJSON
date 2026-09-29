package com.ijson.plugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.*;
import com.ijson.plugin.IJSONBundle;
import com.intellij.openapi.diagnostic.Logger;

import java.util.*;

/**
 * JSON 工具类 - 校验、美化、解析、生成
 */
public final class JsonUtils {

    private static final Logger LOG = Logger.getInstance(JsonUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    private JsonUtils() {
    }

    /**
     * 校验 JSON 字符串是否合法
     */
    public static ValidationResult validate(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ValidationResult(false, IJSONBundle.message("json.validation.empty"));
        }
        try {
            MAPPER.readTree(json);
            return new ValidationResult(true, IJSONBundle.message("json.validation.success"));
        } catch (JsonProcessingException e) {
            return new ValidationResult(false,
                    IJSONBundle.message("json.validation.failure", e.getOriginalMessage()));
        } catch (Exception e) {
            return new ValidationResult(false, IJSONBundle.message("json.validation.failure", e.getMessage()));
        }
    }

    /**
     * 美化（格式化）JSON
     */
    public static String beautify(String json) throws JsonProcessingException {
        JsonNode node = MAPPER.readTree(json);
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    /**
     * 压缩 JSON
     */
    public static String minify(String json) throws JsonProcessingException {
        JsonNode node = MAPPER.readTree(json);
        return MAPPER.writeValueAsString(node);
    }

    /**
     * 解析为 JsonNode
     */
    public static JsonNode parse(String json) throws JsonProcessingException {
        return MAPPER.readTree(json);
    }

    /**
     * 将 JsonNode 转为美化后的字符串
     */
    public static String toPrettyString(JsonNode node) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to serialize JsonNode", e);
            return node.toString();
        }
    }

    /**
     * 将 JsonNode 转为压缩字符串
     */
    public static String toCompactString(JsonNode node) {
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            return node.toString();
        }
    }

    /**
     * 根据简单输入生成 JSON。
     * 支持格式：
     * 1. key=value 每行一对
     * 2. key:value
     * 3. 逗号分隔的 key=value
     * 4. 纯文本描述时生成示例结构
     */
    public static String generateFromInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return generateSampleJson();
        }

        String trimmed = input.trim();

        // 已经是合法 JSON 则直接美化返回
        ValidationResult vr = validate(trimmed);
        if (vr.isValid()) {
            try {
                return beautify(trimmed);
            } catch (Exception ignored) {
            }
        }

        // 尝试解析 key=value 或 key:value 形式
        ObjectNode root = MAPPER.createObjectNode();
        String[] lines = trimmed.split("[\\r\\n,]+");
        boolean hasPairs = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] kv = null;
            if (line.contains("=")) {
                kv = line.split("=", 2);
            } else if (line.contains(":")) {
                kv = line.split(":", 2);
            }

            if (kv != null && kv.length == 2) {
                String key = kv[0].trim().replaceAll("^[\"']|[\"']$", "");
                String value = kv[1].trim().replaceAll("^[\"']|[\"']$", "");
                root.set(key, guessValueNode(value));
                hasPairs = true;
            }
        }

        if (hasPairs) {
            return toPrettyString(root);
        }

        // 否则根据描述生成简单示例
        return generateFromDescription(trimmed);
    }

    private static JsonNode guessValueNode(String value) {
        if (value == null || value.isEmpty()) {
            return NullNode.getInstance();
        }
        String lower = value.toLowerCase();
        if ("true".equals(lower) || "false".equals(lower)) {
            return BooleanNode.valueOf(Boolean.parseBoolean(lower));
        }
        if ("null".equals(lower)) {
            return NullNode.getInstance();
        }
        try {
            if (value.contains(".")) {
                return DoubleNode.valueOf(Double.parseDouble(value));
            }
            return LongNode.valueOf(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
        }
        // 数组尝试
        if (value.startsWith("[") && value.endsWith("]")) {
            try {
                return MAPPER.readTree(value);
            } catch (Exception ignored) {
            }
        }
        return TextNode.valueOf(value);
    }

    private static String generateFromDescription(String desc) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("description", desc);
        root.put("generatedAt", new Date().toString());
        root.put("status", "success");
        ObjectNode data = root.putObject("data");
        data.put("message", IJSONBundle.message("json.generator.description.message"));
        data.put("inputLength", desc.length());
        ArrayNode items = data.putArray("items");
        items.addObject().put("id", 1).put("name", IJSONBundle.message("json.generator.description.item", 1));
        items.addObject().put("id", 2).put("name", IJSONBundle.message("json.generator.description.item", 2));
        return toPrettyString(root);
    }

    public static String generateSampleJson() {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("name", "IJSON Sample");
        root.put("version", "1.0.0");
        root.put("active", true);
        root.putNull("optional");
        ArrayNode tags = root.putArray("tags");
        tags.add("json");
        tags.add("intellij");
        tags.add("plugin");
        ObjectNode config = root.putObject("config");
        config.put("timeout", 30);
        config.put("retry", 3);
        ArrayNode users = root.putArray("users");
        users.addObject().put("id", 1).put("name", "Alice").put("role", "admin");
        users.addObject().put("id", 2).put("name", "Bob").put("role", "user");
        return toPrettyString(root);
    }

    /**
     * 创建一个空的 Object 节点
     */
    public static ObjectNode createObject() {
        return MAPPER.createObjectNode();
    }

    /**
     * 创建一个空的 Array 节点
     */
    public static ArrayNode createArray() {
        return MAPPER.createArrayNode();
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * 校验结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
