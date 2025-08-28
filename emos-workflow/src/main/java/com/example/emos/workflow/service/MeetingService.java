package com.example.emos.workflow.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MeetingService {
    public HashMap searchMeetingByInstanceId(String instanceId);

    public HashMap searchMeetingByUUID(String uuid);

    public Long searchRoomIdByUUID(String uuid);

    public List<String> searchUserMeetingInMonth(HashMap param);

    public void updateMeetingStatus(HashMap param);

    public ArrayList<Integer> searchMeetingUnpresent(String uuid);

    public int updateMeetingUnpresent(HashMap param);


}
