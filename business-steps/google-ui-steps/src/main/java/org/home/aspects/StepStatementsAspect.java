package org.home.aspects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.home.pages.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

@Aspect
public class StepStatementsAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger("ACTION");

    @Pointcut("withincode(public static void org.home.steps*.*Steps.*(..))")
    void anyStatementInStep() {
    }

    @Before("anyStatementInStep()")
    public void beforeAnyStatement(JoinPoint joinPoint) {
        if (joinPoint.getKind().equals(JoinPoint.METHOD_CALL)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getName();
            String message = getPrefix(signature) + methodName + " " + getParameters(signature, joinPoint.getArgs());
            LOGGER.info(message);
        }
    }

    private String getPrefix(MethodSignature signature) {
        String result = "";
        if (signature.getDeclaringType().getName().startsWith("org.home.pages.")) {
            if (signature.getReturnType().equals(SelenideElement.class)) {
                result = "get element " + signature.getDeclaringType().getSimpleName() + ".";
            } else if (signature.getReturnType().equals(ElementsCollection.class)) {
                result = "get elements collection " + signature.getDeclaringType().getSimpleName() + ".";
            }
        }
        return result;
    }

    private String getParameters(MethodSignature signature, Object... args) {
        if (args.length == 0) {
            return "";
        }
        String[] parameterNames = signature.getParameterNames();
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < Math.min(parameterNames.length, args.length); i++) {
            params.put(parameterNames[i], String.valueOf(args[i]));
        }
        List<String> entries = params.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.toList());
        return "[" + StringUtils.join(entries, ", ") + "]";
    }

}