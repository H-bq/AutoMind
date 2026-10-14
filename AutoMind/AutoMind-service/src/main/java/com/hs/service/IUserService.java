package com.hs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.domain.dto.LoginDTO;
import com.hs.domain.dto.RegisterDTO;
import com.hs.domain.po.User;
import com.hs.domain.vo.LoginVO;

public interface IUserService extends IService<User> {

    LoginVO login(LoginDTO dto);

    LoginVO register(RegisterDTO dto);
}
