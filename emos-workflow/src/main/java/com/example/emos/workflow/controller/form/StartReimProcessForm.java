package com.example.emos.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class StartReimProcessForm {
    @NotNull(message = "creatorId不能为空")
    @Min(value = 1, message = "creatorId不能小于1")
    private Integer creatorId;

    @NotBlank(message = "creatorName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$", message = "creatorName内容不正确")
    private String creatorName;

    @NotBlank(message = "title不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,30}$", message = "title内容不正确")
    private String title;

    @NotNull(message = "gmId不能为空")
    @Min(value = 1, message = "gmId不能小于1")
    private Integer gmId;

    @NotNull(message = "managerId不能为空")
    @Min(value = 1, message = "managerId不能小于1")
    private Integer managerId;

    @NotBlank(message = "url不能为空")
    private String url;

    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "tcode不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "tcode必须是6位数字")
    private String tcode;
}
