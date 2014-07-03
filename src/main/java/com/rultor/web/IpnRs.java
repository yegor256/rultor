/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.web;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.urn.URN;
import com.rultor.tools.Dollars;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.Validate;
import org.hamcrest.Matchers;

/**
 * Instant Payment Notification (IPN) callback (from PayPal).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @see <a href="https://developer.paypal.com/webapps/developer/docs/classic/ipn/integration-guide/IPNIntro/">IPN variables</a>
 */
@Path("/ipn")
@Loggable(Loggable.DEBUG)
public final class IpnRs extends BaseRs {

    /**
     * Get a call from PayPal.
     * @param post Raw POST stream
     * @return The JAX-RS response
     * @throws IOException If fails on tailing
     */
    @POST
    @Path("/")
    public String index(final InputStream post) throws IOException {
        final List<Map.Entry<String, String>> pairs = this.validate(
            this.split(IOUtils.toString(post, CharEncoding.UTF_8))
        );
        final ConcurrentMap<String, String> vars = this.map(pairs);
        if ("web_accept".equals(vars.get("txn_type"))
            && "Completed".equals(vars.get("payment_status"))) {
            Validate.isTrue(
                "paypal@rultor.com".equals(vars.get("receiver_email")),
                "invalid email of money receiver"
            );
            Validate.isTrue(
                "USD".equals(vars.get("mc_currency")),
                "unexpected currency, only USD is accepted"
            );
            this.fund(vars);
        }
        return this.join(pairs);
    }

    /**
     * Fund account.
     * @param vars Vars to use
     */
    private void fund(final Map<String, String> vars) {
        final String invoice = vars.get("invoice");
        if (invoice == null) {
            throw new IllegalArgumentException("invoice not found");
        }
        final String[] parts = invoice.split(" ");
        this.users().get(URN.create(parts[0])).account().fund(
            new Dollars(
                new BigDecimal(vars.get("mc_gross"))
                    .movePointRight(Tv.SIX).longValue()
            ),
            String.format(
                "paid PayPal customer #%s (%s) on %s, TxID %s",
                vars.get("payer_id"),
                vars.get("payer_email"),
                vars.get("payment_date"),
                vars.get("txn_id")
            )
        );
    }

    /**
     * Split them into pairs.
     * @param text Raw text
     * @return Pairs found
     * @throws UnsupportedEncodingException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<Map.Entry<String, String>> split(final String text)
        throws UnsupportedEncodingException {
        final List<Map.Entry<String, String>> list =
            new LinkedList<Map.Entry<String, String>>();
        for (final String pair : text.split("&")) {
            final String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                list.add(
                    new AbstractMap.SimpleEntry<String, String>(
                        parts[0],
                        URLDecoder.decode(parts[1], CharEncoding.UTF_8)
                    )
                );
            }
        }
        return list;
    }

    /**
     * Validate them through paypal.
     * @param pairs Pairs to validate
     * @return Pairs found
     * @throws UnsupportedEncodingException If fails
     */
    private List<Map.Entry<String, String>> validate(
        final List<Map.Entry<String, String>> pairs)
        throws UnsupportedEncodingException {
        final List<Map.Entry<String, String>> ext =
            new LinkedList<Map.Entry<String, String>>();
        ext.add(
            new AbstractMap.SimpleEntry<String, String>(
                "cmd", "_notify-validate"
            )
        );
        ext.addAll(pairs);
        try {
            new JdkRequest("https://www.paypal.com/cgi-bin/webscr")
                .body()
                .set(this.join(ext))
                .back()
                .fetch()
                .as(RestResponse.class)
                .assertBody(Matchers.equalTo("VERIFIED"));
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        return pairs;
    }

    /**
     * Join them into a URL.
     * @param pairs Pairs
     * @return Joined
     * @throws UnsupportedEncodingException If fails
     */
    private String join(final List<Map.Entry<String, String>> pairs)
        throws UnsupportedEncodingException {
        final StringBuilder text = new StringBuilder();
        for (final Map.Entry<String, String> pair : pairs) {
            if (text.length() > 0) {
                text.append('&');
            }
            text.append(pair.getKey())
                .append('=')
                .append(URLEncoder.encode(pair.getValue(), CharEncoding.UTF_8));
        }
        return text.toString();
    }

    /**
     * Convert them to a map.
     * @param pairs Pairs
     * @return Map
     */
    private ConcurrentMap<String, String> map(
        final List<Map.Entry<String, String>> pairs) {
        final ConcurrentMap<String, String> map =
            new ConcurrentHashMap<String, String>(0);
        for (final Map.Entry<String, String> pair : pairs) {
            map.put(pair.getKey(), pair.getValue());
        }
        return map;
    }

}
