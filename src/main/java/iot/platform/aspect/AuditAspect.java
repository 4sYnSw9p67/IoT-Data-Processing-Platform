package iot.platform.aspect;

import iot.platform.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Around("@within(iot.platform.aspect.Auditable) || @annotation(iot.platform.aspect.Auditable)")
    public Object aroundAuditable(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Auditable annotation = method.getAnnotation(Auditable.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(Auditable.class);
        }
        String functionality = annotation == null || annotation.value().isBlank()
                ? method.getDeclaringClass().getSimpleName() + "#" + method.getName()
                : annotation.value();
        Optional<UUID> userId = SecurityUtils.currentUserIdOptional();
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("AUDIT functionality='{}' user={} status=OK durationMs={}",
                    functionality, userId.map(UUID::toString).orElse("anonymous"), elapsed);
            return result;
        } catch (Throwable throwable) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("AUDIT functionality='{}' user={} status=FAIL durationMs={} cause={}",
                    functionality, userId.map(UUID::toString).orElse("anonymous"),
                    elapsed, throwable.getClass().getSimpleName());
            throw throwable;
        }
    }
}
