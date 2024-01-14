package org.asamk.signal.util;

import org.asamk.signal.commands.exceptions.CommandException;
import org.asamk.signal.commands.exceptions.RateLimitErrorException;
import org.asamk.signal.commands.exceptions.UntrustedKeyErrorException;
import org.asamk.signal.commands.exceptions.UserErrorException;
import org.asamk.signal.dbus.errors.FailureException;
import org.asamk.signal.dbus.errors.IdentityUntrustedException;
import org.asamk.signal.json.JsonSendMessageResult;
import org.asamk.signal.manager.api.ProofRequiredException;
import org.asamk.signal.manager.api.RateLimitException;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.asamk.signal.manager.api.SendGroupMessageResults;
import org.asamk.signal.manager.api.SendMessageResult;
import org.asamk.signal.manager.api.SendMessageResults;
import org.asamk.signal.output.JsonWriter;
import org.asamk.signal.output.OutputWriter;
import org.asamk.signal.output.PlainTextWriter;
import org.freedesktop.dbus.exceptions.DBusExecutionException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SendMessageResultUtils {

    private SendMessageResultUtils() {
    }

    public static void outputResult(OutputWriter outputWriter, SendGroupMessageResults sendMessageResults) {
        switch (outputWriter) {
            case PlainTextWriter writer -> {
                var errors = getErrorMessagesFromSendMessageResults(sendMessageResults.results());
                printSendMessageResultErrors(writer, errors);
                writer.println("{}", sendMessageResults.timestamp());
            }
            case JsonWriter writer -> {
                var results = getJsonSendMessageResults(sendMessageResults.results());
                writer.write(Map.of("timestamp", sendMessageResults.timestamp(), "results", results));
            }
        }
    }

    public static List<String> getErrorMessagesFromSendMessageResults(Collection<SendMessageResult> results) {
        return results.stream()
                .map(SendMessageResultUtils::getErrorMessageFromSendMessageResult)
                .filter(Objects::nonNull)
                .toList();
    }

    public static void printSendMessageResultErrors(PlainTextWriter writer, List<String> errors) {
        if (errors.isEmpty()) {
            return;
        }
        writer.println("Failed to send (some) messages:");
        for (var error : errors) {
            writer.println(error);
        }
    }

    public static List<JsonSendMessageResult> getJsonSendMessageResults(Collection<SendMessageResult> results) {
        return results.stream().map(JsonSendMessageResult::from).toList();
    }

    private static String getErrorMessageFromSendMessageResult(SendMessageResult result) {
        var identifier = result.address().getLegacyIdentifier();
        if (result.proofRequiredFailure() != null) {
            var failure = result.proofRequiredFailure();
            return String.format(
                    "CAPTCHA proof required for sending to \"%s\", available options \"%s\" with challenge token \"%s\", or wait \"%d\" seconds.\n"
                            + (
                            failure.getOptions().contains(ProofRequiredException.Option.RECAPTCHA)
                                    ? """
                                      To get the captcha token, go to https://signalcaptchas.org/challenge/generate.html
                                      After solving the captcha, right-click on the "Open Signal" link and copy the link.
                                      Use the following command to submit the captcha token:
                                      signal-cli submitRateLimitChallenge --challenge CHALLENGE_TOKEN --captcha CAPTCHA_TOKEN"""
                                    : ""
                    ),
                    identifier,
                    failure.getOptions()
                            .stream()
                            .map(ProofRequiredException.Option::toString)
                            .collect(Collectors.joining(", ")),
                    failure.getToken(),
                    failure.getRetryAfterSeconds());
        } else if (result.isNetworkFailure()) {
            return String.format("Network failure for \"%s\"", identifier);
        } else if (result.isRateLimitFailure()) {
            return String.format("Rate limit failure for \"%s\"", identifier);
        } else if (result.isUnregisteredFailure()) {
            return String.format("Unregistered user \"%s\"", identifier);
        } else if (result.isIdentityFailure()) {
            return String.format("Untrusted DbusPropertyIdentity for \"%s\"", identifier);
        }
        return null;
    }

    public static void outputResult(
            OutputWriter outputWriter, SendMessageResults sendMessageResults
    ) throws CommandException {
        switch (outputWriter) {
            case PlainTextWriter writer -> {
                var errors = getErrorMessagesFromSendMessageResults(sendMessageResults.results());
                printSendMessageResultErrors(writer, errors);
                writer.println("{}", sendMessageResults.timestamp());
            }
            case JsonWriter writer -> {
                var results = getJsonSendMessageResults(sendMessageResults.results());
                writer.write(Map.of("timestamp", sendMessageResults.timestamp(), "results", results));
            }
        }
        if (!sendMessageResults.hasSuccess()) {
            if (sendMessageResults.hasOnlyUntrustedIdentity()) {
                throw new UntrustedKeyErrorException("Failed to send message due to untrusted identities");
            } else if (sendMessageResults.hasOnlyRateLimitFailure()) {
                throw new RateLimitErrorException("Failed to send message due to rate limiting",
                        new RateLimitException(0));
            } else {
                throw new UserErrorException("Failed to send message");
            }
        }
    }

    private static List<String> getErrorMessagesFromSendMessageResults(Map<RecipientIdentifier, List<SendMessageResult>> mapResults) {
        return mapResults.entrySet()
                .stream()
                .flatMap(entry -> entry.getValue()
                        .stream()
                        .map(SendMessageResultUtils::getErrorMessageFromSendMessageResult)
                        .filter(Objects::nonNull)
                        .map(error -> entry.getKey().getIdentifier() + ": " + error))
                .toList();
    }

    private static List<JsonSendMessageResult> getJsonSendMessageResults(Map<RecipientIdentifier, List<SendMessageResult>> mapResults) {
        return mapResults.entrySet().stream().flatMap(entry -> {
            var groupId = entry.getKey() instanceof RecipientIdentifier.Group g ? g.groupId() : null;
            return entry.getValue().stream().map(r -> JsonSendMessageResult.from(r, groupId));
        }).toList();
    }

    public static void checkGroupSendMessageResults(
            long timestamp, Collection<SendMessageResult> results
    ) throws DBusExecutionException {
        if (results.size() == 1) {
            checkSendMessageResult(timestamp, results.stream().findFirst().get());
            return;
        }

        var errors = getErrorMessagesFromSendMessageResults(results);
        if (errors.isEmpty() || errors.size() < results.size()) {
            return;
        }

        var message = new StringBuilder();
        message.append("Failed to send message:\n");
        for (var error : errors) {
            message.append(error).append('\n');
        }
        message.append(timestamp);

        throw new FailureException(message.toString());
    }

    private static void checkSendMessageResult(long timestamp, SendMessageResult result) throws DBusExecutionException {
        var error = getErrorMessageFromSendMessageResult(result);

        if (error == null) {
            return;
        }

        var message = "\nFailed to send message:\n" + error + '\n' + timestamp;

        if (result.isIdentityFailure()) {
            throw new IdentityUntrustedException(message);
        } else {
            throw new FailureException(message);
        }
    }

    public static void checkSendMessageResults(SendMessageResults results) {
        var sendMessageResults = results.results().values().stream().findFirst();
        if (results.results().size() == 1 && sendMessageResults.get().size() == 1) {
            checkSendMessageResult(results.timestamp(), sendMessageResults.get().stream().findFirst().get());
            return;
        }

        if (results.hasSuccess()) {
            return;
        }

        var message = new StringBuilder();
        message.append("Failed to send messages:\n");
        var errors = getErrorMessagesFromSendMessageResults(results.results());
        for (var error : errors) {
            message.append(error).append('\n');
        }
        message.append(results.timestamp());

        throw new FailureException(message.toString());
    }

}
