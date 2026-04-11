package com.crm.lambda.notification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CleanupHandler implements RequestHandler<ScheduledEvent, Void> {

    private static final Logger log = LoggerFactory.getLogger(CleanupHandler.class);

    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {
        log.info("Running notification cleanup");

        try (Connection conn = DbConfig.getConnection()) {
            int deletedRead;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM notifications WHERE is_read = true AND read_at < NOW() - INTERVAL '30 days'")) {
                deletedRead = ps.executeUpdate();
            }

            int deletedArchived;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM notifications WHERE archived = true AND created_at < NOW() - INTERVAL '60 days'")) {
                deletedArchived = ps.executeUpdate();
            }

            log.info("Cleanup complete: {} read, {} archived deleted", deletedRead, deletedArchived);
        } catch (Exception e) {
            log.error("Cleanup failed", e);
            throw new RuntimeException("Cleanup failed", e);
        }

        return null;
    }
}
