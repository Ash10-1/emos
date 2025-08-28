package com.example.emos.workflow.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.emos.workflow.controller.form.*;
import com.example.emos.workflow.exception.EmosException;
import com.example.emos.workflow.service.WorkflowService;
import com.example.emos.workflow.util.R;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
public class WorkFlowController {

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @PostMapping("/startMeetingProcess")
    public R startMeetingProcess(@Valid @RequestBody StartMeetingProcessForm form) {

        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("filing", false);
        param.put("type", "会议申请");
        param.put("createDate", DateUtil.today());

        param.remove("code");
        if (form.getGmId() == null) {
            //会议状态通过
            param.put("identity", "总经理");
            param.put("result", "同意");

        } else {
            param.put("identity", "员工");
        }

        String instanceId = workflowService.startMeetingProcess(param);
        return R.ok().put("instanceId", instanceId);

    }

    @PostMapping("/startLeaveProcess")
    public R startLeaveProcess(@Valid @RequestBody StartLeaveProcessForm form) {

        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("filing", false);
        param.put("type", "员工请假");
        param.put("createDate", DateUtil.today());

        param.remove("code");

        String instanceId = workflowService.startLeaveProcess(param);
        return R.ok().put("instanceId", instanceId);
    }

    @PostMapping("/startReimProcess")
    public R startReimProcess(@Valid @RequestBody StartReimProcessForm form) {


        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.put("filing", false);
        param.put("type", "报销申请");
        param.put("createDate", DateUtil.today());

        param.remove("code");

        String instanceId = workflowService.startReimProcess(param);
        return R.ok().put("instanceId", instanceId);
    }




    //审批会议任务
    @PostMapping("/approvalTask")
    public R approvalTask(@Valid @RequestBody ApprovalTaskForm form) {

        HashMap param = new HashMap();
        param.put("taskId", form.getTaskId());
        param.put("approval", form.getApproval());
        workflowService.approvalTask(param);
        return R.ok();
    }

    @PostMapping("/archiveTask")
    public R archiveTask(@Valid @RequestBody ArchiveTaskForm form) {
        if (!JSONUtil.isJsonArray(form.getFiles())) {
            return R.error("files不是JSON数组");
        }

        HashMap param = new HashMap() {{
            put("taskId", form.getTaskId());
            put("userId", form.getUserId());
            put("files", JSONUtil.parseArray(form.getFiles()));
        }};

        workflowService.archiveTask(param);
        return R.ok();
    }

    /**
     * 查询工作流是否完成
     */
    @PostMapping("/searchProcessStatus")
    public R searchProcessStatus(@Valid @RequestBody SearchProcessStatusForm form) {

        boolean bool = workflowService.searchProcessStatus(form.getInstanceId());
        if (bool == false) {
            //工作流未结束
            return R.ok().put("result", "未结束");
        } else {
            //工作流已经结束
            return R.ok().put("result", "已结束");
        }
    }

    /**
     * 删除工作流
     */
    @PostMapping("/deleteProcessById")
    public R deleteProcessById(@Valid @RequestBody DeleteProcessByIdForm form) {

        workflowService.deleteProcessById(form.getUuid(), form.getInstanceId(), form.getType(), form.getReason());
        return R.ok();

    }

    /**
     * 查询已经结束工作流实例中所有的用户
     */
    @PostMapping("/searchProcessUsers")
    public R searchProcessUsers(@Valid @RequestBody SearchProcessUsersForm form) {

        ArrayList list = workflowService.searchProcessUsers(form.getInstanceId());
        return R.ok().put("result", list);
    }

    @PostMapping("/searchTaskByPage")
    public R searchTaskByPage(@Valid @RequestBody SearchTaskByPageForm form) {

        int page = form.getPage();
        int length = form.getLength();
        int start = (page - 1) * length;
        HashMap param = JSONUtil.parse(form).toBean(HashMap.class);
        param.remove("page");
        param.put("start", start);
        HashMap map = workflowService.searchTaskByPage(param);
        return R.ok().put("page", map);
    }

    @PostMapping("/searchApprovalContent")
    public R searchApprovalContent(@Valid @RequestBody SearchApprovalContentForm form) {

        HashMap map = workflowService.searchApprovalContent(form.getInstanceId(), form.getUserId(), form.getRole(), form.getType(), form.getStatus());
        return R.ok().put("content", map);
    }

    @PostMapping("/searchApprovalBpmn")
    public void searchApprovalBpmn(@Valid @RequestBody SearchApprovalBpmnForm form, HttpServletResponse response) {

        response.setContentType("image/jpg");
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processInstanceId(form.getInstanceId()).singleResult();
        BpmnModel bpmnModel;
        List activeActivityIds;
        if (task != null) {
            //流程定义
            bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            activeActivityIds = runtimeService.getActiveActivityIds(task.getExecutionId());
        } else {
            HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(form.getInstanceId()).list().get(0);
            bpmnModel = repositoryService.getBpmnModel(taskInstance.getProcessDefinitionId());
            activeActivityIds = new ArrayList<>();
        }

        Map map = bpmnModel.getItemDefinitions();
        DefaultProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        //绘制bpmnModel代表的流程的流程图
        String os = System.getProperty("os.name").toLowerCase();
        String font = "SimSun";
        if (os.startsWith("win")) {
            font = "宋体";
        }
        try (InputStream in = diagramGenerator.generateDiagram(bpmnModel, "jpg", activeActivityIds, activeActivityIds,
                font, font, font,
                processEngine.getProcessEngineConfiguration().getProcessEngineConfiguration().getClassLoader(), 1.0);
             BufferedInputStream bin = new BufferedInputStream(in);
             OutputStream out = response.getOutputStream();
             BufferedOutputStream bout = new BufferedOutputStream(out);
        ) {
            IOUtils.copy(bin, bout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把任务转交其他人处理（同部门同职务的id排序第一人）
     *
     * @return
     */
    @PostMapping("/turnTask")
    public R turnTask(@Valid @RequestBody TuneTaskForm form) {

        workflowService.turnTask(form.getUserId(), form.getAssigneeId());
        return R.ok();
    }


}