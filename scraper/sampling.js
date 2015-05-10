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
            var r = ((randgen.rnorm()+1) * ((this.max - this.min) / this.inc + 1));
            if(r<0){
                return this.min;
            }
            return Math.floor(r) * this.inc + this.min;
        },
        randomDrift: function() {
            var drift =(Math.floor(Math.random() * 3) - 1) * this.inc;
            return drift;
        },
        stdRandDrift: function(){
            var rand = randgen.rnorm(0,5);
            var drift = rand*this.inc;
            if(drift<0){
                drift = Math.ceil(drift);
            }else{
                drift = Math.floor(drift);
            }
            return drift;
        }
    };
};

module.exports = samplingInfo;
