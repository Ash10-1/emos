package com.example.emos.workflow.config.quartz;

import com.example.emos.workflow.db.dao.TbMeetingDao;
import com.example.emos.workflow.service.MeetingService;
import com.example.emos.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 检查工作流的会议审批任务
 */
@Slf4j
@Component
public class MeetingWorkflowJob extends QuartzJobBean {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private WorkflowService workflowService;

    /**
     * 检查工作里的审批状态
     *
     * @param ctx
     * @throws JobExecutionException
     */
    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        Map map = ctx.getJobDetail().getJobDataMap();
        String uuid = map.get("uuid").toString();
        String instanceId = map.get("instanceId").toString();
        //判断会议审批是不是未结束
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (instance != null) {
            map.put("processStatus", "未结束");
            workflowService.deleteProcessById(uuid, instanceId, "会议", "会议过期");
            HashMap param = new HashMap();
            param.put("uuid", uuid);
            param.put("status", 2); //更改会议状态为已拒绝
            meetingService.updateMeetingStatus(param); //更新会议状态
            log.debug("会议已失效");
        }
    }
}

