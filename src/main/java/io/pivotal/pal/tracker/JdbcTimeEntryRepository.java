package io.pivotal.pal.tracker;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.*;
import java.util.List;

@Component
public class JdbcTimeEntryRepository implements TimeEntryRepository {

    private final JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    public JdbcTimeEntryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public TimeEntry create(TimeEntry entry) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps =
                            connection.prepareStatement("INSERT INTO time_entries(project_id, user_id, date, hours) VALUES(?, ?, ?, ?)", new String[]{"id"});
                    ps.setLong(1, entry.getProjectId());
                    ps.setLong(2, entry.getUserId());
                    ps.setDate(3, Date.valueOf(entry.getDate()));
                    ps.setInt(4, entry.getHours());
                    return ps;
                },
                keyHolder);

        entry.setTimeEntryId(keyHolder.getKey().longValue());

        return entry;
    }

    @Override
    public TimeEntry find(long timeEntryId) {

        return jdbcTemplate.query("SELECT id, project_id, user_id, date, hours FROM time_entries WHERE id = ?", new Object[]{timeEntryId},
                rs -> {
                    if (rs.next()) {
                        TimeEntry timeEntry = new TimeEntry();
                        timeEntry.setTimeEntryId(rs.getLong(1));
                        timeEntry.setProjectId(rs.getLong(2));
                        timeEntry.setUserId(rs.getLong(3));
                        timeEntry.setDate(rs.getDate(4).toLocalDate());
                        timeEntry.setHours(rs.getInt(5));
                        return timeEntry;
                    }
                    else
                        return null;
                });
    }

    @Override
    public List<TimeEntry> list() {
        return jdbcTemplate.query("SELECT id, project_id, user_id, date, hours FROM time_entries",
                (rs, rowNum) -> {
                    TimeEntry timeEntry = new TimeEntry();
                    timeEntry.setTimeEntryId(rs.getLong(1));
                    timeEntry.setProjectId(rs.getLong(2));
                    timeEntry.setUserId(rs.getLong(3));
                    timeEntry.setDate(rs.getDate(4).toLocalDate());
                    timeEntry.setHours(rs.getInt(5));
                    return timeEntry;

                });
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        jdbcTemplate.update("UPDATE time_entries SET project_id = ?, user_id = ?, date =?, hours=? where id = ?", timeEntry.getProjectId(), timeEntry.getUserId(), Date.valueOf(timeEntry.getDate()), timeEntry.getHours(), id);
        timeEntry.setTimeEntryId(id);
        return timeEntry;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM time_entries where id = ?", id);
    }
}
