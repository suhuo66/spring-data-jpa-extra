package com.slyak.spring.jpa;

import org.springframework.data.jpa.repository.query.*;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Method;

/**
 * .
 *
 * @author stormning on 16/6/5.
 */
public class AbstractTemplateBasedJpaQuery extends AbstractJpaQuery {

	private JpaQueryMethod method;

	private QueryTemplate query;

	private QueryTemplate countQuery;

	/**
	 * Creates a new {@link AbstractJpaQuery} from the given {@link JpaQueryMethod}.
	 *
	 * @param method jpa query method
	 * @param em entity manager
	 * @param context query template context
	 */
	public AbstractTemplateBasedJpaQuery(JpaQueryMethod method, EntityManager em, QueryTemplateContext context) {
		super(method, em);
		this.method = method;
		this.query = context.lookup(getQueryMethod().getNamedQueryName());
		String cq = executeMethod("getCountQuery");
		this.countQuery = cq == null ? null : context.lookup(cq);
	}

	@Override
	protected Query doCreateQuery(Object[] values) {
		ParameterAccessor accessor = new ParametersParameterAccessor(getQueryMethod().getParameters(), values);
		String sortedQueryString = QueryUtils.applySorting(query.getQueryString(), accessor.getSort(),
				QueryUtils.detectAlias(query.getQueryString()));

		Query query = createJpaQuery(sortedQueryString);

		return createBinder(values).bindAndPrepare(query);
	}

	@Override
	protected Query doCreateCountQuery(Object[] values) {
		String queryString = countQuery.getQueryString();
		EntityManager em = getEntityManager();
		boolean isNativeQuery = executeMethod("nativeQuery");
		return createBinder(values)
				.bind(isNativeQuery ? em.createNativeQuery(queryString) : em.createQuery(queryString, Long.class));
	}

	public Query createJpaQuery(String queryString) {
		return getEntityManager().createQuery(queryString);
	}

	@Override
	protected ParameterBinder createBinder(Object[] values) {
		return new TemplateQueryParameterBinder(getQueryMethod().getParameters(), values);
	}

	@SuppressWarnings("unchecked")
	private <T> T executeMethod(String methodName) {
		Method countQueryMethod = ReflectionUtils.findMethod(JpaQueryMethod.class, methodName);
		ReflectionUtils.makeAccessible(countQueryMethod);
		return (T) ReflectionUtils.invokeMethod(countQueryMethod, method);
	}

	private static class TemplateQueryParameterBinder extends ParameterBinder {

		private Object[] values;

		private JpaParameters parameters;

		/**
		 * Creates a new {@link ParameterBinder}.
		 *
		 * @param parameters must not be {@literal null}.
		 * @param values     must not be {@literal null}.
		 */
		public TemplateQueryParameterBinder(JpaParameters parameters, Object[] values) {
			super(parameters, values);
			this.values = values;
			this.parameters = parameters;
		}

	}
}
