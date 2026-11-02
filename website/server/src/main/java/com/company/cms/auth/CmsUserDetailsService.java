package com.company.cms.auth;

import com.company.cms.system.SysUser;
import com.company.cms.system.SysUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CmsUserDetailsService implements UserDetailsService {

    private final SysUserRepository users;

    public CmsUserDetailsService(SysUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("账号或密码错误"));
        return new CmsPrincipal(user.id(), user.username(), user.displayName(),
                user.roleCode(), user.passwordHash(), user.enabled());
    }
}
