package com.example.emos.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ArchiveTaskForm {
    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "taskId不能为空")
    private String taskId;

    @NotNull(message = "userId不能为空")
    @Min(value = 1, message = "userId不能小于1")
    private Integer userId;

    @NotBlank(message = "files不能为空")
    private String files;

    @NotBlank(message = "tcode不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "tcode必须是6位数字")
    private String tcode;
}
