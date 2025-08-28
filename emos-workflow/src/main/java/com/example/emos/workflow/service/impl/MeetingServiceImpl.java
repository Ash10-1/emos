package com.example.emos.workflow.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.example.emos.workflow.db.dao.TbMeetingDao;
import com.example.emos.workflow.exception.EmosException;
import com.example.emos.workflow.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class MeetingServiceImpl implements MeetingService {
    @Autowired
    private TbMeetingDao meetingDao;

    @Autowired
    @Qualifier(value = "redisTemplate")
    private RedisTemplate redisTemplate;


    public HashMap searchMeetingByInstanceId(String instanceId) {
        HashMap map = meetingDao.searchMeetingByInstanceId(instanceId);
        String date = map.get("date").toString();
        String start = map.get("start").toString();
        DateTime startDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm");
        String end = map.get("end").toString();
        DateTime endDate = DateUtil.parse(date + " " + end, "yyyy-MM-dd HH:mm");
        long hours = DateUtil.between(endDate, startDate, DateUnit.HOUR, true);
        map.put("hours", hours);
        return map;
    }

    @Override
    public HashMap searchMeetingByUUID(String uuid) {
        HashMap meeting = meetingDao.searchMeetingByUUID(uuid);
        return meeting;
    }


    @Override
    public Long searchRoomIdByUUID(String uuid) {
        Object temp = redisTemplate.opsForValue().get(uuid);
        long roomId = Long.parseLong(temp.toString());
        return roomId;
    }

    @Override
    public List<String> searchUserMeetingInMonth(HashMap param) {
        List list = meetingDao.searchUserMeetingInMonth(param);
        return list;
    }

    @Override
    public void updateMeetingStatus(HashMap param) {
        int row = meetingDao.updateMeetingStatus(param);
        if (row != 1) {
            throw new EmosException("会议状态更新失败");
        }
    }

    @Override
    public ArrayList<Integer> searchMeetingUnpresent(String uuid) {
        ArrayList<Integer> list=meetingDao.searchMeetingUnpresent(uuid);
        return list;
    }

    @Override
    public int updateMeetingUnpresent(HashMap param) {
        int rows=meetingDao.updateMeetingUnpresent(param);
        return rows;
    }
}
