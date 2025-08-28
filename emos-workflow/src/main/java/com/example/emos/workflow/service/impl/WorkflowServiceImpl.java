package com.example.emos.workflow.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import com.example.emos.workflow.bo.Approval;
import com.example.emos.workflow.config.quartz.MeetingWorkflowJob;
import com.example.emos.workflow.config.quartz.QuartzUtil;
import com.example.emos.workflow.service.LeaveService;
import com.example.emos.workflow.service.MeetingService;
import com.example.emos.workflow.service.ReimService;
import com.example.emos.workflow.service.WorkflowService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.drools.core.base.RuleNameStartsWithAgendaFilter;
import org.kie.api.runtime.KieSession;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkflowServiceImpl implements WorkflowService {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private KieSession kieSession;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private ReimService reimService;

    @Autowired
    private QuartzUtil quartzUtil;

    @Override
    public String startMeetingProcess(HashMap param) {
        String instanceId = runtimeService.startProcessInstanceByKey("meeting", param).getProcessInstanceId(); //启动工作流
        String uuid = param.get("uuid").toString();
        String date = param.get("date").toString();
        String start = param.get("start").toString();

        /*
         * 创建定时器，执行时间为会议开始时间。
         * 如果会议开始前，该会议还没有审批通过，定时器就把会议状态更新成2，然后关闭工作流
         * 如果会议审批通过，需要删除这个定时器任务
         */
        JobDetail jobDetail = JobBuilder.newJob(MeetingWorkflowJob.class).build();
        Map dataMap = jobDetail.getJobDataMap();
        dataMap.put("uuid", uuid);
        dataMap.put("instanceId", instanceId);

        Date executeDate = DateUtil.parse(date + " " + start, "yyyy-MM-dd HH:mm:ss");
        quartzUtil.addJob(jobDetail, uuid, "会议工作流组", executeDate); //创建定时任务，检查工作流审批状态

        return instanceId;
    }

    @Override
    public String startLeaveProcess(HashMap param) {
        String instanceId = runtimeService.startProcessInstanceByKey("leave", param).getProcessInstanceId(); //启动工作流
        return instanceId;
    }

    @Override
    public String startReimProcess(HashMap param) {
        String instanceId = runtimeService.startProcessInstanceByKey("reim", param).getProcessInstanceId(); //启动工作流
        return instanceId;
    }



    @Override
    public void approvalTask(HashMap param) {
        String taskId = MapUtil.getStr(param, "taskId");
        String approval = MapUtil.getStr(param, "approval");
        taskService.setVariableLocal(taskId, "result", approval);
        taskService.complete(taskId);
    }

    @Override
    public void archiveTask(HashMap param) {
        String taskId = MapUtil.getStr(param, "taskId");
        int userId = MapUtil.getInt(param, "userId");
        JSONArray files = (JSONArray) param.get("files");
        taskService.setVariable(taskId, "files", files); //把归档文件信息存储在工作流实例中
        taskService.setVariable(taskId,"filing",false);
        //认领该任务，否则查询不到我审批过该记录
        taskService.setOwner(taskId, userId + "");
        taskService.setAssignee(taskId, userId + "");
        taskService.complete(taskId);
    }


    @Override
    public boolean searchProcessStatus(String instanceId) {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (instance != null) {
            //工作流未结束
            return false;
        } else {
            //工作流已经结束
            return true;
        }
    }

    @Override
    public void deleteProcessById(String uuid, String instanceId, String type, String reason) {
        long count = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).count();
        if (count > 0) {
            runtimeService.deleteProcessInstance(instanceId, reason);    //删除工作流
        }
        count = historyService.createHistoricProcessInstanceQuery().processInstanceId(instanceId).count();
        if (count > 0) {
            historyService.deleteHistoricProcessInstance(instanceId);   //删除工作流历史
        }
        //判断是否是会议工作流，然后删除定时器
        if (type.equals("会议申请")) {
            quartzUtil.deleteJob(uuid, "会议开始任务组");
            quartzUtil.deleteJob(uuid, "会议结束任务组");
            quartzUtil.deleteJob(uuid, "会议工作流组");
            quartzUtil.deleteJob(uuid, "创建会议室ID任务组");
        }


    }

    @Override
    public ArrayList searchProcessUsers(String instanceId) {
        List<HistoricTaskInstance> taskList = historyService.createHistoricTaskInstanceQuery().processInstanceId(instanceId).finished().list();
        ArrayList<String> list = new ArrayList<>();
        taskList.forEach(one -> {
            list.add(one.getAssignee());
        });
        return list;
    }

    @Override
    public HashMap searchTaskByPage(HashMap param) {
        ArrayList<Approval> list = new ArrayList();
        int userId = (Integer) param.get("userId");
        JSONArray role = (JSONArray) param.get("role");
        int start = (Integer) param.get("start");
        int length = (Integer) param.get("length");
        String status = (String) param.get("status");
        String creatorName = MapUtil.getStr(param, "creatorName");
        String type = MapUtil.getStr(param, "type");
        String instanceId = MapUtil.getStr(param, "instanceId");
        Long totalCount = 0L;
        List<String> assignee = new ArrayList();
        assignee.add(userId + "");
        role.forEach(one -> {
            assignee.add(one.toString());
        });

        if ("待审批".equals(status)) {
            TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskCreateTime().desc()
                    .includeProcessVariables().includeTaskLocalVariables().taskAssigneeIds(assignee);
            if (StrUtil.isNotBlank(creatorName)) {
                taskQuery.processVariableValueEquals("creatorName", creatorName);
            }
            if (StrUtil.isNotBlank(type)) {
                taskQuery.processVariableValueEquals("type", type);
            }
            if (StrUtil.isNotBlank(instanceId)) {
                taskQuery.processInstanceId(instanceId);
            }
            totalCount = taskQuery.count();
            List<Task> taskList = taskQuery.listPage(start, length);
            for (Task task : taskList) {
                Map map = task.getProcessVariables();
                Approval approval = createApproval(task.getProcessInstanceId(), status, map);
                approval.setTaskId(task.getId());
                list.add(approval);
            }
        } else {
            if ("已审批".equals(status)) {
                HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                        .orderByHistoricTaskInstanceStartTime().desc()
                        .includeTaskLocalVariables().includeProcessVariables()
                        .taskAssigneeIds(assignee).finished().processUnfinished();
                if (StrUtil.isNotBlank(creatorName)) {
                    taskQuery.processVariableValueEquals("creatorName", creatorName);
                }
                if (StrUtil.isNotBlank(type)) {
                    taskQuery.processVariableValueEquals("type", type);
                }
                if (StrUtil.isNotBlank(instanceId)) {
                    taskQuery.processInstanceId(instanceId);
                }
                totalCount = taskQuery.count();
                List<HistoricTaskInstance> taskList = taskQuery.listPage(start, length);
                for (HistoricTaskInstance task : taskList) {
                    Map map = task.getProcessVariables();
                    Approval approval = createApproval(task.getProcessInstanceId(), status, map);
                    approval.setTaskId(task.getId());
                    list.add(approval);
                }
            } else if ("已结束".equals(status)) {
                HistoricTaskInstanceQuery taskQuery = historyService.createHistoricTaskInstanceQuery()
                        .orderByHistoricTaskInstanceStartTime().desc()
                        .includeTaskLocalVariables().includeProcessVariables()
                        .taskAssigneeIds(assignee).finished().processFinished();
                List<HistoricTaskInstance> taskList = taskQuery.listPage(start, length);
                for (HistoricTaskInstance task : taskList) {
                    Map map = task.getProcessVariables();
                    Approval approval = createApproval(task.getProcessInstanceId(), status, map);
                    approval.setTaskId(task.getId());
                    list.add(approval);
                }


//                HistoricProcessInstanceQuery processInstanceQuery = historyService.createHistoricProcessInstanceQuery()
//                        .orderByProcessInstanceStartTime().desc().includeProcessVariables()
//                        .involvedUser(userId + "").finished();
//                if (StrUtil.isNotBlank(creatorName)) {
//                    processInstanceQuery.variableValueEquals("creatorName", creatorName);
//                }
//                if (StrUtil.isNotBlank(type)) {
//                    processInstanceQuery.variableValueEquals("type", type);
//                }
//                if (StrUtil.isNotBlank(instanceId)) {
//                    processInstanceQuery.processInstanceId(instanceId);
//                }
//                totalCount = processInstanceQuery.count();
//                List<HistoricProcessInstance> processInstancesList = processInstanceQuery.listPage(start, length);
//                for (HistoricProcessInstance instance : processInstancesList) {
//                    Map map = instance.getProcessVariables();
//                    Approval approval = createApproval(instance.getId(), status, map);
//                    list.add(approval);
//
//                }


            }
        }
        HashMap map = new HashMap();
        map.put("list", list);
        map.put("totalCount", totalCount);
        map.put("pageIndex", start);
        map.put("pageSize", length);
        return map;
    }

    @Override
    public HashMap searchApprovalContent(String instanceId, int userId, String[] role, String type, String status) {
        HashMap map = null;
        List<String> assignee = new ArrayList();
        assignee.add(userId + "");
        for (String one : role) {
            assignee.add(one);
        }
        if ("会议申请".equals(type)) {
            map = meetingService.searchMeetingByInstanceId(instanceId);
        } else if ("员工请假".equals(type)) {
            map = leaveService.searchLeaveByInstanceId(instanceId);
        } else if ("报销申请".equals(type)) {
            map = reimService.searchReimByInstanceId(instanceId);
        }
        Map variables;
        if (!"已结束".equals(status)) {
            variables = runtimeService.getVariables(instanceId);
        } else {
            HistoricTaskInstance instance = historyService.createHistoricTaskInstanceQuery()
                    .includeTaskLocalVariables().includeProcessVariables().processInstanceId(instanceId).taskAssigneeIds(assignee).processFinished().list().get(0);
            variables = instance.getProcessVariables();
        }
        if (variables != null && variables.containsKey("files")) {
            ArrayNode files = (ArrayNode) variables.get("files");
            map.put("files", files);
        }

//        map.put("list", variables.get("list"));
        ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (instance != null) {
            map.put("result", "");
        } else {
            map.put("result", variables.get("result"));
        }
        return map;
    }

    @Override
    public void turnTask(int userId, int assigneeId) {
        List<Task> list = taskService.createTaskQuery().taskAssignee(userId + "").list();
        list.forEach(task -> {
            taskService.setOwner(task.getId(), assigneeId + "");
            taskService.setAssignee(task.getId(), assigneeId + "");
        });
    }

    private Approval createApproval(String processId, String status, Map map) {
        String type = (String) map.get("type");
        String createDate = (String) map.get("createDate");
        boolean filing = (Boolean) map.get("filing");

        Approval approval = new Approval();
        approval.setCreatorName(map.get("creatorName").toString());
        approval.setProcessId(processId);
        approval.setType(type);
        approval.setTitle(map.get("title").toString());
        approval.setStatus(status);
        approval.setCreateDate(createDate);
        approval.setFiling(filing);
        approval.setResult(MapUtil.getStr(map, "result"));

        kieSession.setGlobal("type", type);
        kieSession.setGlobal("map", map);
        kieSession.insert(approval);
        kieSession.fireAllRules(new RuleNameStartsWithAgendaFilter("approval-list"));
        return approval;
    }
}
