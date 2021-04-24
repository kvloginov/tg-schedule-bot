package ru.madbunny.schedule.bot.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.madbunny.schedule.bot.dto.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    private final String GET_USER_QUERY = """
            SELECT id, tg_id, nickname
            FROM tbl_user
            WHERE id = ?
            """;

    private final String CREATE_USER_QUERY = """
            INSERT INTO tbl_user (tg_id, nickname)
            VALUES (?, ?)
            """;

    private final String UPDATE_USER_NICKNAME = """
            UPDATE tbl_user
            SET nickname = ?
            WHERE id = ?
            """;

    /**
     * @return id of created user
     */
    public int create(int telegramId, String nickname) {
        try (PreparedStatement pst = Database.prepareStatement(CREATE_USER_QUERY)) {
            pst.setInt(1, telegramId);
            pst.setString(2, nickname);

            pst.executeUpdate();

            if (pst.getGeneratedKeys().next()) {
                return pst.getGeneratedKeys().getInt(1);
            }
        } catch (SQLException ex) {
            LOGGER.error("trouble", ex);
        }
        return -1;
    }

    public void updateNickname(int id, String newNickName) {
        try (PreparedStatement pst = Database.prepareStatement(UPDATE_USER_NICKNAME)) {
            pst.setString(1, newNickName);
            pst.setInt(2, id);

            pst.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("trouble", ex);
        }
    }

    public User get(int id) {
        try (PreparedStatement pst = Database.prepareStatement(GET_USER_QUERY)) {
            pst.setInt(1, id);

            var rs = pst.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getInt("tg_id"),
                        rs.getString("nickname"));
            }
        } catch (SQLException ex) {
            LOGGER.error("trouble", ex);
        }
        return null;
    }

}
