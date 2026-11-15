package com.sp.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.NewUser;

@Mapper
public interface UserMapper {

	public int insertUser(NewUser user);
}
