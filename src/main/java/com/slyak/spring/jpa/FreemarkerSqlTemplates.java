package com.slyak.spring.jpa;

import com.orange.flower.core.util.StringUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * .
 * <p>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/10.
 */
@Component
public class FreemarkerSqlTemplates implements ResourceLoaderAware {

    private static DocumentLoader documentLoader = new DefaultDocumentLoader();

    private static Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

    private static StringTemplateLoader sqlTemplateLoader = new StringTemplateLoader();
    private static ResourceLoader resourceLoader;
    private static Map<String, Long> lastModifiedCache = new ConcurrentHashMap<String, Long>();

    static {
        cfg.setTemplateLoader(sqlTemplateLoader);
    }

    public static String process(String entityName, String methodName, Map<String, Object> model) {
        reloadIfPossible(entityName);
        try {
            StringWriter writer = new StringWriter();
            cfg.getTemplate(getTemplateKey(entityName, methodName), "UTF-8").process(model, writer);
            return writer.toString();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    private static String getTemplateKey(String entityName, String methodName) {
        return entityName + ":" + methodName;
    }

    private static void reloadIfPossible(String entityName) {
        try {
            Long lastModified = lastModifiedCache.get(entityName);
            Resource resource = resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + File.separator + "sqls" + File.separator + entityName + ".xml");
            long newLastModified = resource.lastModified();
            if (lastModified == null || newLastModified > lastModified) {
                InputStream inputStream = resource.getInputStream();
                Document document = Jsoup.parse(IOUtils.toString(inputStream));
                Elements sqlEles = document.select("sql");
                for (Element sqlEle : sqlEles) {
                    sqlTemplateLoader.putTemplate(getTemplateKey(entityName, sqlEle.attr("name")), sqlEle.text());
                }
                lastModifiedCache.put(entityName, newLastModified);
            }
        } catch (Exception e) {
            //
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        FreemarkerSqlTemplates.resourceLoader = resourceLoader;
    }
}
