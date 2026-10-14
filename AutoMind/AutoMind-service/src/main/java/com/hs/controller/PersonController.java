package com.hs.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hs.common.domain.R;
import com.hs.common.utils.UserContext;
import com.hs.domain.po.Person;
import com.hs.domain.vo.ReadModelVO;
import com.hs.service.IPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Tag(name = "Person", description = "人员档案查询接口")
@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {

    private final IPersonService personService;

//    @Operation(summary = "查询人员档案列表")
    @GetMapping("/list")
    public R<List<ReadModelVO.PersonVO>> list() {
        String tenantId = UserContext.requireTenantId();
        return R.ok(personService.lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), Person::getTenantId, tenantId)
                .list().stream().map(ReadModelVO::toPersonVO).toList());
    }

//    @Operation(summary = "根据 userId 查询人员档案")
    @GetMapping("/{userId}")
    public R<ReadModelVO.PersonVO> getByUserId(@PathVariable("userId") Integer userId) {
        String tenantId = UserContext.requireTenantId();
        Person person = personService.getOne(Wrappers.<Person>lambdaQuery()
                .eq(!UserContext.isAdminTenant(tenantId), Person::getTenantId, tenantId)
                .eq(Person::getUserId, userId));
        return R.ok(ReadModelVO.toPersonVO(person));
    }

}
