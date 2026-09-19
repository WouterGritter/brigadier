// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package com.mojang.brigadier.tree;

import com.mojang.brigadier.Command;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Predicate;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.Silent.class)
public abstract class AbstractCommandNodeTest {
    @Mock
    private Command command;

    protected abstract CommandNode<Object> getCommandNode();

    @Test
    public void testAddChild() throws Exception {
        final CommandNode<Object> node = getCommandNode();

        node.addChild(literal("child1").build());
        node.addChild(literal("child2").build());
        node.addChild(literal("child1").build());

        assertThat(node.getChildren(), hasSize(2));
    }

    @Test
    public void testAddChildMergesGrandchildren() throws Exception {
        final CommandNode<Object> node = getCommandNode();

        node.addChild(literal("child").then(
            literal("grandchild1")
        ).build());

        node.addChild(literal("child").then(
            literal("grandchild2")
        ).build());

        assertThat(node.getChildren(), hasSize(1));
        assertThat(node.getChildren().iterator().next().getChildren(), hasSize(2));
    }

    @Test
    public void testAddChildPreservesCommand() throws Exception {
        final CommandNode<Object> node = getCommandNode();

        node.addChild(literal("child").executes(command).build());
        node.addChild(literal("child").build());

        assertThat(node.getChildren().iterator().next().getCommand(), is(command));
    }

    @Test
    public void testAddChildOverwritesCommand() throws Exception {
        final CommandNode<Object> node = getCommandNode();

        node.addChild(literal("child").build());
        node.addChild(literal("child").executes(command).build());

        assertThat(node.getChildren().iterator().next().getCommand(), is(command));
    }

    // PaperMC start
    @Test
    public void testRemoveChildByName() throws Exception {
        final CommandNode<Object> node = getCommandNode();
        node.addChild(literal("child").build());
        node.addChild(argument("arg", integer()).build());

        node.removeChildByName("child");
        assertThat(node.getChildren(), hasSize(1));
        assertThat(node.getChild("child"), is(nullValue()));
        assertThat(node.getLiteral("child"), is(nullValue()));
        assertThat(node.getRelevantNodes(new com.mojang.brigadier.StringReader("child"), new Object()), hasSize(1));

        node.removeChildByName("arg");
        assertThat(node.getChildren(), hasSize(0));
        assertThat(node.getRelevantNodes(new com.mojang.brigadier.StringReader("1"), new Object()), hasSize(0));
    }

    @Test
    public void testClearChildren() throws Exception {
        final CommandNode<Object> node = getCommandNode();
        node.addChild(literal("child").build());
        node.addChild(argument("arg", integer()).build());

        node.clearChildren();
        assertThat(node.getChildren(), hasSize(0));
        assertThat(node.getRelevantNodes(new com.mojang.brigadier.StringReader("child"), new Object()), hasSize(0));
    }

    @Test
    public void testSetRequirement() throws Exception {
        final CommandNode<Object> node = getCommandNode();
        final Object source = new Object();
        assertThat(node.canUse(source), is(true));

        final Predicate<Object> requirement = s -> false;
        node.setRequirement(requirement);
        assertThat(node.getRequirement(), is(sameInstance(requirement)));
        assertThat(node.canUse(source), is(false));
    }

    @Test
    public void testAttachments() throws Exception {
        final CommandNode<Object> node = getCommandNode();
        final NodeAttachmentKey<String> first = NodeAttachmentKey.create("first");
        final NodeAttachmentKey<Integer> second = NodeAttachmentKey.create("second");
        final NodeAttachmentKey<String> unrelated = NodeAttachmentKey.create("first");

        assertThat(node.getAttachment(first), is(nullValue()));
        assertThat(node.setAttachment(first, "a"), is(nullValue()));
        assertThat(node.setAttachment(second, 1), is(nullValue()));
        assertThat(node.getAttachment(first), is("a"));
        assertThat(node.getAttachment(second), is(1));
        assertThat(node.getAttachment(unrelated), is(nullValue()));

        assertThat(node.setAttachment(first, "b"), is("a"));
        assertThat(node.getAttachment(first), is("b"));

        assertThat(node.setAttachment(first, null), is("b"));
        assertThat(node.getAttachment(first), is(nullValue()));
        assertThat(node.getAttachment(second), is(1));
        assertThat(node.setAttachment(second, null), is(1));
        assertThat(node.setAttachment(second, null), is(nullValue()));

        // attachments are per instance, and are not carried over by builders
        assertThat(node.setAttachment(first, "c"), is(nullValue()));
        if (!(node instanceof RootCommandNode)) {
            assertThat(node.createBuilder().build().getAttachment(first), is(nullValue()));
        }
    }
    // PaperMC end
}
