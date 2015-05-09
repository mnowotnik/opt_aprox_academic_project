var require = patchRequire(require);
var Scraper = require('./scrape_casper.js');
var objUtils = require('./obj_utils.js');
var credentials = require('./user');
var Q = require('./node_modules/q/q');

var login = credentials.username;
var pass = credentials.password;

function samplingInfo(min, max, inc, label) {
    return {
        label: label,
        min: min,
        max: max,
        inc: inc,
        randomSample: function() {
            var r = (Math.random() * ((this.max - this.min) / this.inc + 1));
            return Math.floor(r) * this.inc + this.min;
        }
    };
};

var values = (function() {

    var commercialRate = samplingInfo(0, 50000, 1000, null);
    var tvRate = objUtils.copy(commercialRate, {
        label: 'tv'
    });
    var internetRate = objUtils.copy(commercialRate, {
        label: 'internet'
    });
    var warehouseRate = objUtils.copy(commercialRate, {
        label: 'warehouse'
    });
    var qualityRate = samplingInfo(65, 90, 1, 'quality');
    var priceRate = samplingInfo(10, 25, 1, 'price');
    var vec = [qualityRate, tvRate, internetRate, warehouseRate, priceRate];
    var volume = 50000;

    function makeSample(vec) {
        var l = vec.length;
        var s = {};
        for (var i = 0; i < l; i++) {
            var gen = vec[i];
            s[gen.label] = gen.randomSample();
        }
        s.volume = volume;
        return s;
    }
    var samples = [];
    for (var i = 0; i < 10000; i++) {
        samples.push(makeSample(vec));
    }
    return samples;
})();


var defHeaders = ['volume', 'quality', 'tv', 'internet', 'warehouse', 'price',
    'sold_num', 'sold_ratio', 'income', 'return_rate'
];

var config = objUtils.copy(credentials, {
    csv: {
        headers: defHeaders,
        path: 'sample.csv',
        delim: '\t'
    }
});

var scraper = Scraper(config);
var l = values.length;

function iter(i) {
    if (i >= l) {
        return;
    }
    var valSlice = [values[i]];
    promises = valSlice.map(function(values){
        return evaluate(values);
    });
    // Scraper.resume();
    Q.all(promises).then(function(dataArr) {
        setTimeout(function() {
            iter(i + 1);
        }, 0);
    });
}

function evaluate(values) {
    var deferred = Q.defer();
    scraper.evaluate(values, function(data) {
        // Scraper.pause();
        deferred.resolve(data);
    });
    return deferred.promise;
};

iter(0);
