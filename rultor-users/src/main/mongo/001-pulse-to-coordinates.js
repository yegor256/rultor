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

/**
 * Convert PULSE into COORDINATES.
 */
db.stands.find({coordinates: {"$exists": false}}).forEach(
    function(item) {
        var pulse = item.pulse;
        var owner;
        var rule;
        var scheduled;
        if (/^urn:/.test(pulse)) {
            // urn:facebook:1531296526:rultor-on-commit:2013-08-28T18:05:00Z
            var parts = pulse.split(":");
            owner = [parts[0], parts[1], parts[2]].join(":");
            rule = parts[3];
            scheduled = [parts[4], parts[5], parts[6]].join(":");
        } else if (pulse.indexOf(" ") !== -1) {
            // 2013-08-29T19:25:00Z urn:facebook:1531296526 rultor-on-commit
            var parts = pulse.split(" ");
            owner = parts[1];
            rule = parts[2];
            scheduled = parts[0];
        } else if (/^2013/.test(pulse)) {
            // 2013-08-28T20:35:00Z:urn:facebook:1531296526:rultor-on-commit
            var parts = pulse.split(":");
            owner = [parts[3], parts[4], parts[5]].join(":");
            rule = parts[6];
            scheduled = [parts[0], parts[1], parts[2]].join(":");
        } else {
            print("unrecognized format: " + pulse);
            return;
        }
        db.stands.update(
            { "_id": item._id },
            {
                "$set": {
                    "coordinates": {
                        "owner": owner,
                        "rule": rule,
                        "scheduled": scheduled
                    }
                }
            }
        );
        print("changed " + pulse + " to {owner:" + owner + ", rule:" + rule + ", scheduled:" + scheduled + "}");
    }
);

