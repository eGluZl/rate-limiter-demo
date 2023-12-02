package moe.egluzl.rl.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Limit {

    String key() default "default-key";

    double permitsPerSecond ();

    long timeout();

    TimeUnit timeUnit() default TimeUnit.MICROSECONDS;

    String msg() default "system busy, please try again later";

}
