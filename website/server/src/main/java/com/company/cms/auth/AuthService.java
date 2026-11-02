package com.company.cms.auth;

import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.system.AuditLogService;
import com.company.cms.system.SysUser;
import com.company.cms.system.SysUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 登录相关业务：最后登录时间、审计、修改本人密码。
 * 认证本身由 Spring Security 过滤器完成，这里不重复实现密码比对流程。
 */
@Service
public class AuthService {

    private final SysUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService audit;
    private final SessionRevoker sessionRevoker;

    public AuthService(SysUserRepository users, PasswordEncoder passwordEncoder,
                       AuditLogService audit, SessionRevoker sessionRevoker) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
        this.sessionRevoker = sessionRevoker;
    }

    @Transactional
    public void onLoginSuccess(CmsPrincipal principal) {
        users.touchLastLogin(principal.id());
        audit.success(principal.id(), "LOGIN_SUCCESS", "USER", principal.id(),
                "账号 " + principal.username() + " 登录成功");
    }

    @Transactional
    public void onLoginFailure(String username, String reason) {
        audit.failure(null, "LOGIN_FAILURE", "USER", null,
                "账号 " + safeUsername(username) + " 登录失败：" + reason);
    }

    @Transactional
    public void onLogout(CmsPrincipal principal) {
        audit.success(principal.id(), "LOGOUT", "USER", principal.id(),
                "账号 " + principal.username() + " 退出登录");
    }

    /**
     * 修改本人密码：校验旧密码，写入新哈希，撤销该账号全部会话（包含当前会话）。
     */
    @Transactional
    public void changePassword(CmsPrincipal principal, String oldPassword, String newPassword) {
        SysUser user = users.findById(principal.id())
                .orElseThrow(() -> ApiException.notFound("账号不存在"));

        if (!passwordEncoder.matches(oldPassword, user.passwordHash())) {
            audit.failure(user.id(), "PASSWORD_CHANGE", "USER", user.id(), "修改密码失败：原密码不正确");
            throw ApiException.validation("oldPassword", "原密码不正确");
        }
        if (oldPassword.equals(newPassword)) {
            throw ApiException.validation("newPassword", "新密码不能与原密码相同");
        }

        String hash = passwordEncoder.encode(newPassword);
        int updated = users.updatePassword(user.id(), hash, null);
        if (updated != 1) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "账号信息已变化，请重新登录后再试");
        }

        sessionRevoker.revokeAll(user.username());
        audit.success(user.id(), "PASSWORD_CHANGE", "USER", user.id(), "修改本人密码，全部会话已撤销");
    }

    private String safeUsername(String username) {
        if (username == null || username.isBlank()) {
            return "(空)";
        }
        return username.length() <= 50 ? username : username.substring(0, 50);
    }
}
