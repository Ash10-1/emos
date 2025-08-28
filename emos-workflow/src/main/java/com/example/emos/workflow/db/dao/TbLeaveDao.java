package com.example.emos.workflow.db.dao;


import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbLeaveDao {
    public HashMap searchLeaveByInstanceId(String instanceId);

    public int updateLeaveStatus(HashMap param);
}
