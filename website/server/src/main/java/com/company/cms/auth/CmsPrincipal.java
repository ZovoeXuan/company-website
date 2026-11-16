package com.company.cms.auth;

import com.company.cms.system.RoleCode;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 登录主体。实现 CredentialsContainer 且将 passwordHash 声明为 transient：
 * 认证完成后自动擦除密码哈希，Spring Session 持久化到数据库时绝不保存 passwordHash。
 */
public final class CmsPrincipal implements UserDetails, CredentialsContainer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long id;
    private final String username;
    private final String displayName;
    private final RoleCode roleCode;
    private transient String passwordHash;
    private final boolean enabled;

    public CmsPrincipal(
            long id,
            String username,
            String displayName,
            RoleCode roleCode,
            String passwordHash,
            boolean enabled
    ) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.roleCode = roleCode;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
    }

    public long id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String displayName() {
        return displayName;
    }

    public RoleCode roleCode() {
        return roleCode;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public boolean enabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CmsPrincipal that)) return false;
        return id == that.id && enabled == that.enabled
                && Objects.equals(username, that.username)
                && roleCode == that.roleCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, roleCode, enabled);
    }
}
