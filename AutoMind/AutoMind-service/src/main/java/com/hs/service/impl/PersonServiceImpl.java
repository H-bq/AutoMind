package com.hs.service.impl;

import com.hs.domain.po.Person;
import com.hs.mapper.PersonMapper;
import com.hs.service.IPersonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hbq
 * @since 2026-05-13
 */
@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements IPersonService {

}
