package com.example.emos.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SearchProcessUsersForm {
    @NotBlank(message = "instanceId不能为空")
    private String instanceId;

    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "tcode不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "tcode必须是6位数字")
    private String tcode;
}
