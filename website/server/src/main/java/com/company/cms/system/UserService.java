package com.company.cms.system;

import com.company.cms.auth.CmsPrincipal;
import com.company.cms.auth.SessionRevoker;
import com.company.cms.common.ApiException;
import com.company.cms.common.ErrorCode;
import com.company.cms.common.PageQuery;
import com.company.cms.common.PageResult;
import com.company.cms.system.dto.CreateUserRequest;
import com.company.cms.system.dto.ResetPasswordRequest;
import com.company.cms.system.dto.UpdateUserRequest;
import com.company.cms.system.dto.UserListItem;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

/**
 * 账号管理。首版不提供自助注册、找回密码与物理删除。
 */
@Service
public class UserService {

    private final SysUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService audit;
    private final SessionRevoker sessionRevoker;

    public UserService(SysUserRepository users, PasswordEncoder passwordEncoder,
                       AuditLogService audit, SessionRevoker sessionRevoker) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
        this.sessionRevoker = sessionRevoker;
    }

    public PageResult<UserListItem> list(String keyword, PageQuery page) {
        List<UserListItem> items = users.findPage(keyword, page.offset(), page.pageSize())
                .stream().map(this::toItem).toList();
        long total = users.countByKeyword(keyword);
        return PageResult.of(items, page.page(), page.pageSize(), total);
    }

    @Transactional
    public String create(CreateUserRequest request, CmsPrincipal operator) {
        String username = request.username().trim();
        if (users.existsByUsername(username)) {
            throw ApiException.validation("username", "该账号已存在");
        }
        RoleCode role = RoleCode.parse(request.roleCode());
        long id = users.insert(username, passwordEncoder.encode(request.initialPassword()),
                request.displayName().trim(), role);
        audit.success(operator.id(), "USER_CREATE", "USER", id,
                "创建账号 " + username + "（" + role.name() + "）");
        return String.valueOf(id);
    }

    /**
     * 修改账号。降级或禁用最后一个启用的管理员会被拒绝。
     * 为避免并发绕过，先在事务内锁住其他启用的管理员集合再判断。
     */
    @Transactional
    public void update(long id, UpdateUserRequest request, CmsPrincipal operator) {
        SysUser target = users.findById(id)
                .orElseThrow(() -> ApiException.notFound("账号不存在"));

        RoleCode newRole = RoleCode.parse(request.roleCode());
        boolean wasActiveAdmin = target.roleCode() == RoleCode.ADMIN && target.enabled();
        boolean staysActiveAdmin = newRole == RoleCode.ADMIN && request.enabled();

        if (wasActiveAdmin && !staysActiveAdmin && users.lockOtherEnabledAdmins(id) == 0) {
            throw new ApiException(ErrorCode.LAST_ADMIN_PROTECTED,
                    "不能禁用或降级最后一个启用的管理员");
        }

        int updated = users.updateProfile(id, request.displayName().trim(), newRole,
                request.enabled(), request.expectedLockVersion());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "账号已被他人修改，请刷新后重试");
        }

        if (target.enabled() && !request.enabled()) {
            sessionRevoker.revokeAll(target.username());
        }

        audit.success(operator.id(), "USER_UPDATE", "USER", id,
                "修改账号 " + target.username() + "：角色 " + newRole.name()
                        + "，状态 " + (request.enabled() ? "启用" : "禁用"));
    }

    @Transactional
    public void resetPassword(long id, ResetPasswordRequest request, CmsPrincipal operator) {
        SysUser target = users.findById(id)
                .orElseThrow(() -> ApiException.notFound("账号不存在"));

        int updated = users.updatePassword(id, passwordEncoder.encode(request.newPassword()),
                request.expectedLockVersion());
        if (updated == 0) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT, "账号已被他人修改，请刷新后重试");
        }

        sessionRevoker.revokeAll(target.username());
        audit.success(operator.id(), "USER_RESET_PASSWORD", "USER", id,
                "重置账号 " + target.username() + " 的密码，会话已撤销");
    }

    private UserListItem toItem(SysUser user) {
        String lastLogin = user.lastLoginAt() == null ? null
                : user.lastLoginAt().atOffset(ZoneOffset.UTC).toString();
        return new UserListItem(String.valueOf(user.id()), user.username(), user.displayName(),
                user.roleCode().name(), user.enabled(), user.lockVersion(), lastLogin);
    }
}
