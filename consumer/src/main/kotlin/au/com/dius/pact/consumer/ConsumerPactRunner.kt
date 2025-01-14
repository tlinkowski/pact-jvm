package au.com.dius.pact.consumer

import au.com.dius.pact.consumer.model.MockProviderConfig
import au.com.dius.pact.core.model.BasePact
import au.com.dius.pact.core.model.Interaction
import au.com.dius.pact.core.model.InvalidPactException
import au.com.dius.pact.core.model.Pact
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.support.V4PactFeaturesException

interface PactTestRun<R> {
  @Throws(Throwable::class)
  fun run(mockServer: MockServer, context: PactTestExecutionContext?): R
}

fun <R> runConsumerTest(pact: BasePact, config: MockProviderConfig, test: PactTestRun<R>): PactVerificationResult {
  val errors = pact.validateForVersion(config.pactVersion)
  if (errors.isNotEmpty()) {
    return PactVerificationResult.Error(
      V4PactFeaturesException("Pact specification V4 features can not be used with version " +
        "${config.pactVersion} - ${errors.joinToString(", ")}"), PactVerificationResult.Ok())
  }

  if (!pact.isRequestResponsePact()) {
    throw InvalidPactException("Expected an HTTP Request/Response Pact")
  }
  val server = mockServer(pact, config)
  return server.runAndWritePact(pact, config.pactVersion, test)
}

interface MessagePactTestRun<R> {
  @Throws(Throwable::class)
  fun run(messages: List<Interaction>, context: PactTestExecutionContext?): R
}

fun <R> runMessageConsumerTest(
  pact: Pact,
  pactVersion: PactSpecVersion = PactSpecVersion.V3,
  testFunc: MessagePactTestRun<R>
): PactVerificationResult {
  val errors = pact.validateForVersion(pactVersion)
  if (errors.isNotEmpty()) {
    return PactVerificationResult.Error(
      V4PactFeaturesException("Pact specification V4 features can not be used with version " +
        "$pactVersion - ${errors.joinToString(", ")}"), PactVerificationResult.Ok())
  }

  return try {
    val context = PactTestExecutionContext()
    val messagePact = pact.asMessagePact().expect { "Expected a message Pact" }
    val result = testFunc.run(messagePact.messages, context)
    pact.write(context.pactFolder, pactVersion).expect { "Failed to write the Pact" }
    PactVerificationResult.Ok(result)
  } catch (e: Throwable) {
    PactVerificationResult.Error(e, PactVerificationResult.Ok())
  }
}
