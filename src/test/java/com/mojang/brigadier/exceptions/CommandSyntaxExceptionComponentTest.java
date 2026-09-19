// PaperMC - adventure component messages
// Copyright (c) PaperMC Developers. All rights reserved.
// Licensed under the MIT license.

package com.mojang.brigadier.exceptions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.junit.After;
import org.junit.Test;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CommandSyntaxExceptionComponentTest {
    private final Function<Message, Component> originalConverter = CommandSyntaxException.MESSAGE_TO_COMPONENT;

    @After
    public void restoreConverter() {
        CommandSyntaxException.MESSAGE_TO_COMPONENT = originalConverter;
    }

    @Test
    public void testPlainMessage() {
        final CommandSyntaxException ex = new SimpleCommandExceptionType(new LiteralMessage("oops")).createWithContext(new com.mojang.brigadier.StringReader("foo bar"));
        assertThat(ex.componentMessage(), equalTo(Component.text("oops")));
        assertThat(ComponentMessageThrowable.getOrConvertMessage(ex), equalTo(Component.text("oops")));
    }

    @Test
    public void testComponentLikeMessage() {
        final Component component = Component.text("rich", NamedTextColor.RED);
        final Message message = new ComponentMessage(component);
        assertThat(new SimpleCommandExceptionType(message).create().componentMessage(), equalTo(component));
    }

    @Test
    public void testCustomConverter() {
        CommandSyntaxException.MESSAGE_TO_COMPONENT = message -> Component.text("converted " + message.getString());
        assertThat(new SimpleCommandExceptionType(new LiteralMessage("x")).create().componentMessage(), equalTo(Component.text("converted x")));
    }

    private static final class ComponentMessage implements Message, ComponentLike {
        private final Component component;

        ComponentMessage(final Component component) {
            this.component = component;
        }

        @Override
        public String getString() {
            return "plain";
        }

        @Override
        public Component asComponent() {
            return component;
        }
    }
}
