// PaperMC - node attachments
// Copyright (c) PaperMC Developers. All rights reserved.
// Licensed under the MIT license.

package com.mojang.brigadier.tree;

/**
 * A typed key for data attached to a {@link CommandNode} via {@link CommandNode#setAttachment(NodeAttachmentKey, Object)}.
 *
 * <p>Attachments let the platform hosting a dispatcher associate its own data with nodes it did not create
 * (for example nodes built by plugins through the regular builders), without resorting to identity maps.
 * Keys are compared by identity, so each holder should create its keys once and keep them in a static field.</p>
 *
 * <p>Attachments are bound to the node instance: they are neither copied by {@link CommandNode#createBuilder()}
 * nor merged by {@link CommandNode#addChild(CommandNode)}.</p>
 *
 * @param <T> the type of the attached value
 */
public final class NodeAttachmentKey<T> {
    private final String name;

    private NodeAttachmentKey(final String name) {
        this.name = name;
    }

    /**
     * Creates a new, unique key.
     *
     * @param name a descriptive name, used for debugging only
     * @param <T> the type of the attached value
     * @return the key
     */
    public static <T> NodeAttachmentKey<T> create(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return new NodeAttachmentKey<>(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "NodeAttachmentKey{" + name + "}";
    }
}
