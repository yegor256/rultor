/**
 * Execute it like this:
 * $ mongo -u rultor -p ... ds037688.mongolab.com:37688/pulses rultor-users/src/main/mongo/normalize.js
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

/**
 * Convert plain-text tags to objects.
 */
db.stands.find({tags: {"$exists": true}}).forEach(
    function(item) {
        var tags = new Array();
        item.tags.forEach(
            function(tag) {
                if (typeof tag === 'string') {
                    tags.push(
                        {
                            "label": tag,
                            "level": "INFO",
                            "data": "{}",
                            "markdown": ""
                        }
                    );
                } else {
                    tags.push(tag);
                }
            }
        );
        db.stands.update(
            { "_id": item._id },
            {
                "$set": {
                    "tags": tags
                }
            }
        );
        print("converted " + tags.length + " tag(s) in " + item.coordinates.rule);
    }
);
