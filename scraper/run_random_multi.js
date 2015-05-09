//the msg.szewczyk.pro app is bugged for 1 player:
//- a player cannot play 2 games simultaneously
//
//currently casperjs/slimerjs uses shared cookies 
var require = patchRequire(require);
var Scraper = require('./scrape_casper.js');
var Q = require('node_modules/q/q');
var objUtils = require('./obj_utils.js');
var credentials = require('./user');
var credentials2 = require('./user2');

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

var config = objUtils.copy(credentials2, {
    csv: {
        headers: defHeaders,
        path: 'sample.csv',
        delim: '\t'
    }
});

var config2 = objUtils.copy(credentials, {
    csv: {
        headers: defHeaders,
        path: 'sample2.csv',
        delim: '\t'
    }
});

var configs = [config, config2];

var instances = 2;
var valueSlices = (function() {
    var l = values.length;
    var arr = [];
    for (var i = 0; i < l; i += instances) {
        arr.push(values.slice(i, i + instances));
    }
    return arr;
})();

var runner = runnerFactory(instances, configs);

var len = valueSlices.length;

function iter(i) {
    if (i >= len) {
        return;
    }
    var valSlice = valueSlices[i];

    promises = valSlice.map(function(values){
        return runner.evaluate(values);
    });
    Scraper.resume();
    Q.all(promises).then(function(dataArr) {
        setTimeout(function() {
            iter(i + 1);
        }, 0);
    });

};

function runnerFactory(inNum, configs) {
    var instances = [];
    for (var i = 0; i < inNum; i++) {
        instances.push({
            instance: Scraper(configs[i]),
            queue: []
        });
    }

    var cins = 0;
    return {

        evaluate: function evaluate(values) {
            var queue = instances[cins].queue;
            var ins = instances[cins].instance;
            var deferred = Q.defer();
            ins.evaluate(values, function(data) {
                Scraper.pause();
                deferred.resolve(data);
            });
            queue.push(values);
            cins = (cins + 1) % inNum;
            return deferred.promise;
        }
    }

};

iter(0);
