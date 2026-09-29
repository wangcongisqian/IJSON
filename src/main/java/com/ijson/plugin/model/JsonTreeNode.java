package com.ijson.plugin.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * 可编辑的 JSON 树节点
 */
public class JsonTreeNode extends DefaultMutableTreeNode {

    public enum NodeType {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL, ROOT
    }

    private String key;          // 字段名（数组元素时为索引字符串）
    private NodeType type;
    private Object value;        // 叶子节点的值

    public JsonTreeNode(String key, NodeType type, Object value) {
        this.key = key;
        this.type = type;
        this.value = value;
        setUserObject(buildDisplayText());
    }

    public static JsonTreeNode fromJsonNode(JsonNode jsonNode) {
        return fromJsonNode(null, jsonNode);
    }

    public static JsonTreeNode fromJsonNode(String key, JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return new JsonTreeNode(key, NodeType.NULL, null);
        }
        if (jsonNode.isObject()) {
            JsonTreeNode node = new JsonTreeNode(key, NodeType.OBJECT, null);
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                node.add(fromJsonNode(entry.getKey(), entry.getValue()));
            }
            return node;
        }
        if (jsonNode.isArray()) {
            JsonTreeNode node = new JsonTreeNode(key, NodeType.ARRAY, null);
            for (int i = 0; i < jsonNode.size(); i++) {
                node.add(fromJsonNode(String.valueOf(i), jsonNode.get(i)));
            }
            return node;
        }
        if (jsonNode.isTextual()) {
            return new JsonTreeNode(key, NodeType.STRING, jsonNode.asText());
        }
        if (jsonNode.isNumber()) {
            return new JsonTreeNode(key, NodeType.NUMBER, jsonNode.numberValue());
        }
        if (jsonNode.isBoolean()) {
            return new JsonTreeNode(key, NodeType.BOOLEAN, jsonNode.asBoolean());
        }
        return new JsonTreeNode(key, NodeType.NULL, null);
    }

    public JsonNode toJsonNode() {
        switch (type) {
            case OBJECT:
            case ROOT:
                ObjectNode obj = JsonNodeFactory.instance.objectNode();
                Enumeration<?> children = children();
                while (children.hasMoreElements()) {
                    JsonTreeNode child = (JsonTreeNode) children.nextElement();
                    if (child.getKey() != null) {
                        obj.set(child.getKey(), child.toJsonNode());
                    }
                }
                return obj;
            case ARRAY:
                ArrayNode arr = JsonNodeFactory.instance.arrayNode();
                Enumeration<?> arrChildren = children();
                while (arrChildren.hasMoreElements()) {
                    JsonTreeNode child = (JsonTreeNode) arrChildren.nextElement();
                    arr.add(child.toJsonNode());
                }
                return arr;
            case STRING:
                return TextNode.valueOf(value != null ? value.toString() : "");
            case NUMBER:
                if (value instanceof Number) {
                    Number n = (Number) value;
                    if (n instanceof Double || n instanceof Float) {
                        return DoubleNode.valueOf(n.doubleValue());
                    }
                    return LongNode.valueOf(n.longValue());
                }
                try {
                    String s = value.toString();
                    if (s.contains(".")) {
                        return DoubleNode.valueOf(Double.parseDouble(s));
                    }
                    return LongNode.valueOf(Long.parseLong(s));
                } catch (Exception e) {
                    return TextNode.valueOf(String.valueOf(value));
                }
            case BOOLEAN:
                return BooleanNode.valueOf(value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value)));
            case NULL:
            default:
                return NullNode.getInstance();
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
        setUserObject(buildDisplayText());
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
        setUserObject(buildDisplayText());
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        setUserObject(buildDisplayText());
    }

    public boolean isLeafType() {
        return type == NodeType.STRING || type == NodeType.NUMBER
                || type == NodeType.BOOLEAN || type == NodeType.NULL;
    }

    public boolean isContainer() {
        return type == NodeType.OBJECT || type == NodeType.ARRAY || type == NodeType.ROOT;
    }

    private String buildDisplayText() {
        StringBuilder sb = new StringBuilder();
        if (key != null) {
            sb.append(key).append(": ");
        }
        switch (type) {
            case OBJECT:
            case ROOT:
                sb.append("{...}");
                break;
            case ARRAY:
                sb.append("[...]");
                break;
            case STRING:
                sb.append("\"").append(value).append("\"");
                break;
            case NUMBER:
            case BOOLEAN:
                sb.append(value);
                break;
            case NULL:
                sb.append("null");
                break;
        }
        return sb.toString();
    }

    public void refreshDisplay() {
        setUserObject(buildDisplayText());
    }

    @Override
    public String toString() {
        return buildDisplayText();
    }
}
