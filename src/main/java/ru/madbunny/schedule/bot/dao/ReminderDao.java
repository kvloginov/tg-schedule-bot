package ru.madbunny.schedule.bot.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.madbunny.schedule.bot.dto.Reminder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ReminderDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderDao.class);

    private static final String ADD_REMINDER_QUERY = """
            INSERT INTO tbl_reminder(creation_time, reminder_time, description, user_id)
            VALUES (?, ?, ?, ?)    
            """;

    private static final String GET_ACTUAL_QUERY = """
            SELECT id, creation_time, reminder_time, description, user_id, completed
            FROM tbl_reminder
            WHERE completed = false 
                AND reminder_time < ?
            """;

    private static final String GET_FOR_USER = """
            SELECT id, creation_time, reminder_time, description, user_id, completed
            FROM tbl_reminder
            WHERE completed = false 
                AND user_id = ?
            """;

    private static final String MARK_AS_COMPLETED = """
            UPDATE tbl_reminder
            SET completed = true
            WHERE id = ?
            """;

    /**
     * @return id of created reminder, -1 if error
     */
    public int addReminder(Instant reminderTime, int userId, String description) {
        try (PreparedStatement pst = Database.prepareStatement(ADD_REMINDER_QUERY)) {
            //TODO: THINK ABOUT CURRENT TIME ZONE
            pst.setTimestamp(1, Timestamp.from(Instant.now()));
            pst.setTimestamp(2, Timestamp.from(reminderTime));
            pst.setString(3, description);
            pst.setInt(4, userId);

            pst.executeUpdate();

            if (pst.getGeneratedKeys().next()) {
                return pst.getGeneratedKeys().getInt(1);
            }
        } catch (SQLException ex) {
            LOGGER.error("trouble", ex);
        }
        return -1;
    }

    //TODO: Think about timeZones. And count :)
    public List<Reminder> getActual(Instant fromTime) {
        var result = new ArrayList<Reminder>();
        try (PreparedStatement pst = Database.prepareStatement(GET_ACTUAL_QUERY)) {
            pst.setTimestamp(1, Timestamp.from(fromTime));

            var rs = pst.executeQuery();

            while (rs.next()) {
                result.add(mapReminder(rs));
            }

        } catch (SQLException ex) {
            LOGGER.error("trouble", ex);
        }
        return result;
    }

    public List<Reminder> getForUser(int userId) {
        var result = new ArrayList<Reminder>();
        try (PreparedStatement pst = Database.prepareStatement(GET_FOR_USER)) {
            pst.setInt(1, userId);

            var rs = pst.executeQuery();

            while (rs.next()) {
                result.add(mapReminder(rs));
            }

        } catch (SQLException ex) {
            LOGGER.error("trouble", ex);
        }
        return result;
    }

    public void markAsCompleted(int id) {
        try (PreparedStatement pst = Database.prepareStatement(MARK_AS_COMPLETED)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("trouble", ex);
        }
    }

    public Reminder mapReminder(ResultSet resultSet) throws SQLException {
        return new Reminder(
                resultSet.getInt("id"),
                resultSet.getTimestamp("creation_time").toInstant(),
                resultSet.getTimestamp("reminder_time").toInstant(),
                resultSet.getString("description"),
                resultSet.getBoolean("completed"),
                resultSet.getInt("user_id")
        );
    }
}
