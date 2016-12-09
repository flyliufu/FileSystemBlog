package org.springframework.blog.bean;

import org.springframework.blog.util.A;

import java.io.Serializable;

/**
 * Created by liufu on 2016/12/9.
 */
public class ResponseBean<T> implements Serializable {

    private String code = A.code.SUCCESS;
    private String msg;
    private T content;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
