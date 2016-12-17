package com.slyak.spring.jpa;

import org.springframework.core.io.Resource;

import java.util.Iterator;

/**
 * .
 *
 * @author stormning on 2016/12/17.
 */
public class FsqlNamedTemplateResolver implements NamedTemplateResolver {
	@Override
	public Iterator<Void> doInTemplateResource(Resource resource, NamedTemplateCallback callback)
			throws Exception {
		return null;
	}
}
