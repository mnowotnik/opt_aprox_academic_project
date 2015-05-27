//run : casperjs --engine=slimerjs script.js
//
var require = patchRequire(require);
var jQueryUrl = "https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"

var fs = require('fs');
var botname = 'BOT-';
var hashes = require('node_modules/jshashes/hashes.min.js');
var MD5 = hashes.MD5;
var csvFactory = require('./csv');
var Casper = require('casper');
var url = 'http://msg.szewczyk.pro';
var objUtils = require('./obj_utils.js');

var GROSS_INC_SELECT = 'input[tabindex="0"]:eq(9)';
var SOLD_CT_SELECT = 'input[tabindex="0"]:eq(1)';
var FREE_CASH_SELECT = 'input[tabindex="0"]:eq(29)';
var CASH_SELECT = 'input[tabindex="0"][maxlength="12"]';
var UPRICE_SELECT = 'input[tabindex="0"]:eq(2)';
var COMMIT_SELECT = ':contains("Zatwierdź")[class="v-button v-widget"]';
var SCORES_TAB_SELECT = 'div[class="v-caption"]:contains(Wyniki)';
var GAME_LIST_SELECT = 'div > a :contains("List gier")';
var DECISIONS_TAB_SELECT = 'div[class="v-caption"]:contains(Decyzje)';

var DEBUG_FLAG = true;

var PRODUCT_CONST_COST = 10000;

var _void = function() {};

var lock = {
    paused: false,
    pause: function() {
        this.paused = true;
    },
    resume: function() {
        this.paused = false;
    },
    unlocked: function() {
        return !this.paused;
    }
};

var defHeaders = ['vol', 'qual', 'tv', 'internet', 'warehouse', 'price',
    'sold_num', 'sold_ratio', 'income', 'return_rate'
];

var defCsvPath = function() {
    var hash = new MD5;
    return 'samples_' + hash.hex(Math.random()).slice(25) + '.csv';
}

