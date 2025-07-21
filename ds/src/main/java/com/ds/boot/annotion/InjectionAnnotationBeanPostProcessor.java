package com.ds.boot.annotion;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author ds
 * @date 2025/7/21
 * @description
 */

public class InjectionAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {

	private final Set<Class<? extends Annotation>> injectionAnnotationTypes = new LinkedHashSet<>(4);

	private final transient Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

	private BeanFactory beanFactory;

	public InjectionAnnotationBeanPostProcessor() {
		injectionAnnotationTypes.add(Injection.class);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
		InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
		try {
			metadata.inject(bean, beanName, pvs);
		} catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of resource dependencies failed", ex);
		}
		return pvs;
	}

	private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
		// Fall back to class name as cache key, for backwards compatibility with custom callers.
		String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
		// Quick check on the concurrent map first, with minimal locking.
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					if (metadata != null) {
						metadata.clear(pvs);
					}
					metadata = buildResourceMetadata(clazz);
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}

	private InjectionMetadata buildResourceMetadata(final Class<?> clazz) {
		if (!AnnotationUtils.isCandidateClass(clazz, injectionAnnotationTypes)) {
			return InjectionMetadata.EMPTY;
		}

		List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
		Class<?> targetClass = clazz;

		do {
			final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

			ReflectionUtils.doWithLocalFields(targetClass, field -> {
				if (field.isAnnotationPresent(Injection.class)) {
					if (Modifier.isStatic(field.getModifiers())) {
						throw new IllegalStateException("@Resource annotation is not supported on static fields");
					}

					currElements.add(new InjectionFieldElement(field, true));

				}
			});

//			ReflectionUtils.doWithLocalMethods(targetClass, method -> {
//				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
//				if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
//					return;
//				}
//				if (method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
//					if (bridgedMethod.isAnnotationPresent(Injection.class)) {
//						if (Modifier.isStatic(method.getModifiers())) {
//							throw new IllegalStateException("@Resource annotation is not supported on static methods");
//						}
//						Class<?>[] paramTypes = method.getParameterTypes();
//						if (paramTypes.length != 1) {
//							throw new IllegalStateException("@Resource annotation requires a single-arg method: " + method);
//						}
//
//						PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
//						currElements.add(new InjectionFieldElement(method, bridgedMethod, pd));
//
//					}
//				}
//			});

			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		return InjectionMetadata.forElements(elements, clazz);
	}

	private class InjectionFieldElement extends InjectionMetadata.InjectedElement {

		public InjectionFieldElement(Field field, boolean required) {
			super(field, null);
		}

		@Override
		protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			Object value;
			String requiredBeanName = field.getName();
			if (beanFactory.containsBean(requiredBeanName)) {
				 value = beanFactory.getBean(requiredBeanName);
			} else {
				value = beanFactory.getBean(field.getType());
			}

			ReflectionUtils.makeAccessible(field);
			field.set(bean, value);
		}
	}

}
