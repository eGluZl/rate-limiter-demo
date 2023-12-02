package moe.egluzl.rl.aop;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import moe.egluzl.rl.annotation.Limit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@Aspect
@Component
public class LimitAspect {


    private static final Map<String, RateLimiter> LIMITER_MAP = Maps.newConcurrentMap();

    @Around("@annotation(moe.egluzl.rl.annotation.Limit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        var limit = method.getAnnotation(Limit.class);
        if (limit == null) {
            //没有注解
            return joinPoint.proceed();
        }
        var key = limit.key();
        RateLimiter limiter;
        if (!LIMITER_MAP.containsKey(key)) {
            limiter = RateLimiter.create(limit.permitsPerSecond());
            LIMITER_MAP.put(key, limiter);
            log.info(">>> 创建了新的令牌桶：{}，容量：{}", key, limit.permitsPerSecond());
        } else {
            limiter = LIMITER_MAP.get(key);
        }
        var acquire = limiter.tryAcquire(limit.timeout(), limit.timeUnit());
        if (!acquire) {
            log.error(">>> 令牌桶：{}获取不到令牌", key);
            responseFail(limit.msg());
            return null;
        }
        return joinPoint.proceed();
    }

    /**
     * 直接向前端抛出异常
     * @param msg 提示信息
     */
    private void responseFail(String msg)  {
        var response=((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        try {
            // 设置字符编码
            response.setContentType(APPLICATION_JSON_UTF8_VALUE);
            response.getWriter().write(msg);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
