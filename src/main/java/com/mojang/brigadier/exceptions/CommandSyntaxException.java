// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package com.mojang.brigadier.exceptions;

import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.util.ComponentMessageThrowable;

import java.util.function.Function;

public class CommandSyntaxException extends Exception implements ComponentMessageThrowable { // PaperMC - adventure component messages
    public static final int CONTEXT_AMOUNT = 10;
    public static boolean ENABLE_COMMAND_STACK_TRACES = true;
    public static BuiltInExceptionProvider BUILT_IN_EXCEPTIONS = new BuiltInExceptions();
    // PaperMC start - adventure component messages
    /**
     * Converts a {@link Message} to an adventure {@link Component} for {@link #componentMessage()}.
     */
    public static Function<Message, Component> MESSAGE_TO_COMPONENT = message ->
        message instanceof ComponentLike ? ((ComponentLike) message).asComponent() : Component.text(message.getString());
    // PaperMC end - adventure component messages

    private final CommandExceptionType type;
    private final Message message;
    private final String input;
    private final int cursor;

    public CommandSyntaxException(final CommandExceptionType type, final Message message) {
        super(message.getString(), null, ENABLE_COMMAND_STACK_TRACES, ENABLE_COMMAND_STACK_TRACES);
        this.type = type;
        this.message = message;
        this.input = null;
        this.cursor = -1;
    }

    public CommandSyntaxException(final CommandExceptionType type, final Message message, final String input, final int cursor) {
        super(message.getString(), null, ENABLE_COMMAND_STACK_TRACES, ENABLE_COMMAND_STACK_TRACES);
        this.type = type;
        this.message = message;
        this.input = input;
        this.cursor = cursor;
    }

    @Override
    public String getMessage() {
        String message = this.message.getString();
        final String context = getContext();
        if (context != null) {
            message += " at position " + cursor + ": " + context;
        }
        return message;
    }

    public Message getRawMessage() {
        return message;
    }

    public String getContext() {
        if (input == null || cursor < 0) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        final int cursor = Math.min(input.length(), this.cursor);

        if (cursor > CONTEXT_AMOUNT) {
            builder.append("...");
        }

        builder.append(input.substring(Math.max(0, cursor - CONTEXT_AMOUNT), cursor));
        builder.append("<--[HERE]");

        return builder.toString();
    }

    public CommandExceptionType getType() {
        return type;
    }

    public String getInput() {
        return input;
    }

    public int getCursor() {
        return cursor;
    }

    // PaperMC start - non-recoverable exceptions abort parsing
    /**
     * Whether the dispatcher may recover from this exception by trying sibling nodes.
     *
     * <p>Subclasses can return {@code false} for errors that make the whole input invalid, no matter which node parses
     * it (for example an input that is too deeply nested to be safe). The dispatcher then stops parsing immediately
     * and reports this exception in {@link com.mojang.brigadier.ParseResults#getExceptions()}.</p>
     *
     * @return {@code true} (the default) if parsing may continue with other nodes
     */
    public boolean isRecoverable() {
        return true;
    }
    // PaperMC end - non-recoverable exceptions abort parsing

    // PaperMC start - adventure component messages
    /**
     * Gets the raw message of this exception as an adventure component, see {@link #MESSAGE_TO_COMPONENT}.
     * Unlike {@link #getMessage()}, this does not include the input context.
     *
     * @return the message as a component
     */
    @Override
    public Component componentMessage() {
        return MESSAGE_TO_COMPONENT.apply(this.message);
    }
    // PaperMC end - adventure component messages
}
