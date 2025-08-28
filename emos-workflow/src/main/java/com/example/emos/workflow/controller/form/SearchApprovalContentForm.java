package com.example.emos.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class SearchApprovalContentForm {
    @NotBlank(message = "instanceId不能为空")
    @Pattern(regexp = "^[0-9A-Za-z\\-]{36}$", message = "instanceId内容不正确")
    private String instanceId;

    @NotNull(message = "userId不能为空")
    @Min(value = 1, message = "userId不能小于1")
    private Integer userId;

    @NotEmpty(message = "role不能为空")
    private String[] role;

    @NotBlank(message = "type不能为空")
    @Pattern(regexp = "^员工请假$|^会议申请$|^报销申请$", message = "type内容不正确")
    private String type;

    @NotBlank(message = "status不能为空")
    @Pattern(regexp = "^待审批$|^已审批$|^已结束$", message = "status内容不正确")
    private String status;

    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "tcode不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "tcode必须是6位数字")
    private String tcode;
}
