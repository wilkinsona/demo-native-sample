package com.example.demonativesample;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.core.annotation.SynthesizedAnnotation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;

class WebRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		registerReflectionHints(hints);
		registerProxyHints(hints);
	}

	private void registerReflectionHints(RuntimeHints hints) {
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] converterResources = resolver
					.getResources("classpath:org/springframework/web/bind/annotation/*.class");
			List<TypeReference> annotations = Stream.of(converterResources).map(this::className).map(this::load).filter(Class::isAnnotation).map(TypeReference::of)
					.collect(Collectors.toList());
			annotations.add(TypeReference.of(Component.class));
			annotations.add(TypeReference.of(Controller.class));
			hints.reflection().registerTypes(annotations, (hint) -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private String className(Resource resource) {
		String path = ((ClassPathResource)resource).getPath();
		path = path.substring(0, path.length() - ".class".length());
		return ClassUtils.convertResourcePathToClassName(path);
	}
	
	private Class<?> load(String className) {
		try {
			return ClassUtils.forName(className, getClass().getClassLoader());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void registerProxyHints(RuntimeHints hints) {
		hints.proxies().registerJdkProxy(RequestMapping.class, SynthesizedAnnotation.class);
	}

}
