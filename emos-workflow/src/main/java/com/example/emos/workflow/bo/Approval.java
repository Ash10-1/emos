package com.example.emos.workflow.bo;

import lombok.Data;

@Data
public class Approval {
    private String processId;
    private String taskId;
    private String title;
    private String type;
    private String creatorName;
    private String createDate;
    private String status;
    private String result;
    private boolean filing;
}
