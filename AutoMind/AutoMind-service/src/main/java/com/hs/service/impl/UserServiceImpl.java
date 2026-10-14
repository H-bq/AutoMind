package com.hs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hs.common.exception.BadRequestException;
import com.hs.domain.dto.LoginDTO;
import com.hs.domain.dto.RegisterDTO;
import com.hs.domain.po.NetworkDomain;
import com.hs.domain.po.Person;
import com.hs.domain.po.User;
import com.hs.domain.vo.LoginVO;
import com.hs.mapper.UserMapper;
import com.hs.security.JwtTokenService;
import com.hs.service.INetworkDomainService;
import com.hs.service.IPersonService;
import com.hs.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final IPersonService personService;
    private final INetworkDomainService networkDomainService;
    private final JwtTokenService jwtTokenService;

    @Override
    public LoginVO login(LoginDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BadRequestException("用户名和密码不能为空");
        }

        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BadRequestException("用户名或密码错误");
        }

        String passwordMd5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!passwordMd5.equalsIgnoreCase(user.getPassword())) {
            throw new BadRequestException("用户名或密码错误");
        }

        Person person = personService.lambdaQuery()
                .eq(Person::getUserId, user.getUserId())
                .one();
        if (person == null || person.getTenantId() == null || person.getTenantId().isBlank()) {
            throw new BadRequestException("用户未绑定租户");
        }

        return toLoginVO(user, person);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank()
                || dto.getPassword() == null || dto.getPassword().isBlank()
                || dto.getDomainCode() == null || dto.getDomainCode().isBlank()) {
            throw new BadRequestException("用户名、密码和租户网络域编码不能为空");
        }

        boolean usernameExists = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .exists();
        if (usernameExists) {
            throw new BadRequestException("用户名已存在");
        }

        NetworkDomain domain = networkDomainService.lambdaQuery()
                .eq(NetworkDomain::getDomainCode, dto.getDomainCode())
                .last("limit 1")
                .one();
        if (domain == null || domain.getTenantId() == null || domain.getTenantId().isBlank()) {
            throw new BadRequestException("租户网络域编码不存在或未绑定租户");
        }

        User user = new User()
                .setUsername(dto.getUsername())
                .setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))
                .setStatus("ACTIVE")
                .setIdentity("USER");
        save(user);

        Person person = new Person()
                .setUserId(user.getUserId())
                .setTenantId(domain.getTenantId())
                .setName(dto.getUsername())
                .setPosition(defaultText(domain.getDomainName(), domain.getDomainCode()) + "注册用户")
                .setIntroduce("通过网络域编码 " + domain.getDomainCode() + " 注册")
                .setLabel("注册用户");
        personService.save(person);

        return toLoginVO(user, person);
    }

    private LoginVO toLoginVO(User user, Person person) {
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getIdentity());
        vo.setTenantId(person.getTenantId());
        vo.setName(person.getName());
        vo.setPosition(person.getPosition());
        vo.setToken(jwtTokenService.createToken(user.getUserId(), person.getTenantId(), user.getIdentity(), user.getUsername()));
        vo.setTenantHeaderName("X-Tenant-Id");
        vo.setUserHeaderName("X-User-Id");
        vo.setRoleHeaderName("X-User-Role");
        vo.setIntentHeaderName("X-Intent-Id");
        return vo;
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
