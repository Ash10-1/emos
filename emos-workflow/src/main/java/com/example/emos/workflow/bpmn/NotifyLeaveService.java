package com.example.emos.workflow.bpmn;

import cn.hutool.core.map.MapUtil;
import com.example.emos.workflow.db.dao.TbLeaveDao;
import com.example.emos.workflow.db.dao.TbUserDao;
import com.example.emos.workflow.exception.EmosException;
import com.example.emos.workflow.task.EmailTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class NotifyLeaveService implements JavaDelegate {
    @Autowired
    private HistoryService historyService;

    @Autowired
    private TbLeaveDao leaveDao;

    @Autowired
    private TbUserDao userDao;

    @Autowired
    private EmailTask emailTask;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        //查找该任务流中最后一个人的审批任务
        HistoricTaskInstance taskInstance = historyService.createHistoricTaskInstanceQuery().includeProcessVariables()
                .includeTaskLocalVariables().processInstanceId(delegateExecution.getProcessInstanceId())
                .orderByHistoricTaskInstanceEndTime().orderByTaskCreateTime().desc().list().get(0);
        //获取最后的审批人的审批结果
        String result = taskInstance.getTaskLocalVariables().get("result").toString();
        delegateExecution.setVariable("result", result);
        delegateExecution.setVariable("filing",true); //可以归档
        String instanceId = delegateExecution.getProcessInstanceId();
        //修改请假状态
        HashMap param = new HashMap() {{
            put("status", "同意".equals(result) ? 3 : 2);
            put("instanceId", instanceId);
        }};

        int rows = leaveDao.updateLeaveStatus(param);
        if (rows != 1) {
            throw new EmosException("更新请假记录状态失败");
        }
        //给员工发邮件，并且抄送给所在部门经理和所有HR
        int creatorId = delegateExecution.getVariable("creatorId", Integer.class);
        List<String> list_1 = userDao.searchEmailByIds(new int[]{creatorId});

        int managerId = delegateExecution.getVariable("managerId", Integer.class);
        ArrayList<String> list_2 = userDao.searchEmailByIds(new int[]{managerId});
        list_2.addAll(userDao.searchEmailByRoles(new String[]{"HR"}));

        SimpleMailMessage email = new SimpleMailMessage();
        String title = delegateExecution.getVariable("title", String.class);
        email.setSubject(title + "已经被批准");
        String creatorName = delegateExecution.getVariable("creatorName", String.class);
        HashMap map = leaveDao.searchLeaveByInstanceId(instanceId);
        String start = MapUtil.getStr(map, "start");
        String end = MapUtil.getStr(map, "end");
        email.setText("员工" + creatorName + "，于" + start + "至" + end + "的请假申请已经被批准，请及时把请假单签字交给HR归档！");
        email.setTo(list_1.toArray(new String[list_1.size()])); //发送员工本人
        email.setCc(list_2.toArray(new String[list_2.size()])); //抄送给部门经理和HR
        emailTask.sendAsync(email); //异步发送邮件
    }
}
