package org.ironrhino.core.throttle;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.core.aop.BaseAspect;
import org.ironrhino.core.util.ExpressionUtils;
import org.ironrhino.core.util.IllegalConcurrentAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ConcurrencyAspect extends BaseAspect {

	@Autowired
	private ConcurrencyService concurrencyService;

	public ConcurrencyAspect() {
		order = -1000;
	}

	@Around("execution(public * *(..)) and @annotation(concurrency)")
	public Object control(ProceedingJoinPoint jp, Concurrency concurrency) throws Throwable {
		Map<String, Object> context = buildContext(jp);
		String key = concurrency.key();
		if (!key.isEmpty()) {
			key = ExpressionUtils.evalString(key, context);
		} else {
			key = jp.getSignature().toLongString();
		}
		int permits = ExpressionUtils.evalInt(concurrency.permits(), context, 0);
		if (!concurrency.block()) {
			if (concurrencyService.tryAcquire(key, permits, concurrency.timeout(), concurrency.timeUnit())) {
				try {
					return jp.proceed();
				} finally {
					concurrencyService.release(key);
				}
			} else {
				throw new IllegalConcurrentAccessException(key);
			}
		} else {
			concurrencyService.acquire(key, permits);
			try {
				return jp.proceed();
			} finally {
				concurrencyService.release(key);
			}
		}

	}

}
