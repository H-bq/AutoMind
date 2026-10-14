package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录请求")
public class LoginDTO {

    @Schema(description = "用户名", example = "tenant1_admin")
    private String username;

    @Schema(description = "密码明文", example = "123456")
    private String password;
}
