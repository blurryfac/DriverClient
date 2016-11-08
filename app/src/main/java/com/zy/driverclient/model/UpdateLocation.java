package com.zy.driverclient.model;

import java.util.List;

/**
 * Created by blurryFace on 2016/8/24.
 */
public class UpdateLocation {
    private String msg;
    private String code;
    private List<String> content;

    @Override
    public String toString() {
        return "UpdateLocation{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", content=" + content +
                '}';
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
