package com.company.cms.auth;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 撤销某个账号的全部会话：退出、禁用、重置密码、修改本人密码后调用。
 * 依赖 Spring Session JDBC 的 principalName 索引，不手工改框架表。
 */
@Service
public class SessionRevoker {

    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    public SessionRevoker(FindByIndexNameSessionRepository<? extends Session> sessions) {
        this.sessions = sessions;
    }

    public int revokeAll(String username) {
        Map<String, ? extends Session> found = sessions.findByPrincipalName(username);
        found.keySet().forEach(sessions::deleteById);
        return found.size();
    }
}
