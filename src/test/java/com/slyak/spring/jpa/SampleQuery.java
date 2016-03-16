package com.slyak.spring.jpa;

/**
 * .
 * <p>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 16/3/15.
 */
@TemplateQueryObject
public class SampleQuery {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content != null) {
            this.content = "%" + content + "%";
        }
    }
}
