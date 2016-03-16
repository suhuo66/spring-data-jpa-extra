package com.slyak.spring.jpa;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.xml.DefaultDocumentLoader;
import org.springframework.beans.factory.xml.DocumentLoader;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.xml.DomUtils;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
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

    protected final Log logger = LogFactory.getLog(getClass());

    private static Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

    private static StringTemplateLoader sqlTemplateLoader = new StringTemplateLoader();

    private DocumentLoader documentLoader = new DefaultDocumentLoader();

    private ResourceLoader resourceLoader;

    private EntityResolver entityResolver;

    private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

    private Map<String, Long> lastModifiedCache = new ConcurrentHashMap<String, Long>();

    private String encoding = "UTF-8";

    @Value("${spring.jpa.template-location:classpath:/sqls}")
    private String templateLocation;

    static {
        cfg.setTemplateLoader(sqlTemplateLoader);
    }

    public String process(String entityName, String methodName, Map<String, Object> model) {
        reloadIfPossible(entityName);
        try {
            StringWriter writer = new StringWriter();
            cfg.getTemplate(getTemplateKey(entityName, methodName), encoding).process(model, writer);
            return writer.toString();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    private String getTemplateKey(String entityName, String methodName) {
        return entityName + ":" + methodName;
    }

    private void reloadIfPossible(String entityName) {
        try {
            Long lastModified = lastModifiedCache.get(entityName);
            Resource resource = resourceLoader.getResource(templateLocation + File.separator + entityName + ".xml");
            long newLastModified = resource.lastModified();
            if (lastModified == null || newLastModified > lastModified) {
                InputSource inputSource = new InputSource(resource.getInputStream());
                inputSource.setEncoding(encoding);
                Document doc = documentLoader.loadDocument(inputSource, entityResolver, errorHandler, XmlValidationModeDetector.VALIDATION_XSD, false);
                List<Element> sqes = DomUtils.getChildElementsByTagName(doc.getDocumentElement(), "sql");
                for (Element sqle : sqes) {
                    sqlTemplateLoader.putTemplate(getTemplateKey(entityName, sqle.getAttribute("name")), sqle.getTextContent());
                }
                lastModifiedCache.put(entityName, newLastModified);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.entityResolver = new ResourceEntityResolver(resourceLoader);
    }
}
