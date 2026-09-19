// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package com.mojang.brigadier.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public abstract class ArgumentBuilder<S, T extends ArgumentBuilder<S, T>> {
    // PaperMC start - default requirement singletons
    private static final Predicate<Object> DEFAULT_REQUIREMENT = s -> true;
    private static final BiPredicate<Object, ImmutableStringReader> DEFAULT_CONTEXT_REQUIREMENT = (context, reader) -> true;

    /**
     * The requirement a node has when none was set with {@link #requires(Predicate)}: it always passes.
     *
     * <p>This is a singleton, so platforms can use an identity check against {@link CommandNode#getRequirement()}
     * to find out whether a node ever had a requirement configured.</p>
     *
     * @param <S> the source type
     * @return the default requirement
     */
    @SuppressWarnings("unchecked")
    public static <S> Predicate<S> defaultRequirement() {
        return (Predicate<S>) DEFAULT_REQUIREMENT;
    }

    /**
     * The context requirement a node has when none was set with {@link #requiresWithContext(BiPredicate)}: it always passes.
     *
     * @param <S> the source type
     * @return the default context requirement
     * @see #defaultRequirement()
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S> BiPredicate<CommandContextBuilder<S>, ImmutableStringReader> defaultContextRequirement() {
        return (BiPredicate) DEFAULT_CONTEXT_REQUIREMENT;
    }
    // PaperMC end - default requirement singletons

    private final RootCommandNode<S> arguments = new RootCommandNode<>();
    private Command<S> command;
    private Predicate<S> requirement = defaultRequirement(); // PaperMC - default requirement singletons
    private BiPredicate<CommandContextBuilder<S>, ImmutableStringReader> contextRequirement = defaultContextRequirement(); // PaperMC - context-aware requirements
    private CommandNode<S> target;
    private RedirectModifier<S> modifier = null;
    private boolean forks;

    protected abstract T getThis();

    public T then(final ArgumentBuilder<S, ?> argument) {
        if (target != null) {
            throw new IllegalStateException("Cannot add children to a redirected node");
        }
        arguments.addChild(argument.build());
        return getThis();
    }

    public T then(final CommandNode<S> argument) {
        if (target != null) {
            throw new IllegalStateException("Cannot add children to a redirected node");
        }
        arguments.addChild(argument);
        return getThis();
    }

    public Collection<CommandNode<S>> getArguments() {
        return arguments.getChildren();
    }

    public T executes(final Command<S> command) {
        this.command = command;
        return getThis();
    }

    public Command<S> getCommand() {
        return command;
    }

    public T requires(final Predicate<S> requirement) {
        this.requirement = requirement;
        return getThis();
    }

    public Predicate<S> getRequirement() {
        return requirement;
    }

    // PaperMC start - context-aware requirements
    /**
     * Sets a requirement that is checked after the node has been parsed, with access to the context built so far
     * (including the arguments parsed for this node) and the reader positioned right after this node's input.
     *
     * <p>Unlike {@link #requires(Predicate)}, which is checked before parsing, a failing context requirement makes
     * the dispatcher act as if this node did not match the input at all.</p>
     *
     * @param requirement the requirement
     * @return this builder
     */
    public T requiresWithContext(final BiPredicate<CommandContextBuilder<S>, ImmutableStringReader> requirement) {
        this.contextRequirement = requirement;
        return getThis();
    }

    public BiPredicate<CommandContextBuilder<S>, ImmutableStringReader> getContextRequirement() {
        return contextRequirement;
    }
    // PaperMC end - context-aware requirements

    public T redirect(final CommandNode<S> target) {
        return forward(target, null, false);
    }

    public T redirect(final CommandNode<S> target, final SingleRedirectModifier<S> modifier) {
        return forward(target, modifier == null ? null : o -> Collections.singleton(modifier.apply(o)), false);
    }

    public T fork(final CommandNode<S> target, final RedirectModifier<S> modifier) {
        return forward(target, modifier, true);
    }

    public T forward(final CommandNode<S> target, final RedirectModifier<S> modifier, final boolean fork) {
        if (!arguments.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot forward a node with children");
        }
        this.target = target;
        this.modifier = modifier;
        this.forks = fork;
        return getThis();
    }

    public CommandNode<S> getRedirect() {
        return target;
    }

    public RedirectModifier<S> getRedirectModifier() {
        return modifier;
    }

    public boolean isFork() {
        return forks;
    }

    public abstract CommandNode<S> build();
}
