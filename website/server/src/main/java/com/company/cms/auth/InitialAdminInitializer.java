package com.company.cms.auth;

import com.company.cms.system.AuditLogService;
import com.company.cms.system.RoleCode;
import com.company.cms.system.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始管理员：仅在账号表为空且显式提供环境变量时创建一次。
 * 不写入任何固定默认密码；未提供变量时保持空表并打印告警。
 */
@Component
public class InitialAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminInitializer.class);
    private static final String USERNAME_PATTERN = "^[A-Za-z0-9_-]{4,50}$";

    private final SysUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService audit;

    private final String username;
    private final String password;

    public InitialAdminInitializer(SysUserRepository users, PasswordEncoder passwordEncoder,
                                   AuditLogService audit,
                                   @Value("${cms.init.username:}") String username,
                                   @Value("${cms.init.password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            log.warn("账号表为空，且未提供 CMS_INIT_USERNAME / CMS_INIT_PASSWORD，未创建初始管理员；"
                    + "请设置后重启一次，创建完成后移除这两个变量");
            return;
        }
        if (!username.trim().matches(USERNAME_PATTERN)) {
            log.error("初始管理员账号不符合规则（4~50 位字母、数字、下划线、连字符），未创建");
            return;
        }
        if (password.length() < 12 || password.length() > 128) {
            log.error("初始管理员密码长度需在 12 到 128 位之间，未创建");
            return;
        }

        long id = users.insert(username.trim(), passwordEncoder.encode(password), "系统管理员", RoleCode.ADMIN);
        audit.success(null, "INIT_ADMIN", "USER", id, "创建初始管理员 " + username.trim());
        log.info("已创建初始管理员 id={}。请登录后立即修改密码，并从运行环境移除 CMS_INIT_PASSWORD", id);
    }
}
