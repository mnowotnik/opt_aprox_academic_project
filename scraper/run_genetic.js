var require = patchRequire(require);
var Scraper = require('./scrape_generic.js');
var objUtils = require('./obj_utils.js');
var Q = require('./node_modules/q/q');
var Genetic = require('./node_modules/genetic-js/lib/genetic');
var randgen = require('./node_modules/randgen/lib/randgen');
var samplingInfo = require('./sampling.js');

var cliArgs = (function() {
    var casper = require("casper").create();
    return casper.cli.args;
})();

var credentials = (function() {
    if (cliArgs.length) {
        return require('./user')[cliArgs[0]];
    } else {
        return require('./user')['default'];
    }
})();

var login = credentials.username;
var pass = credentials.password;

var genetic = Genetic.create();
genetic.optimize = Genetic.Optimize.Maximize;
genetic.select1 = Genetic.Select1.Tournament2;
genetic.select2 = Genetic.Select2.Tournament2;

var features = (function() {

    var commercialRate = samplingInfo(0, 0, 1000, null);
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

    return [qualityRate, tvRate, internetRate, warehouseRate, priceRate];
})();


genetic.seed = function() {
    var l = features.length;
    var s = {};
    for (var i = 0; i < l; i++) {
        var gen = features[i];
        s[gen.label] = gen.randomSample();
    }
    s.volume=10000;
    return s;
};

genetic.mutate = function(entity) {
    var i = Math.floor(Math.random() * features.length);
    var label = features[i].label;
    var drift = features[i].randomDrift();
    var mutated = entity[label] + drift;
    if (mutated < features[i].min) {
        mutated = features[i].min;
    } else if (mutated > features[i].max) {
        mutated = features[i].max;
    }
    entity[label] = mutated;
    return entity;
};

genetic.crossover = (function() {
    function copyFeatures(from, to, r1, r2) {
        for (var i = r1; i < r2; i++) {
            to[features[i].label] = from[features[i].label];
        }
    };

    function generate(i1, i2, a, b) {
        var child = objUtils.copy(i1);
        copyFeatures(i1, child, 0, a);
        copyFeatures(i2, child, a, b);
        copyFeatures(i1, child, b, features.length);
        return child;
    };

    function crossover(mother, father) {
        var a = Math.floor(Math.random() * features.length);
        var b = Math.floor(Math.random() * features.length);

        if (a > b) {
            var tmp = a;
            a = b;
            b = tmp;
        }

        var son = generate(father, mother, a, b);
        var daughter = generate(mother, father, a, b);
        return [son, daughter];
    };
    return crossover;
})();

genetic.generation = function(pop, generation, stats) {
    return true;
}

genetic.notification = function(pop, generation, stats, isFinished) {
    var entity = pop[0].entity;

    var buf = '';
    buf += 'vol:' + entity.volume + ' ';
    buf += 'qual:' + entity.quality + ' ';
    buf += 'price:' + entity.price + ' ';
    buf += 'tv:' + entity.tv + ' ';
    buf += 'internet:' + entity.internet + ' ';
    buf += 'warehouse:' + entity.warehouse + ' ';
    buf += 'fitness:' + pop[0].fitness;
    console.log(buf);
};

var scraper = (function() {
    var headers = ['volume', 'quality', 'tv', 'internet', 'warehouse', 'price',
        'sold_num', 'sold_ratio', 'income', 'return_rate', 'unit_price'
    ];

    var date = new Date();
    var ts = date.getDate() + '-' + date.getHours() + date.getMinutes();
    var config = objUtils.copy(credentials, {
        rounds: 10,
        csv: {
            headers: headers,
            path: 'data/genetic' + ts + '.csv',
            delim: '\t'
        }
    });
    return Scraper(config);
})();

genetic.fitness = function(entity) {
    // var fitness = 0;
    var deferred = Q.defer();

    // fitness = Math.exp(-Math.pow(entity.quality - 79, 2));
    // fitness *= Math.exp(-Math.pow(entity.price - 19, 2));
    scraper.evaluate(entity, function(results) {
        console.log(results.returnRate);
        deferred.resolve(results.returnRate);
    })

    // deferred.resolve(fitness);
    return deferred.promise;
};

var config = {
    "iterations": 40000,
    "size": 29,
    "crossover": 0.3,
    "mutation": 0.3,
    "skip": 0,
    "webWorkers": false
};
genetic.evolve(config);
scraper.close();
