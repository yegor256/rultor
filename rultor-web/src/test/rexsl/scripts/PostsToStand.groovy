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
package com.rultor.web.rexsl.scripts

/*
import com.jcabi.manifests.Manifests
import com.jcabi.urn.URN
import com.rexsl.page.auth.Identity
import com.rultor.client.RestUser
import com.rultor.spi.Spec
import com.rultor.web.AuthKeys
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

Manifests.append(new File(rexsl.basedir, 'target/test-classes/META-INF/MANIFEST.MF'))
def identity = new Identity.Simple(new URN('urn:facebook:1'), '', new URI('#'))
def key = new AuthKeys().make(identity)
def user = new RestUser(rexsl.home, identity.urn(), key)

def name = 'sample-unit'
if (!user.stands().contains(name)) {
    user.stands().create(name)
}
def stand = user.stands().get(name)
stand.acl(new Spec.Simple('com.rultor.acl.MD5Keyed("test")'))
stand.post(name, 'ADD "test"; SET "works fine!";')
def pulse = user.rules().pulses().iterator().next()
MatcherAssert.assertThat(pulse.xembly(), Matchers.containsString('works fine'))
*/