var scraper = function(config) {
    var self = {};

    if (config.csv) {
        var csvInfo = config.csv;
        csvInfo.path = csvInfo.path || defCsvPath();
        csvInfo.delim = csvInfo.delim || '\t';
        csvInfo.headers = csvInfo.headers || defHeaders;
        var csv = csvFactory(csvInfo.path, csvInfo.delim, csvInfo.headers);
        self.csv = csv;
    }
    self.rounds = config.rounds || 10;
    self.players = config.players || 1;

    var login = config.username;
    var pass = config.password;

    var casper = Casper.create({
        remoteScripts: [jQueryUrl],
        // pageSettings: {
        //     loadImages: false,
        //     loadPlugins: false
        // },
        logLevel: "info",
        // verbose: true,
        waitTimeout: 600000
    });

    function start() {

        if (!self.started) {
            casper.start(url);

            casper.waitForSelector('#loginButton', function() {
                console.log('login')
                this.evaluate(function(login, password) {
                    $("#j_username").val(login);
                    $("#j_password").val(password);
                    $("#loginButton").click();
                }, login, pass);
            });
            self.started = true;
        }
    }


    function upriceSampling(values) {

        var csv = self.csv;
        start();
        initGame();
        clickDecisions();
        var len = values.length;

        function iter(ii) {
            if (ii >= len) {
                return;
            }

            var val = values[ii];


            casper.then(function() {
                casper.evaluate(function() {
                    $('[tabindex="0"]:eq(3)')[0].value = '-';
                });
                setVolume(val.volume);
                if (setQuality(val.quality)) {
                    casper.wait(1000);
                }
                casper.evaluate(function() {
                    $('[tabindex="0"]:eq(3)')[0].value = '-';
                });
            });
            casper.waitFor(function() {
                return casper.evaluate(function() {
                    return $('[tabindex="0"]:eq(3)')[0].value !== '-';
                });
            }, function() {}, function() {}, 3000);

            readFloat('[tabindex="0"]:eq(3)', setField('earlyUnitPrice'));

            casper.then(function() {
                if (isNaN(self.earlyUnitPrice)) {
                    casper.waitFor(function() {
                        return !isIndicator();
                    });
                    readFloat('[tabindex="0"]:eq(3)', setField('earlyUnitPrice'));
                }
            });

            casper.then(function() {
                if (!isNaN(self.earlyUnitPrice)) {
                    self.unitPrice = self.earlyUnitPrice;
                }
            });

            casper.then(function() {
                console.log(self.unitPrice);
                var sample = {
                    volume: val.volume,
                    quality: val.quality,
                    unit_price: self.unitPrice
                };
                writeRowToCsv(sample);
                iter(ii + 1);
            });
        };
        iter(0);
    };

    function round(values, callback) {
        if (!self.iteration) {
            self.iteration = 0;
        }

        var csv = self.csv;
        start();

        casper.then(function() {
            if (self.iteration == 0) {
                initGame();
            }
        });
        clickDecisions();

        casper.waitFor(function() {
            return casper.evaluate(function() {
                return $('[tabindex="0"]:eq(-2)').is(':visible');
            });
        }, function() {
            readInt('[tabindex="0"]:eq(-2)', setField('demand'));
        });

        casper.then(function() {
            if (!values.volume){
                values.dynamicVolume = true; 
            }
            if (values.dynamicVolume) {
                values.volume = Math.floor(0.8 * self.demand);
            }
            setVolume(values.volume);
            setQuality(values.quality);
            setSellPrice(values.price);
            setTv(values.tv);
            setInternet(values.internet);
            setWarehouse(values.warehouse);

            self.oldVal = values;
        });
        readInt(CASH_SELECT, setField('money'));
        readInt(FREE_CASH_SELECT, setField('moneyLeft'));
        readFloat(UPRICE_SELECT, setField('unitPrice'));

        //commit
        casper.then(function() {
            click(COMMIT_SELECT, 1000);
        });

        click(SCORES_TAB_SELECT);
        casper.waitForText('Udział w rynku');

        readInt(GROSS_INC_SELECT, setField('oldIncome'));
        readInt(SOLD_CT_SELECT, setField('oldSoldNum'));

        casper.then(function() {
            casper.evaluate(function(selinc,selnum) {
                $(selinc)[0].value = '-';
                $(selnum)[0].value = '-';
            }, GROSS_INC_SELECT, SOLD_CT_SELECT);
        });
        casper.waitFor(function() {
            return casper.evaluate(function(selinc, selnum) {
                return $(selinc)[0].value !== '-' ||
                    $(selnum)[0].value !== '-';
            }, GROSS_INC_SELECT, SOLD_CT_SELECT);
        }, _void, _void, 15000);
        casper.wait(500);

        readInt(GROSS_INC_SELECT, setField('income'));
        readInt(SOLD_CT_SELECT, setField('soldNum'));

        casper.then(function() {
            if (isNaN(self.income)){
                casper.wait(5000, function() {
                    readInt(GROSS_INC_SELECT, setField('income'));
                });
            }
            if(isNan(self.soldNum)){
                casper.wait(2000, function() {
                    readInt(SOLD_CT_SELECT, setField('soldNum'));
                });
            }
                
        });
        casper.then(function() {
            if (isNaN(self.income) && !isNaN(self.oldIncome)) {
                self.income = self.oldIncome;
            }
            if(isNaN(self.soldNum) && !isNaN(self.oldSoldNum)){
                self.soldNum = self.oldSoldNum;
            }
        });


        casper.then(function() {

            self.iteration++;
            if (self.iteration >= self.rounds) {
                self.iteration = 0;
                click(GAME_LIST_SELECT, 1000);
                casper.waitForText('Filtruj');
            }
            self.soldRatio = self.soldNum / values.volume;
            self.moneySpent = self.money - self.moneyLeft;
            if (config.extrapolate) {
                var moneySpent = values.tv + values.internet + values.warehouse + PRODUCT_CONST_COST;
                var moneyLimit = config.extra.moneyLimit;
                var unitPrice = self.unitPrice;
                var volume = Math.round((moneyLimit-moneySpent)/unitPrice);
                var volLimit = config.extra.volumeLimit;
                if(volLimit && volLimit<volume){
                    volume = volLimit;
                }
                moneySpent += volume * unitPrice;
                var income = Math.round(self.soldRatio * volume * values.price);
                msg('spent',moneySpent,'inc', income,'vol',volume);
                self.returnRate = income / moneySpent;
            } else {
                self.returnRate = self.income / self.moneySpent;
            }


            var sample = objUtils.copy(values, {
                sold_num: self.soldNum,
                sold_ratio: self.soldRatio,
                income: self.income,
                return_rate: self.returnRate,
                unit_price: self.unitPrice
            });

            var results = ['money', self.money,
                'moneyLeft', self.moneyLeft,
                'moneySpent', self.moneySpent,
                'uprice', self.unitPrice,
                'price', values.price,
                'quality', values.quality,
                'grossInc', self.income,
                'sold', self.soldNum,
                'volume', values.volume,
                'returnRate', self.returnRate,
                'percSold', self.soldRatio
            ];

            msg.apply(null, results);

            writeRowToCsv(sample);
            debug('run cb')
            callback(objUtils.copy(values, {
                soldNum: self.soldNum,
                soldRatio: self.soldRatio,
                income: self.income,
                returnRate: self.returnRate,
                unitPrice: self.unitPrice
            }));
        });
        casper.waitFor(function() {
            return lock.unlocked();
        });
    };

    function initGame() {
        casper.waitForSelector('#addNewGameLink', function() {
            this.thenOpen(url + '/#!add-new-game');
        });
        casper.waitForSelector('#addNewGameButton');
        var hash = new MD5;
        var hashstr = hash.hex(Math.random() + 7535 + 'DEADBEEF');
        setGame(self.players, Math.pow(10, 9), self.rounds, botname + hashstr.slice(20));
        readField('#nameInput', setField('game'));
        click('#addNewGameButton', 1);
        casper.waitForSelector('#addNewGameLink', function() {
            this.evaluate(function(game) {
                $("[id='join-" + game + "']")[0].click();
            }, self.game);
        });

        casper.waitFor(function() {
            return casper.evaluate(function(sel) {
                return $(sel).is(':visible');
            }, DECISIONS_TAB_SELECT);
        })
    }

    function clickDecisions() {
        click(DECISIONS_TAB_SELECT, 1000);
        casper.waitForText('luksusowy');
        casper.waitFor(function() {
            return casper.evaluate(function(sel) {
                return $(sel).is(':visible');
            }, COMMIT_SELECT);
        })

    }

    function waitForNotEqual(selector, val) {
        casper.waitFor(function() {
            return casper.evaluate(function() {
                return $(selector)[0].value !== val;
            });
        });
    };

    function isIndicator() {
        return casper.evaluate(function() {
            var indic = $('[class="v-loading-indicator first"]').is(':visible');
            indic = indic || $('[class="v-loading-indicator second v-loading-indicator-delay"]').is(':visible');
            indic = indic || $('[class="v-loading-indicator third v-loading-indicator-wait"]').is(':visible');
            return indic;
        });
    };

    var setParam = function(selector, val, eq) {
        var cval = '';
        readField(selector, function(value) {
            cval = value;
        });
        var thenFunc = function(selector, val, eq) {
            if (cval === val.toString()) {
                return false;
            }

            if (eq) {
                click(selector + ':eq(' + eq + ')', 1);
            } else {
                casper.click(selector);
            }
            // this.wait(500);
            var l = casper.evaluate(function(selector, val, eq) {
                if (!eq) {
                    eq = 0;
                }
                $(selector)[eq].value = val;
                return $(selector)[0].outerHTML;
            }, selector, val.toString(), eq);
            if (eq) {
                click(selector + ':eq(' + eq + ')', 1);
            } else {
                casper.click(selector);
            }
            casper.wait(1500);
        };
        casper.then(thenFunc.bind(casper, selector, val, eq));
        casper.waitFor(function() {
            return !isIndicator();
        })
        return true;
    };

    var setQuality = objUtils.partial(setParam, 'input[tabindex="2"]');
    var setVolume = objUtils.partial(setParam, 'input[tabindex="1"]');
    var setTv = objUtils.partial(setParam, 'input[tabindex="3"]');
    var setInternet = objUtils.partial(setParam, 'input[tabindex="4"]');
    var setWarehouse = objUtils.partial(setParam, 'input[tabindex="5"]');
    var setSellPrice = objUtils.partial(setParam, 'input[tabindex="6"]');
    var setGame = function(players, cash, rounds, name) {
        casper.then(function() {
            setParam('input', rounds, 2);
            setParam('input', cash, 3);
            setParam('#playersInput', '');
            this.then(function() {
                // this.sendKeys('#playersInput','',{keepFocus:true});
                casper.sendKeys('#playersInput', players.toString(), {
                    keepFocus: true
                });
            });
            this.wait(1200);
            if (name) {
                setParam('input', name, 0);
            }
        })
        casper.then(function() {
            this.evaluate(function() {
                $('input:eq(2)').focus()
                    .blur().trigger('change').trigger('mousedown').trigger('mouseup').trigger('mousemove');
                $('input:eq(3)').focus()
                    .blur().trigger('change').trigger('mousedown').trigger('mouseup').trigger('mousemove');
                $("#playersInput").focus()
                    .blur().trigger('change').trigger('mousedown').trigger('mouseup').trigger('mousemove');
            })
        });
    };

    function click(selector, time) {
        casper.then(function() {
            this.evaluate(function(selector) {
                $(selector)[0].click();
            }, selector);
        });
        if (time) {
            casper.wait(time);
        }
    };


    function readField(selector, cb) {
        casper.then(function() {
            var val = this.evaluate(function(selector) {
                return $(selector)[0].value;
            }, selector);
            cb(val);
        });
    }

    function readConv(selector, cb, conv) {
        readField(selector, function(val) {
            cb(conv(val));
        });
    };

    function readFloat(selector, cb) {
        readConv(selector, cb, parseFloat);
    };

    function readInt(selector, cb) {
        readConv(selector, cb, parseInt);
    };

    function setField(field) {
        return function(val) {
            self[field] = val;
        }
    };

    function msg() {
        arr = Array.prototype.slice.apply(arguments);
        console.log(arr.join(' '));
    }

    function debug() {
        if (DEBUG_FLAG) {
            msg.apply(null, arguments);
        }
    }

    function assert(cond, message) {
        if (!cond) {
            throw message;
        }
    }


    function writeRowToCsv(data) {
        casper.then(function() {
            if (self.csv) {
                csv.writeMap(data);
                csv.flush();
            }
        });
    };

    return {
        evaluate: function(values, cb) {
            round(values, cb);
            if (!self.initFlag) {
                casper.run();
                self.initFlag = true;
            }
        },
        close: function() {
            if (self.csv) {
                casper.then(function() {
                    self.csv.close();
                });
            }
        },
        sampleUnitPrice: function(valArray) {
            upriceSampling(valArray);
            casper.run();
        },
        casper: casper
    }
};

scraper.resume = lock.resume.bind(lock);
scraper.pause = lock.pause.bind(lock);
scraper.unlocked = lock.unlocked.bind(lock);

module.exports = scraper;
