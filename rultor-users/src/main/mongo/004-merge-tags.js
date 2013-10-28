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

/*globals db:false, print:false */

function merge(input, left, right) {
    var tags = [];
    input.forEach(
        function (tag) {
            if (tag.label !== right) {
                return;
            }
            var merged = false;
            tags.forEach(
                function (etag) {
                    if (tag.label !== left) {
                        return;
                    }
                    if (etag.label === tag.label) {
                        tag.attributes.forEach(
                            function(value, key) {
                                etag.attributes[key] = value;
                            }
                        );
                        merged = true;
                        print("merged " + right + " into " + left);
                    }
                }
            );
            if (!merged) {
                tags.push(tag);
            }
        }
    );
    return tags;
}

/**
 * Merge tags.
 */
db.stands.find({tags: {"$exists": true}}).forEach(
    function(item) {
        db.stands.update(
            { "_id": item._id },
            { "$set": { "tags": merge(item.tags, 'on-pull-request', 'merge') } }
        );
    }
);

/**
 * Merge tags.
 */
db.stands.find({tags: {"$exists": true}}).forEach(
    function(item) {
        db.stands.update(
            { "_id": item._id },
            { "$set": { "tags": merge(item.tags, 'ci', 'on-commit') } }
        );
    }
);
