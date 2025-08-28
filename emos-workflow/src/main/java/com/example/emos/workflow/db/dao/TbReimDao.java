package com.example.emos.workflow.db.dao;

import com.example.emos.workflow.db.pojo.TbAmect;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbReimDao {
    public HashMap searchReimByInstanceId(String instanceId);

    public int updateReimStatus(HashMap param);
}
