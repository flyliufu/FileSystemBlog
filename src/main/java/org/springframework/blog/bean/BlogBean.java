package org.springframework.blog.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by liufu on 2016/12/9.
 */
public class BlogBean implements Serializable {

    private String title;
    private String block;
    private String content;
    private List<BlogBean> children;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<BlogBean> getChildren() {
        return children;
    }

    public void setChildren(List<BlogBean> children) {
        this.children = children;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }
}
