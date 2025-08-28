package com.example.emos.workflow.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface TbMeetingDao {
    public HashMap searchMeetingByInstanceId(String instanceId);

    public boolean searchMeetingMembersInSameDept(String uuid);

    public HashMap searchMeetingByUUID(String uuid);

    public List<String> searchUserMeetingInMonth(HashMap param);

    public int updateMeetingStatus(HashMap param);

    public ArrayList<Integer> searchMeetingUnpresent(String uuid);

    public int updateMeetingUnpresent(HashMap param);
}
