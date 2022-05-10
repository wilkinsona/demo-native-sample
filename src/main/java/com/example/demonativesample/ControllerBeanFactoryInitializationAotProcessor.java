package com.example.demonativesample;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;

public class ControllerBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

	@Override
	public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
		String[] controllerBeanNames = beanFactory.getBeanNamesForAnnotation(Controller.class);
		List<TypeReference> controllerTypes = Stream.of(controllerBeanNames)
				.map((name) -> ClassUtils
						.getUserClass(beanFactory.getMergedBeanDefinition(name).getResolvableType().toClass()))
				.map(TypeReference::of).collect(Collectors.toList());
		return (context, code) -> {
			ReflectionHints reflection = context.getRuntimeHints().reflection();
			reflection.registerTypes(controllerTypes,
					(hint) -> hint.withMembers(MemberCategory.INVOKE_DECLARED_METHODS));
		};
	}

}
