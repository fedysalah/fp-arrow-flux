package org.fsalah.validation

import arrow.*
import arrow.core.*
import arrow.core.extensions.EitherApplicativeError
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicativeError.applicativeError
import arrow.typeclasses.*

sealed class ValidationError(val msg: String) {
    data class DoesNotContain(val value: String) : ValidationError("Did not contain $value")
    data class MaxLength(val value: Int) : ValidationError("Exceeded length of $value")
    data class NotAnEmail(val reasons: Nel<ValidationError>) : ValidationError("Not a valid email")
}

data class FormField(val value: String)
data class Email(val value: String)


sealed class Rules<F>(A: ApplicativeError<F, Nel<ValidationError>>) : ApplicativeError<F, Nel<ValidationError>> by A {

    private fun FormField.contains(needle: String): Kind<F, FormField> =
            if (value.contains(needle, false)) just(this)
            else raiseError(ValidationError.DoesNotContain(needle).nel())

    private fun FormField.maxLength(maxLength: Int): Kind<F, FormField> =
            if (value.length <= maxLength) just(this)
            else raiseError(ValidationError.MaxLength(maxLength).nel())

    fun FormField.validate(): Kind<F, Email> =
            mapN(contains("@"), maxLength(250)) {
                Email(value)
            }.handleErrorWith { raiseError(ValidationError.NotAnEmail(it).nel()) }

    object ErrorAccumulationStrategy : Rules<ValidatedPartialOf<Nel<ValidationError>>>(Validated.applicativeError(NonEmptyList.semigroup()))

    object FailFastStrategy : Rules<EitherPartialOf<Nel<ValidationError>>>(Either.applicativeError())

    companion object {
        infix fun <A> failFast(f: FailFastStrategy.() -> A): A = f(FailFastStrategy)
        infix fun <A> accumulateErrors(f: ErrorAccumulationStrategy.() -> A): A = f(ErrorAccumulationStrategy)
    }
}

abstract class Read<A> {
    abstract fun read(s: String): Option<A>
    companion object {
        val stringRead: Read<String> =
                object: Read<String>() {
                    override fun read(s: String): Option<String> = Option(s)
                }
        val intRead: Read<Int> =
                object: Read<Int>() {
                    override fun read(s: String): Option<Int> =
                            if (s.matches(Regex("-?[0-9]+"))) Option(s.toInt()) else None
                }
    }
}

data class ConnectionParams(val url: String, val port: Int)

sealed class ConfigError {
    data class MissingConfig(val field: String): ConfigError()
    data class ParseConfig(val field: String): ConfigError()
}


data class Config(val map: Map<String, String>) {

    fun positive(field: String, i: Int): Either<ConfigError, Int> {
        return if (i >= 0) i.right()
        else ConfigError.ParseConfig(field).left()
    }

    fun <A> parse(read: Read<A>, key: String): Validated<ConfigError, A> {
        return when (val v = Option.fromNullable(map[key])) {
            is Some -> when (val s = read.read(v.t)) {
                    is Some -> s.t.valid()
                    is None -> ConfigError.ParseConfig(key).invalid()
                }
            is None -> Validated.Invalid(ConfigError.MissingConfig(key))
        }
    }
}

fun <E, A, B, C> parallelValidate(v1: Validated<E, A>, v2: Validated<E, B>, f: (A, B) -> C): Validated<NonEmptyList<E>, C> {
    return when {
        v1 is Validated.Valid && v2 is Validated.Valid -> Validated.Valid(f(v1.a, v2.a))
        v1 is Validated.Valid && v2 is Validated.Invalid -> v2.toValidatedNel()
        v1 is Validated.Invalid && v2 is Validated.Valid -> v1.toValidatedNel()
        v1 is Validated.Invalid && v2 is Validated.Invalid -> Validated.Invalid(NonEmptyList(v1.e, listOf(v2.e)))
        else -> throw IllegalStateException("Not possible value")
    }
}
