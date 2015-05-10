var randgen = require('./node_modules/randgen/lib/randgen');
function samplingInfo(min, max, inc, label) {
    return {
        label: label,
        min: min,
        max: max,
        inc: inc,
        randomSample: function() {
            var r = (Math.random() * ((this.max - this.min) / this.inc + 1));
            return Math.floor(r) * this.inc + this.min;
        },
        stdRandSample: function() {
            var r = (randgen.rnorm() * ((this.max - this.min) / this.inc + 1));
            return Math.floor(r) * this.inc + this.min;
        },
        randomDrift: function() {
            return (Math.floor(Math.random() * 3) - 1) * this.inc;
        }
    };
};

module.exports = samplingInfo;
