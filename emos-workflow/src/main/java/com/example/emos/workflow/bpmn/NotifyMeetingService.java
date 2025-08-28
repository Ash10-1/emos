package com.example.emos.workflow.bpmn;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.example.emos.workflow.config.quartz.MeetingRoomJob;
import com.example.emos.workflow.config.quartz.MeetingStatusJob;
import com.example.emos.workflow.config.quartz.QuartzUtil;
import com.example.emos.workflow.service.MeetingService;
import com.example.emos.workflow.service.impl.MeetingServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NotifyMeetingService implements JavaDelegate {
    @Autowired
    private QuartzUtil quartzUtil;

    @Autowired
    private MeetingService meetingService;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        Map map = delegateExecution.getVariables();
        String uuid = MapUtil.getStr(map, "uuid");
        String url = MapUtil.getStr(map, "url");
        String result = MapUtil.getStr(map, "result");

        HashMap data = meetingService.searchMeetingByUUID(uuid);
        String title = MapUtil.getStr(data, "title");
        String date = MapUtil.getStr(data, "date");
        String start = MapUtil.getStr(data, "start");
        String end = MapUtil.getStr(data, "end");

        if (result.equals("同意")) {
            //TODO 判断是不是会议工作流，然后创建视频会议室任务
            //更新会议状态为3
            meetingService.updateMeetingStatus(new HashMap() {{
                put("uuid", uuid);
                put("status", 3);
            }});

            String meetingType = delegateExecution.getVariable("meetingType", String.class);
            //线上会议要创建视频会议室
            if (meetingType.equals("线上会议")) {
                JobDetail jobDetail = JobBuilder.newJob(MeetingRoomJob.class).build();
                Map param = jobDetail.getJobDataMap();
                param.put("uuid", uuid);
                Date expire = DateUtil.parse(date + " " + end, "yyyy-MM-dd HH:mm");
                //roomID过期时间等于会议结束时间
                param.put("expire", expire);
                //会议开始前15分钟，计算roomID
                Date executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm").offset(DateField.MINUTE, -15);

                quartzUtil.addJob(jobDetail, uuid, "创建会议室ID任务组", executeDate);


            }

            quartzUtil.deleteJob(uuid, "会议工作流组");

            //设置会议开始定时器
            JobDetail jobDetail = JobBuilder.newJob(MeetingStatusJob.class).build();
            map = jobDetail.getJobDataMap();
            map.put("uuid", uuid);
            map.put("status", 4);
            map.put("flag", "start");
            Date executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm");
            quartzUtil.addJob(jobDetail, uuid, "会议开始任务组", executeDate);

            //设置会议结束定时器
            jobDetail = JobBuilder.newJob(MeetingStatusJob.class).build();
            map = jobDetail.getJobDataMap();
            map.put("uuid", uuid);
            map.put("status", 5);
            map.put("title", title);
            map.put("date", date);
            map.put("start", start);
            map.put("end", end);
            map.put("flag", "end");
            executeDate = DateUtil.parse(date + " " + end, "yyyy-MM-dd HH:mm");
            quartzUtil.addJob(jobDetail, uuid, "会议结束任务组", executeDate);
        } else {
            meetingService.updateMeetingStatus(new HashMap() {{
                put("uuid", uuid);
                put("status", 2);
            }});
        }

        JSONObject json = new JSONObject();
        json.set("result", result);
        json.set("uuid", uuid);
        String processId = delegateExecution.getProcessInstanceId();
        json.set("processId", processId);
        try {
            HttpResponse response = HttpRequest.post(url).header("Content-Type", "application/json").body(json.toString()).execute();
            log.debug(response.body());
        } catch (Exception e) {
            log.error("发送通知失败", e);
        }

    }
}
