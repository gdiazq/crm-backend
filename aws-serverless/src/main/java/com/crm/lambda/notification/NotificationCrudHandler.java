package com.crm.lambda.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NotificationCrudHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger log = LoggerFactory.getLogger(NotificationCrudHandler.class);
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String method = event.getHttpMethod();
        String path = event.getPath();
        Map<String, String> headers = event.getHeaders();
        if ("OPTIONS".equals(method)) {
            APIGatewayProxyResponseEvent optionsResp = new APIGatewayProxyResponseEvent();
            optionsResp.setStatusCode(200);
            optionsResp.setBody("");
            optionsResp.setHeaders(Map.of(
                    "Access-Control-Allow-Origin", "*",
                    "Access-Control-Allow-Headers", "Content-Type, X-User-Id, Authorization",
                    "Access-Control-Allow-Methods", "GET, PATCH, OPTIONS"
            ));
            return optionsResp;
        }

        String userId = headers != null ? headers.get("x-user-id") : null;

        if (userId == null || userId.isBlank()) {
            return response(401, Map.of("error", "X-User-Id header is required"));
        }

        try {
            Long uid = Long.parseLong(userId);

            if ("GET".equals(method) && path.endsWith("/paged")) {
                return handleList(uid, event.getQueryStringParameters());
            } else if ("GET".equals(method) && path.endsWith("/count")) {
                return handleCount(uid);
            } else if ("PATCH".equals(method) && path.endsWith("/read")) {
                return handleMarkAsRead(uid, event.getBody());
            } else if ("PATCH".equals(method) && path.endsWith("/read-all")) {
                return handleMarkAllAsRead(uid);
            } else if ("PATCH".equals(method) && path.endsWith("/archive")) {
                return handleArchive(uid, event.getBody());
            }

            return response(404, Map.of("error", "Not found"));
        } catch (Exception e) {
            log.error("Error processing request", e);
            return response(500, Map.of("error", "Internal server error"));
        }
    }

    private APIGatewayProxyResponseEvent handleList(Long userId, Map<String, String> params) throws Exception {
        int page = params != null && params.get("page") != null ? Integer.parseInt(params.get("page")) : 0;
        int size = params != null && params.get("size") != null ? Integer.parseInt(params.get("size")) : 20;
        String type = params != null ? params.get("type") : null;
        int offset = page * size;

        String whereClause;
        if ("unread".equalsIgnoreCase(type)) {
            whereClause = "user_id = ? AND is_read = false AND archived = false";
        } else if ("archived".equalsIgnoreCase(type)) {
            whereClause = "user_id = ? AND archived = true";
        } else {
            whereClause = "user_id = ? AND archived = false";
        }

        String countSql = "SELECT COUNT(*) FROM notifications WHERE " + whereClause;
        String dataSql = "SELECT * FROM notifications WHERE " + whereClause +
                         " ORDER BY created_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DbConfig.getConnection()) {
            long totalElements;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setLong(1, userId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                totalElements = rs.getLong(1);
            }

            List<NotificationResponse> content = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                ps.setLong(1, userId);
                ps.setInt(2, size);
                ps.setInt(3, offset);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    content.add(mapRow(rs));
                }
            }

            int totalPages = (int) Math.ceil((double) totalElements / size);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("content", content);
            result.put("totalElements", totalElements);
            result.put("totalPages", totalPages);
            result.put("number", page);
            result.put("size", size);
            result.put("last", page >= totalPages - 1);

            return response(200, result);
        }
    }

    private APIGatewayProxyResponseEvent handleCount(Long userId) throws Exception {
        try (Connection conn = DbConfig.getConnection()) {
            Map<String, Long> counts = new LinkedHashMap<>();

            counts.put("totalInbox", count(conn, "user_id = ? AND archived = false", userId));
            counts.put("totalUnread", count(conn, "user_id = ? AND is_read = false AND archived = false", userId));
            counts.put("totalArchived", count(conn, "user_id = ? AND archived = true", userId));

            return response(200, counts);
        }
    }

    private APIGatewayProxyResponseEvent handleMarkAsRead(Long userId, String body) throws Exception {
        Map<String, Object> parsed = mapper.readValue(body, Map.class);
        List<Integer> rawIds = (List<Integer>) parsed.get("ids");

        try (Connection conn = DbConfig.getConnection()) {
            String placeholders = String.join(",", rawIds.stream().map(i -> "?").toList());
            String sql = "UPDATE notifications SET is_read = true, read_at = NOW() " +
                         "WHERE id IN (" + placeholders + ") AND user_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < rawIds.size(); i++) {
                    ps.setLong(i + 1, rawIds.get(i).longValue());
                }
                ps.setLong(rawIds.size() + 1, userId);
                ps.executeUpdate();
            }
        }
        return response(200, Map.of("message", "Marked as read"));
    }

    private APIGatewayProxyResponseEvent handleMarkAllAsRead(Long userId) throws Exception {
        try (Connection conn = DbConfig.getConnection()) {
            String sql = "UPDATE notifications SET is_read = true, read_at = NOW() " +
                         "WHERE user_id = ? AND archived = false AND is_read = false";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, userId);
                ps.executeUpdate();
            }
        }
        return response(200, Map.of("message", "All marked as read"));
    }

    private APIGatewayProxyResponseEvent handleArchive(Long userId, String body) throws Exception {
        Map<String, Object> parsed = mapper.readValue(body, Map.class);
        List<Integer> rawIds = (List<Integer>) parsed.get("ids");

        try (Connection conn = DbConfig.getConnection()) {
            String placeholders = String.join(",", rawIds.stream().map(i -> "?").toList());
            String sql = "UPDATE notifications SET archived = true " +
                         "WHERE id IN (" + placeholders + ") AND user_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < rawIds.size(); i++) {
                    ps.setLong(i + 1, rawIds.get(i).longValue());
                }
                ps.setLong(rawIds.size() + 1, userId);
                ps.executeUpdate();
            }
        }
        return response(200, Map.of("message", "Archived"));
    }

    private APIGatewayProxyResponseEvent handleCleanup() throws Exception {
        try (Connection conn = DbConfig.getConnection()) {
            String sqlRead = "DELETE FROM notifications WHERE is_read = true AND read_at < NOW() - INTERVAL '30 days'";
            String sqlArchived = "DELETE FROM notifications WHERE archived = true AND created_at < NOW() - INTERVAL '60 days'";

            int deletedRead, deletedArchived;
            try (PreparedStatement ps = conn.prepareStatement(sqlRead)) {
                deletedRead = ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlArchived)) {
                deletedArchived = ps.executeUpdate();
            }

            log.info("Cleanup: {} read, {} archived deleted", deletedRead, deletedArchived);
            return response(200, Map.of("deletedRead", deletedRead, "deletedArchived", deletedArchived));
        }
    }

    private long count(Connection conn, String where, Long userId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM notifications WHERE " + where)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        }
    }

    private NotificationResponse mapRow(ResultSet rs) throws Exception {
        NotificationResponse n = new NotificationResponse();
        n.setId(rs.getLong("id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setRead(rs.getBoolean("is_read"));
        n.setArchived(rs.getBoolean("archived"));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) n.setReadAt(readAt.toLocalDateTime());
        return n;
    }

    private APIGatewayProxyResponseEvent response(int statusCode, Object body) {
        APIGatewayProxyResponseEvent resp = new APIGatewayProxyResponseEvent();
        resp.setStatusCode(statusCode);
        resp.setHeaders(Map.of(
                "Content-Type", "application/json",
                "Access-Control-Allow-Origin", "*",
                "Access-Control-Allow-Headers", "Content-Type, X-User-Id, Authorization",
                "Access-Control-Allow-Methods", "GET, PATCH, OPTIONS"
        ));
        try {
            resp.setBody(mapper.writeValueAsString(body));
        } catch (Exception e) {
            resp.setBody("{\"error\":\"serialization error\"}");
        }
        return resp;
    }
}
