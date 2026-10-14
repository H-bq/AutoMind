package com.hs.controller;

import com.hs.common.domain.R;
import com.hs.domain.dto.LoginDTO;
import com.hs.domain.dto.RegisterDTO;
import com.hs.domain.vo.LoginVO;
import com.hs.domain.vo.ReadModelVO;
import com.hs.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User", description = "用户查询与登录接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @Operation(summary = "用户登录", description = "校验用户名密码，返回前端后续请求需要携带的租户、用户、角色请求头上下文")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = LoginVO.class)))
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }

    @Operation(summary = "用户注册", description = "根据租户网络域编码校验 network_domain，注册成功后把账号绑定到该网络域所属租户")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = LoginVO.class)))
    @PostMapping("/register")
    public R<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return R.ok(userService.register(dto));
    }

//    @Operation(summary = "查询用户列表")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = ReadModelVO.UserVO.class)))
    @GetMapping("/list")
    public R<List<ReadModelVO.UserVO>> list() {
        return R.ok(userService.list().stream().map(ReadModelVO::toUserVO).toList());
    }

//    @Operation(summary = "根据 id 查询用户")
    @ApiResponse(responseCode = "200", description = "成功",
            content = @Content(schema = @Schema(implementation = ReadModelVO.UserVO.class)))
    @GetMapping("/{id}")
    public R<ReadModelVO.UserVO> getById(@Parameter(description = "用户主键") @PathVariable("id") Integer id) {
        return R.ok(ReadModelVO.toUserVO(userService.getById(id)));
    }
}
