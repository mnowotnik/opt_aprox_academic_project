var randgen = require('./node_modules/randgen/lib/randgen');
function samplingInfo(min, max, inc, label) {
    function bern(){
        return Math.floor(Math.random()*3)-1;
    }
    return {
        label: label,
        min: min,
        max: max,
        inc: inc,
        randomSample: function() {
            var r = (Math.random() * ((this.max - this.min) / this.inc + 1));
            return Math.floor(r) * this.inc + this.min;
        },
        randomDrift: function() {
            var rand=0;
            for(var i=0;i<10;i++){
                rand += bern();
            }
            drift = rand*this.inc;
            return drift;
        },
        stdRandDrift: function(){
            var rand = randgen.rnorm(0,10);
            if(rand<0){
                var drift = Math.ceil(rand);
            }else{
                var drift = Math.floor(rand);
            }
            drift = rand*this.inc;
            return drift;
        }
    };
};

module.exports = samplingInfo;
