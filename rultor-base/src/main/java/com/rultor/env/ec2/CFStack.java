/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.env.ec2;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.CFClient;
import com.rultor.env.Environment;
import com.rultor.snapshot.Step;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * CloudFormation stack.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "name", "client" })
@Loggable(Loggable.DEBUG)
final class CFStack implements Environment {

    /**
     * Stack ID.
     */
    private final transient String name;

    /**
     * EC2 client.
     */
    private final transient CFClient client;

    /**
     * Public ctor.
     * @param stack Stack ID
     * @param clnt EC2 client
     */
    protected CFStack(final String stack, final CFClient clnt) {
        this.name = stack;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "CloudFormation stack `%s` accessed with %s",
            this.name, this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress address() throws IOException {
        final Stack stack = this.whenReady();
        return CFStack.address(stack);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Step("requested deletion of stack `${this.name}`")
    public void close() throws IOException {
        final AmazonCloudFormation aws = this.client.get();
        try {
            aws.deleteStack(new DeleteStackRequest().withStackName(this.name));
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Create CF stack.
     * @return The stack created
     * @throws IOException If fails
     */
    @Step(
        before = "waiting for CloudFormation stack `${this.name}`",
        value = "CF stack `${this.name}` is ready as `${result.getStackId()}`"
    )
    private Stack whenReady() throws IOException {
        final AmazonCloudFormation aws = this.client.get();
        final DescribeStacksRequest request = new DescribeStacksRequest()
            .withStackName(this.name);
        try {
            while (true) {
                final DescribeStacksResult result = aws.describeStacks(request);
                final Stack stack = result.getStacks().get(0);
                Logger.info(
                    this,
                    "stack `%s` is in `%s` status: %s",
                    stack.getStackId(),
                    stack.getStackStatus(),
                    stack.getStackStatusReason()
                );
                if ("CREATE_COMPLETE".equals(stack.getStackStatus())) {
                    return stack;
                }
                if (!"CREATE_IN_PROGRESS".equals(stack.getStackStatus())) {
                    throw new IllegalStateException(
                        String.format(
                            "stack `%s` is in invalid state `%s`",
                            stack.getStackId(),
                            stack.getStackStatus()
                        )
                    );
                }
                try {
                    TimeUnit.SECONDS.sleep(Tv.FIFTEEN);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
            }
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Get address from stack.
     * @param stack Stack
     * @return IP address of the server
     * @throws IOException If fails
     */
    private static InetAddress address(final Stack stack) throws IOException {
        InetAddress address = null;
        for (Output output : stack.getOutputs()) {
            final String name = output.getOutputKey()
                .toLowerCase(Locale.ENGLISH);
            if ("ip".equals(name)) {
                address = InetAddress.getByName(output.getOutputValue());
                break;
            }
        }
        if (address == null) {
            throw new IllegalArgumentException(
                String.format(
                    "no IP output from stack `%s`", stack.getStackId()
                )
            );
        }
        return address;
    }

}
