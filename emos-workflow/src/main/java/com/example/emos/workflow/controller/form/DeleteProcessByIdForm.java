package com.example.emos.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class DeleteProcessByIdForm {
    @NotBlank(message = "instanceId不能为空")
    private String instanceId;

    @NotBlank(message = "type不能为空")
    private String type;

    @NotBlank(message = "reason不能为空")
    private String reason;

    @NotBlank(message = "code不能为空")
    private String code;

    private String uuid;

    @NotBlank(message = "tcode不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "tcode必须是6位数字")
    private String tcode;
}
