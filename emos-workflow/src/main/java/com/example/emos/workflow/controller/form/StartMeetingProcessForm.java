package com.example.emos.workflow.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class StartMeetingProcessForm {
    @NotBlank(message = "uuid不能为空")
    private String uuid;

    @NotNull(message = "creatorId不能为空")
    @Min(value = 1, message = "creatorId不能小于1")
    private Integer creatorId;

    @NotBlank(message = "creatorName不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$", message = "creatorName内容不正确")
    private String creatorName;

    @NotBlank(message = "title不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,30}$", message = "title内容不正确")
    private String title;

    @Min(value = 1, message = "gmId不能小于1")
    private Integer gmId;

    @Min(value = 1, message = "managerId不能小于1")
    private Integer managerId;

    @NotBlank(message = "url不能为空")
    private String url;

    private Boolean sameDept;

    @NotBlank(message = "code不能为空")
    private String code;

    @NotNull(message = "date不能为空")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-))$", message = "date内容不正确")
    private String date;

    @NotNull(message = "start不能为空")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$", message = "start内容不正确")
    private String start;

    @NotBlank(message = "meetingType不能为空")
    @Pattern(regexp = "^线上会议$|^线下会议$", message = "meetingType内容不正确")
    private String meetingType;

    @NotBlank(message = "tcode不能为空")
    @Pattern(regexp = "^[0-9]{6}$",message = "tcode必须是6位数字")
    private String tcode;

}

