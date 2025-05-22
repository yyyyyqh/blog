package com.yqh.forum.config; // 或者 com.yqh.forum.jackson

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SortDeserializer extends JsonDeserializer<Sort> {

    @Override
    public Sort deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        // 检查 node 是否是有效的 Sort JSON 结构
        // Spring Data 的 Sort 序列化后通常是一个包含 "sorted", "unsorted", "empty", "orders" 的对象。
        // 而 "orders" 字段是一个包含 Sort.Order 对象的数组。

        // 如果 JSON 是一个空对象 {} 或者 null，直接返回 Sort.unsorted()
        if (node.isNull() || (node.isObject() && node.isEmpty())) {
            return Sort.unsorted();
        }

        // 尝试从 "orders" 字段反序列化排序属性
        JsonNode ordersNode = node.get("orders");
        if (ordersNode != null && ordersNode.isArray()) {
            List<Sort.Order> orders = new ArrayList<>();
            for (JsonNode orderNode : (ArrayNode) ordersNode) {
                // 这里需要手动反序列化 Sort.Order 对象
                // 或者依赖 Spring Data Jackson Module 的 Order 反序列化
                // 为了保险，我们在这里手动反序列化 Order
                if (orderNode.isObject()) {
                    String property = orderNode.has("property") ? orderNode.get("property").asText() : null;
                    Sort.Direction direction = orderNode.has("direction")
                            ? Sort.Direction.valueOf(orderNode.get("direction").asText().toUpperCase())
                            : Sort.Direction.ASC; // 默认升序

                    if (property != null && !property.isEmpty()) {
                        orders.add(new Sort.Order(direction, property));
                    }
                }
            }
            if (!orders.isEmpty()) {
                return Sort.by(orders);
            }
        }

        // 进一步处理可能的其他 Sort 序列化形式 (例如，Spring Data Jackson Module 的旧版本可能只序列化属性)
        // 尝试处理直接序列化为 Sort.Order 列表的情况 (不带 "orders" 字段)
        if (node.isArray()) {
            List<Sort.Order> orders = new ArrayList<>();
            for (JsonNode orderNode : (ArrayNode) node) {
                if (orderNode.isObject()) {
                    String property = orderNode.has("property") ? orderNode.get("property").asText() : null;
                    Sort.Direction direction = orderNode.has("direction")
                            ? Sort.Direction.valueOf(orderNode.get("direction").asText().toUpperCase())
                            : Sort.Direction.ASC;

                    if (property != null && !property.isEmpty()) {
                        orders.add(new Sort.Order(direction, property));
                    }
                }
            }
            if (!orders.isEmpty()) {
                return Sort.by(orders);
            }
        }


        // 如果以上所有尝试都失败，返回一个无序的 Sort 对象，避免报错
        // 这是一个兜底策略，确保即使 JSON 格式异常，也能避免反序列化错误
        System.err.println("Warning: Could not deserialize Sort object from JSON: " + node.toString() + ". Returning Sort.unsorted().");
        return Sort.unsorted();
    }
}