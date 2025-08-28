package com.example.emos.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class StopProcessForm {
    @NotBlank(message = "instanceId不能为空")
    private String instanceId;

    @NotBlank(message = "code不能为空")
    private String code;
}
