package com.company.cms.system;

import com.company.cms.common.UtcTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * sys_user 数据访问。所有 SQL 使用绑定参数，不在 Controller 中出现。
 */
@Repository
public class SysUserRepository {

    private static final RowMapper<SysUser> ROW_MAPPER = (rs, rowNum) -> new SysUser(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("display_name"),
            RoleCode.valueOf(rs.getString("role_code")),
            rs.getInt("enabled") == 1,
            rs.getInt("lock_version"),
            rs.getTimestamp("last_login_at") == null ? null : rs.getTimestamp("last_login_at").toLocalDateTime()
    );

    private final JdbcTemplate jdbc;

    public SysUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<SysUser> findByUsername(String username) {
        List<SysUser> rows = jdbc.query(
                "SELECT * FROM sys_user WHERE username = ?", ROW_MAPPER, username);
        return rows.stream().findFirst();
    }

    public Optional<SysUser> findById(long id) {
        List<SysUser> rows = jdbc.query("SELECT * FROM sys_user WHERE id = ?", ROW_MAPPER, id);
        return rows.stream().findFirst();
    }

    public long count() {
        Long value = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Long.class);
        return value == null ? 0L : value;
    }

    public long insert(String username, String passwordHash, String displayName, RoleCode roleCode) {
        String sql = """
                INSERT INTO sys_user
                    (username, password_hash, display_name, role_code, enabled, lock_version, created_at, updated_at)
                VALUES (?, ?, ?, ?, 1, 0, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, displayName);
            ps.setString(4, roleCode.name());
            ps.setObject(5, UtcTime.now());
            ps.setObject(6, UtcTime.now());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建账号未返回主键");
        }
        return key.longValue();
    }

    /**
     * 条件更新：版本不匹配时返回 0，由 Service 转换为 409，避免覆盖他人修改。
     */
    public int updateProfile(long id, String displayName, RoleCode roleCode, boolean enabled,
                             int expectedLockVersion) {
        return jdbc.update("""
                        UPDATE sys_user
                           SET display_name = ?, role_code = ?, enabled = ?,
                               lock_version = lock_version + 1, updated_at = ?
                         WHERE id = ? AND lock_version = ?
                        """,
                displayName, roleCode.name(), enabled ? 1 : 0, UtcTime.now(), id, expectedLockVersion);
    }

    public int updatePassword(long id, String passwordHash, Integer expectedLockVersion) {
        if (expectedLockVersion == null) {
            return jdbc.update("""
                            UPDATE sys_user
                               SET password_hash = ?, lock_version = lock_version + 1, updated_at = ?
                             WHERE id = ?
                            """,
                    passwordHash, UtcTime.now(), id);
        }
        return jdbc.update("""
                        UPDATE sys_user
                           SET password_hash = ?, lock_version = lock_version + 1, updated_at = ?
                         WHERE id = ? AND lock_version = ?
                        """,
                passwordHash, UtcTime.now(), id, expectedLockVersion);
    }

    public int touchLastLogin(long id) {
        return jdbc.update("UPDATE sys_user SET last_login_at = ? WHERE id = ?", UtcTime.now(), id);
    }

    /**
     * 锁住「其他启用的管理员」集合，供并发降级/禁用时做最后管理员检查。
     * 必须在事务中调用。
     */
    public long lockOtherEnabledAdmins(long excludeUserId) {
        List<Long> ids = jdbc.queryForList(
                "SELECT id FROM sys_user WHERE role_code = 'ADMIN' AND enabled = 1 AND id <> ? FOR UPDATE",
                Long.class, excludeUserId);
        return ids.size();
    }

    public List<SysUser> findPage(String keyword, int offset, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return jdbc.query("""
                    SELECT * FROM sys_user ORDER BY id DESC LIMIT ? OFFSET ?
                    """, ROW_MAPPER, limit, offset);
        }
        String pattern = toLikePattern(keyword.trim());
        return jdbc.query("""
                        SELECT * FROM sys_user
                         WHERE username LIKE ? OR display_name LIKE ?
                         ORDER BY id DESC LIMIT ? OFFSET ?
                        """,
                ROW_MAPPER, pattern, pattern, limit, offset);
    }

    public long countByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            Long value = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user", Long.class);
            return value == null ? 0L : value;
        }
        String pattern = toLikePattern(keyword.trim());
        Long value = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username LIKE ? OR display_name LIKE ?",
                Long.class, pattern, pattern);
        return value == null ? 0L : value;
    }

    public boolean existsByUsername(String username) {
        Long value = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = ?", Long.class, username);
        return value != null && value > 0;
    }

    private String toLikePattern(String keyword) {
        String escaped = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
