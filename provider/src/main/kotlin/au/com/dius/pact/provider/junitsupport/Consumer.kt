package au.com.dius.pact.provider.junitsupport

import java.lang.annotation.Inherited
import kotlin.annotation.Retention

/**
 * Used to pass consumer name to Pact runner. Can use expressions (in `${}` form) to get the value from Java system
 * properties or environment variables.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Inherited
annotation class Consumer(
  /**
   * @return consumer name for pact test running
   */
  val value: String
)
