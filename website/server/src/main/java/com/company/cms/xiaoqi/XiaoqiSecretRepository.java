package com.company.cms.xiaoqi;

import com.company.cms.lead.LeadCrypto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class XiaoqiSecretRepository {

    private final JdbcTemplate jdbc;
    private final LeadCrypto crypto;

    public XiaoqiSecretRepository(JdbcTemplate jdbc, LeadCrypto crypto) {
        this.jdbc = jdbc;
        this.crypto = crypto;
    }

    public XiaoqiSecret load() {
        return jdbc.queryForObject("""
                SELECT api_url, api_key_cipher, api_key_last4, auth_header, auth_prefix,
                       model_name, system_prompt, timeout_ms FROM xiaoqi_secret WHERE id = 1
                """, (rs, n) -> {
            byte[] cipher = rs.getBytes("api_key_cipher");
            String key = cipher == null ? "" : crypto.decrypt(cipher);
            return new XiaoqiSecret(rs.getString("api_url"), key, rs.getString("api_key_last4"),
                    rs.getString("auth_header"), rs.getString("auth_prefix"), rs.getString("model_name"),
                    rs.getString("system_prompt"), rs.getInt("timeout_ms"));
        });
    }

    public void save(XiaoqiSecret s, Long actorId) {
        boolean hasKey = s.apiKey() != null && !s.apiKey().isEmpty();
        byte[] cipher = hasKey ? crypto.encrypt(s.apiKey()) : null;
        jdbc.update("""
                UPDATE xiaoqi_secret SET api_url = ?, api_key_cipher = ?, api_key_last4 = ?, auth_header = ?,
                       auth_prefix = ?, model_name = ?, system_prompt = ?, timeout_ms = ?, updated_by = ?,
                       updated_at = ? WHERE id = 1
                """, s.apiUrl(), cipher, hasKey ? last4(s.apiKey()) : "", s.authHeader(),
                s.authPrefix(), s.modelName(), s.systemPrompt(), s.timeoutMs(), actorId,
                Timestamp.from(Instant.now()));
    }

    static String last4(String key) {
        return key.length() <= 4 ? "" : key.substring(key.length() - 4);
    }
}
