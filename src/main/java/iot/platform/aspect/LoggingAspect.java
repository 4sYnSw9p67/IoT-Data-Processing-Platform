package iot.platform.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 500;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void anyRestController() {
    }

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void anyServiceClass() {
    }

    @Around("anyRestController() || anyServiceClass()")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed >= SLOW_METHOD_THRESHOLD_MS) {
                MethodSignature signature = (MethodSignature) pjp.getSignature();
                log.warn("Slow execution detected method={}#{} durationMs={}",
                        signature.getDeclaringType().getSimpleName(), signature.getName(), elapsed);
            }
        }
    }
}
