var require = patchRequire(require);
var Scraper = require('./scrape_generic.js');
var objUtils = require('./obj_utils.js');
var Q = require('./node_modules/q/q');
var samplingInfo = require('./sampling.js');

var cliArgs = (function(){
    var casper = require("casper").create();
    return casper.cli.args;
})();

var credentials = (function(){
    if(cliArgs.length){
        return require('./user')[cliArgs[0]];
    }else{
        return require('./user')['default'];
    }
})();

var login = credentials.username;
var pass = credentials.password;

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

var date = new Date();
var ts = date.getDate()+'-'+date.getHours()+date.getMinutes();
var config = objUtils.copy(credentials, {
    csv: {
        headers: defHeaders,
        path: 'random'+ts+'.csv',
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
