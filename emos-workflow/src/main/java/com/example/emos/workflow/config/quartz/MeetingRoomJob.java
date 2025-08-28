package com.example.emos.workflow.config.quartz;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.Map;

public class MeetingRoomJob extends QuartzJobBean {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        Map map = ctx.getJobDetail().getJobDataMap();
        String uuid = map.get("uuid").toString();
        Date date = DateUtil.parse(map.get("expire").toString(), "yyyy-MM-dd HH:mm:ss");
        //生成当前唯一的视频会议房间ID
        for (; ; ) {
            long roomId = RandomUtil.randomLong(1L, 4294967295L);
            if (redisTemplate.hasKey("roomId-" + roomId)) {
                continue;
            } else {
                redisTemplate.opsForValue().set("roomId-" + roomId, uuid);
                redisTemplate.expireAt("roomId-" + roomId, date);

                redisTemplate.opsForValue().set(uuid, roomId);
                redisTemplate.expireAt(uuid, date);
                break;
            }
        }
    }
}