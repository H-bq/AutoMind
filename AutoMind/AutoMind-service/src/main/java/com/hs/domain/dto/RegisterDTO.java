package com.hs.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户注册请求")
public class RegisterDTO {

    @Schema(description = "用户名", example = "tenant1_new_user")
    private String username;

    @Schema(description = "密码明文", example = "123456")
    private String password;

    @Schema(description = "租户网络域编码，对应 network_domain.domain_code", example = "BJ-OFFICE-DOMAIN")
    private String domainCode;
}
