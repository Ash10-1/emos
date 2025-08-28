package com.example.emos.workflow.service;

import cn.hutool.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public interface WorkflowService {
    public String startMeetingProcess(HashMap param);

    public String startLeaveProcess(HashMap param);

    public String startReimProcess(HashMap param);



    public void approvalTask(HashMap param);

    public void archiveTask(HashMap param);

    public boolean searchProcessStatus(String instanceId);

    public void deleteProcessById(String uuid, String instanceId, String type, String reason);

    public ArrayList searchProcessUsers(String instanceId);

    public HashMap searchTaskByPage(HashMap param);

    public HashMap searchApprovalContent(String instanceId, int userId,String[] role, String type, String status);

    public void turnTask(int userId, int assigneeId);

}
